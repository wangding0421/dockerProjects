package naming;

import java.io.*;
import java.net.*;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

import java.util.concurrent.ConcurrentHashMap;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{

	private Skeleton<Service> serviceSkeleton;
	private Skeleton<Registration> registrationSkeleton;

	/* consider replica, a file may be stored in several storage servers */
	private ConcurrentHashMap<Path, Set<Storage>> pathStorageMap;

	private ConcurrentHashMap<Storage, Command> storageCommandMap;

	private ConcurrentHashMap<Path, Set<Path>> fileStructure;

	private ConcurrentHashMap<Path, ReadWriteLock> dfsLocks;

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
    	this.serviceSkeleton = new NotifySkeleton<Service>(Service.class, this, new InetSocketAddress(NamingStubs.SERVICE_PORT));
		this.registrationSkeleton = new NotifySkeleton<Registration>(Registration.class, this, new InetSocketAddress(NamingStubs.REGISTRATION_PORT));
		this.pathStorageMap = new ConcurrentHashMap<Path, Set<Storage>>();
		this.storageCommandMap = new ConcurrentHashMap<Storage, Command>();
		this.fileStructure = new ConcurrentHashMap<Path, Set<Path>>();
		this.dfsLocks = new ConcurrentHashMap<Path, ReadWriteLock>();
		this.dfsLocks.put(new Path(), new ReadWriteLock());
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
		this.serviceSkeleton.start();
        this.registrationSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
		this.serviceSkeleton.stop();
		synchronized(this.serviceSkeleton){
	    	try {
				this.serviceSkeleton.wait();
			} catch (InterruptedException e) {}
    	}

    	this.registrationSkeleton.stop();
		synchronized(this.registrationSkeleton){
	    	try {
				this.registrationSkeleton.wait();
			} catch (InterruptedException e) {}
    	}

		this.stopped(null);
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
		if(path == null) throw new NullPointerException();
    	if(!this.dfsLocks.containsKey(path)) throw new FileNotFoundException();

		Path[] lockPaths = path.subPaths();

		for(int i = 0; i < lockPaths.length; i++) {

			if(i == lockPaths.length - 1) {
				if(exclusive) {
					try {
						dfsLocks.get(lockPaths[i]).lockWrite();
					} catch (InterruptedException e) {
						throw new IllegalStateException();
					}
				} else {
					try {
						dfsLocks.get(lockPaths[i]).lockRead();
					} catch (InterruptedException e) {
						throw new IllegalStateException();
					}
				}
			} else {
				/* deal with parent directory, share access */
				try {
					dfsLocks.get(lockPaths[i]).lockRead();
				} catch (InterruptedException e) {
					throw new IllegalStateException();
				}
			}
		}

    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
		if(path == null) throw new NullPointerException();
		if (!this.dfsLocks.containsKey(path)) throw new IllegalArgumentException();

    	Path[] lockPaths = path.subPaths();

    	for(int i = 0; i < lockPaths.length; i++) {
			if(i == lockPaths.length - 1) {
				if(exclusive) {
					dfsLocks.get(lockPaths[i]).unlockWrite();
				} else {
					dfsLocks.get(lockPaths[i]).unlockRead();
				}
			} else {
				dfsLocks.get(lockPaths[i]).unlockRead();
			}
    	}
    }

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
		boolean isExistedDirectory = this.fileStructure.containsKey(path);
		boolean isExistedFile = this.pathStorageMap.containsKey(path);

		if(!isExistedDirectory && !isExistedFile)
			throw new FileNotFoundException();

		return isExistedDirectory;
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
		if (!this.isDirectory(directory))
			throw new FileNotFoundException();
		Set<Path> files = this.fileStructure.get(directory);
		String[] filesInDirectory = new String[files.size()];
		int i = 0;
		for(Path path : files)
			filesInDirectory[i++] = path.last();
		return filesInDirectory;
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
		if(!file.isRoot() && !this.fileStructure.containsKey(file.parent()))
			throw new FileNotFoundException();

		if(this.storageCommandMap.isEmpty())
			throw new IllegalStateException();

		if(file.isRoot()) return false;
		if(this.fileStructure.containsKey(file)) return false;
		if(this.pathStorageMap.containsKey(file)) return false;

		Storage curStorageStub = (this.storageCommandMap.keySet()).iterator().next();
		Command curCommandStub = this.storageCommandMap.get(curStorageStub);

		boolean flag = curCommandStub.create(file);

		if(flag){
			Set<Storage> storage = new HashSet<Storage>();
			storage.add(curStorageStub);
			this.pathStorageMap.put(file, storage);
			update(file);
		}

		return flag;
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
		/* 如果父文件夹不存在,返回exception */
		if (!directory.isRoot() && !this.fileStructure.containsKey(directory.parent()))
    		throw new FileNotFoundException();

		if(directory.isRoot()) return false;
		if(this.fileStructure.containsKey(directory)) return false;
		if(this.pathStorageMap.containsKey(directory)) return false;

		update(directory);
		Set<Path> filesInDirectory = new HashSet<Path>();
		this.fileStructure.put(directory, filesInDirectory);

		return true;
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
		if (!this.pathStorageMap.containsKey(file))
			throw new FileNotFoundException();
		return (this.pathStorageMap.get(file)).iterator().next();
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
		// throw new UnsupportedOperationException("not implemented");
		if (client_stub == null || command_stub == null || files == null)
			throw new NullPointerException();
    	if (this.storageCommandMap.containsKey(client_stub))
    		throw new IllegalStateException();

    	this.storageCommandMap.put(client_stub, command_stub);

		ArrayList<Path> duplicatedPaths = new ArrayList<Path>();
		for (Path path : files){
			if(path.isRoot()) {
				continue;
			} else if(this.pathStorageMap.containsKey(path) || this.fileStructure.containsKey(path)) {
				// this file has been added or this directory has been added
				duplicatedPaths.add(path);
			} else {
				Set<Storage> storage = new HashSet<Storage>();
				storage.add(client_stub);
    			this.pathStorageMap.put(path, storage);
    			this.update(path);
			}
		}

		Path[] duplicatedPathsRes = new Path[duplicatedPaths.size()];
	    duplicatedPaths.toArray(duplicatedPathsRes);
	    return duplicatedPathsRes;
    }



	/**********helper function*****************/

	/* when you create a file like /a/b/c
	 * you should map a to b, b to c in the tree structure
	*/
	private boolean update(Path path){
		Path childNode = path;

		if(!this.dfsLocks.containsKey(childNode))
			this.dfsLocks.put(childNode, new ReadWriteLock());

		while (!childNode.isRoot()){
			Path parentNode = childNode.parent();
			if(!this.dfsLocks.containsKey(parentNode)) {
				this.dfsLocks.put(parentNode, new ReadWriteLock());
			}
			if(this.fileStructure.containsKey(parentNode)){
				this.fileStructure.get(parentNode).add(childNode);
			} else {
				Set<Path> filesInDirectory = new HashSet<Path>();
				filesInDirectory.add(childNode);
				this.fileStructure.put(parentNode, filesInDirectory);
			}
			childNode = parentNode;
		}

		return true;
	}


}
