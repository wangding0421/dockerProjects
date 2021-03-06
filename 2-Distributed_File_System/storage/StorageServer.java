package storage;

import java.io.*;
import java.net.*;

import common.*;
import rmi.*;
import naming.*;

import java.util.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{

    private Skeleton<Storage> storageSkeleton;
    private Skeleton<Command> commandSkeleton;
    private File root;
    boolean isRunning = false;

    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root, int client_port, int command_port)
    {
        if (root == null)
            throw new NullPointerException();

        this.root = root;
        this.storageSkeleton = new NotifySkeleton<Storage>(Storage.class, this, new InetSocketAddress(client_port));
        this.commandSkeleton = new NotifySkeleton<Command>(Command.class, this, new InetSocketAddress(command_port));
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        this(root, 0, 0);
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        this.commandSkeleton.start();
        this.storageSkeleton.start();

        Storage storageStub = Stub.create(Storage.class, this.storageSkeleton, hostname);
        Command commandStub = Stub.create(Command.class, this.commandSkeleton, hostname);

        Path[] duplicatedPaths = naming_server.register(storageStub, commandStub, Path.list(this.root));
        for (Path path : duplicatedPaths)
            this.delete(path);
        pruneDirectoriesWithNoFile(this.root);
        isRunning = true;
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        this.commandSkeleton.stop();
        this.storageSkeleton.stop();
        isRunning = false;
        this.stopped(null);
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        File f = file.toFile(this.root);
        if (!f.exists() || f.isDirectory())
            throw new FileNotFoundException();
        return f.length();
    }

    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        File f = file.toFile(this.root);
        if (!f.exists() || f.isDirectory())
            throw new FileNotFoundException();

        if(offset < 0 || length < 0 || offset + length > f.length())
            throw new IndexOutOfBoundsException();

        byte[] result = new byte[length];
        RandomAccessFile fileReader = new RandomAccessFile(f, "r");
        fileReader.seek(offset);
        fileReader.read(result, 0, length);
        fileReader.close();

        return result;
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        if (offset < 0)
            throw new IndexOutOfBoundsException();

        File f = file.toFile(this.root);

        if (!f.exists() || f.isDirectory())
            throw new FileNotFoundException();

        RandomAccessFile fileWriter = new RandomAccessFile(f,"rw");
        fileWriter.seek(offset);
        fileWriter.write(data);
        fileWriter.close();
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
        if (file.isRoot())
            return false;

        Path parent = file.parent();
        if (!parent.toFile(this.root).exists())
            parent.toFile(this.root).mkdirs();

        File f = file.toFile(this.root);
        // true if the named file does not exist and was successfully created; false if the named file already exists
        boolean flag = false;
        try {
            flag = f.createNewFile();
		} catch (IOException e) {

		}

        return flag;
    }

    @Override
    public synchronized boolean delete(Path path)
    {
        if (path.isRoot())
            return false;
        return this.delete(path.toFile(this.root));
    }

    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
        int size = (int)server.size(file);
        this.delete(file.toFile(this.root));
        this.create(file);
        byte[] data = server.read(file, 0, size);
        this.write(file, 0, data);
        return true;
    }



/***************************help functions********************************/

    private boolean delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                boolean flag = delete(c);
                if(!flag) return false;
            }
        }
        if (!f.delete())
            return false;
        return true;
    }

    private boolean pruneDirectoriesWithNoFile(File root) {
        boolean isEmpty = true;

        for (File f : root.listFiles()){
            if (f.isDirectory())
                isEmpty = isEmpty && pruneDirectoriesWithNoFile(f);
            else
                isEmpty = false;
        }

        if (isEmpty)
            root.delete();

        return isEmpty;
    }

}
