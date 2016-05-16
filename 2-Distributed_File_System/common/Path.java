package common;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Distributed filesystem paths.

    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Comparable<Path>, Serializable
{

    /* /var/log/readme */
    private CopyOnWriteArrayList<String> pathComponents;

    /** Creates a new path which represents the root directory. */
    public Path()
    {
        this.pathComponents = new CopyOnWriteArrayList<String>();
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
        this(path.toString());
    	if (component.contains(":") || component.contains("/") || component.length() == 0)
    		throw new IllegalArgumentException();
    	this.pathComponents.add(component);
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
        if (!path.startsWith("/") || path.contains(":"))
    		throw new IllegalArgumentException();
    	this.pathComponents = new CopyOnWriteArrayList<String>();
        String[] components = path.split("/");
        for (String component : components) {
            if(component.length() == 0) continue;
            this.pathComponents.add(component);
        }
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        return this.pathComponents.iterator();
    }

    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
        if(!directory.exists())
        	throw new FileNotFoundException();
        if (!directory.isDirectory())
            throw new IllegalArgumentException();

        ArrayList<Path> result = new ArrayList<Path>();
        for (File file : directory.listFiles()){
            if (file.isDirectory()){
                Path[] subdirResult = Path.list(file);
                for (Path path : subdirResult)
                    result.add(new Path("/" + file.getName() + path.toString()));
            } else {
                result.add(new Path("/" + file.getName()));
            }
        }

        Path[] resultArray = new Path[result.size()];
        result.toArray(resultArray);
        return resultArray;
    }

    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
        return this.pathComponents.isEmpty();
    }

    /** Returns the path to the parent of this path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent()
    {
        if (this.isRoot())
    		throw new IllegalArgumentException();
        int size = this.pathComponents.size();
        String temp = this.pathComponents.remove(size-1);
    	String parentPath = this.toString();
    	this.pathComponents.add(temp);
    	return new Path(parentPath);
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
        if (this.isRoot())
    		throw new IllegalArgumentException();
    	return this.pathComponents.get(this.pathComponents.size()-1);
    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if it is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
        String path1 = this.toString();
        String path2 = other.toString();
        return path1.startsWith(path2);
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
        return new File(root, this.toString());
    }

    /** Compares this path to another.

        <p>
        An ordering upon <code>Path</code> objects is provided to prevent
        deadlocks between applications that need to lock multiple filesystem
        objects simultaneously. By convention, paths that need to be locked
        simultaneously are locked in increasing order.

        <p>
        Because locking a path requires locking every component along the path,
        the order is not arbitrary. For example, suppose the paths were ordered
        first by length, so that <code>/etc</code> precedes
        <code>/bin/cat</code>, which precedes <code>/etc/dfs/conf.txt</code>.

        <p>
        Now, suppose two users are running two applications, such as two
        instances of <code>cp</code>. One needs to work with <code>/etc</code>
        and <code>/bin/cat</code>, and the other with <code>/bin/cat</code> and
        <code>/etc/dfs/conf.txt</code>.

        <p>
        Then, if both applications follow the convention and lock paths in
        increasing order, the following situation can occur: the first
        application locks <code>/etc</code>. The second application locks
        <code>/bin/cat</code>. The first application tries to lock
        <code>/bin/cat</code> also, but gets blocked because the second
        application holds the lock. Now, the second application tries to lock
        <code>/etc/dfs/conf.txt</code>, and also gets blocked, because it would
        need to acquire the lock for <code>/etc</code> to do so. The two
        applications are now deadlocked.

        @param other The other path.
        @return Zero if the two paths are equal, a negative number if this path
                precedes the other path, or a positive number if this path
                follows the other path.
     */
    @Override
    public int compareTo(Path other)
    {
        String path1 = this.toString();
        String path2 = other.toString();
        if(path1.equals(path2)) return 0;
        if(this.isSubpath(other)) return 1;
        if(other.isSubpath(this)) return -1;
        return path1.compareTo(path2);
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
        return this.toString().equals(other.toString());
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
        if (this.pathComponents.isEmpty())
    		return "/";

    	String path = "";
    	for(String component : this.pathComponents) {
    		path += "/";
    		path += component;
    	}
    	return path;
    }

    /***************helper functions******************/
    public Path[] subPaths() {
        Path[] paths = new Path[pathComponents.size() + 1];
        paths[0] = new Path();
        String path = "";
        int i = 1;
        for(String component : this.pathComponents) {
            path += "/";
            path += component;
            paths[i++] = new Path(path);
        }
        return paths;
    }

}
