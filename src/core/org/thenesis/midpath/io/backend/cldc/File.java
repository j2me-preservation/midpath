/* File.java -- Class representing a file on disk
 Copyright (C) 1998, 1999, 2001 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.
 
 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package org.thenesis.midpath.io.backend.cldc;

import java.io.IOException;

import com.sun.cldchi.jvm.JVM;

/*http://cvs.savannah.gnu.org/viewvc/classpath/classpath/java/io/?hideattic=0&pathrev=classpath-0_05-release*/

/**
 * This class represents a file or directory on a local disk.  It provides
 * facilities for dealing with a variety of systems that use various
 * types of path separators ("/" versus "\", for example).  It also
 * contains method useful for creating and deleting files and directories.
 *
 * @version 0.0
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 */
public class File {

	/*************************************************************************/

	/*
	 * Class Variables
	 */

	/**
	 * This is the path separator string for the current host. This field
	 * contains the value of the <code>file.separator</code> system property.
	 * An example separator string would be "/" on the GNU system.
	 */
	public final static String separator = PlatformHelper.getSeparator();

	/**
	 * This is the first character of the file separator string.  On many
	 * hosts (for example, on the GNU system), this represents the entire 
	 * separator string.  The complete separator string is obtained from the
	 * <code>file.separator</code>system property.
	 */
	public final static char separatorChar = PlatformHelper.getSeparatorChar();

	/**
	 * This is the string that is used to separate the host name from the
	 * path name in paths than include the host name.  It is the value of
	 * the <code>path.separator</code> system property.
	 */
	public final static String pathSeparator = PlatformHelper.getPathSeparator();

	/**
	 * This is the first character of the string used to separate the host name
	 * from the path name in paths that include a host.  The separator string
	 * is taken from the <code>path.separator</code> system property.
	 */
	public static char pathSeparatorChar = PlatformHelper.getPathSeparatorChar();

	static {
		
		JVM.loadLibrary("libmidpathfile.so");
		// System.loadLibrary ("midpathfile"); 
		
	}

	/*************************************************************************/

	/*
	 * Instance Variables
	 */

	/**
	 * This is the path to the file set when the object is created.  It
	 * may be an absolute or relative path name.
	 */
	private String path;

	/*************************************************************************/

	/*
	 * Class Methods
	 */

	/**
	 * This method creates a temporary file in the system temporary directory. The
	 * files created are guaranteed not to currently exist and the same file name
	 * will never be used twice in the same virtual machine instance.  The
	 * system temporary directory is determined by examinging the 
	 * <code>java.io.tmpdir</code> system property.
	 * <p>
	 * The <code>prefix</code> parameter is a sequence of at least three
	 * characters that are used as the start of the generated filename.  The
	 * <code>suffix</code> parameter is a sequence of characters that is used
	 * to terminate the file name.  This parameter may be <code>null</code>
	 * and if it is, the suffix defaults to ".tmp".
	 * <p>
	 * If a <code>SecurityManager</code> exists, then its <code>checkWrite</code>
	 * method is used to verify that this operation is permitted.
	 * <p>
	 * This method is identical to calling 
	 * <code>createTempFile(prefix, suffix, null)</code>.
	 *
	 * @param prefix The character prefix to use in generating the path name.
	 * @param suffix The character suffix to use in generating the path name.
	 *
	 * @exception IllegalArgumentException If the prefix or suffix are not valid.
	 * @exception SecurityException If there is no permission to perform this operation
	 * @exception IOException If an error occurs
	 */
	public static File createTempFile(String prefix, String suffix) throws IllegalArgumentException, IOException {
		return (createTempFile(prefix, suffix, null));
	}

	/*************************************************************************/

	/**
	 * This method creates a temporary file in the specified directory.  If 
	 * the directory name is null, then this method uses the system temporary 
	 * directory. The files created are guaranteed not to currently exist and the 
	 * same file name will never be used twice in the same virtual machine instance.  
	 * The system temporary directory is determined by examinging the 
	 * <code>java.io.tmpdir</code> system property.
	 * <p>
	 * The <code>prefix</code> parameter is a sequence of at least three
	 * characters that are used as the start of the generated filename.  The
	 * <code>suffix</code> parameter is a sequence of characters that is used
	 * to terminate the file name.  This parameter may be <code>null</code>
	 * and if it is, the suffix defaults to ".tmp".
	 * <p>
	 * If a <code>SecurityManager</code> exists, then its <code>checkWrite</code>
	 * method is used to verify that this operation is permitted.
	 *
	 * @param prefix The character prefix to use in generating the path name.
	 * @param suffix The character suffix to use in generating the path name.
	 * @param directory The directory to create the file in, or <code>null</code> for the default temporary directory
	 *
	 * @exception IllegalArgumentException If the patterns is not valid
	 * @exception SecurityException If there is no permission to perform this operation
	 * @exception IOException If an error occurs
	 */
	public static synchronized File createTempFile(String prefix, String suffix, File directory)
			throws IllegalArgumentException, IOException {
		// Grab the system temp directory if necessary
		if (directory == null) {
			String dirname = System.getProperty("java.io.tmpdir");
			if (dirname == null)
				throw new IOException("Cannot determine system temporary directory");

			directory = new File(dirname);
			if (!directory.exists())
				throw new IOException("System temporary directory " + directory.getName() + " does not exist.");
			if (!directory.isDirectory())
				throw new IOException("System temporary directory " + directory.getName()
						+ " is not really a directory.");
		}

		// Now process the prefix and suffix.
		if (prefix.length() < 3)
			throw new IllegalArgumentException("Prefix too short: " + prefix);

		if (suffix == null)
			suffix = ".tmp";

		// Now identify a file name and make sure it doesn't exist
		File f;
		for (;;) {
			String filename = prefix + System.currentTimeMillis() + suffix;
			f = new File(directory, filename);

			if (f.exists())
				continue;
			else
				break;
		}

		// Now create the file and return our file object
		createInternal(f.getAbsolutePath());
		return (f);
	}

	/*************************************************************************/

	/**
	 * This method is used to create a temporary file
	 */
	private static native boolean createInternal(String name) throws IOException;

	/*************************************************************************/

	/**
	 * This method returns an array of filesystem roots.  Some operating systems
	 * have volume oriented filesystem.  This method provides a mechanism for
	 * determining which volumes exist.  GNU systems use a single hierarchical
	 * filesystem, so will have only one "/" filesystem root.
	 *
	 * @return An array of <code>File</code> objects for each filesystem root
	 * available.
	 */
	public static File[] listRoots() {
		File[] f = new File[1];
		f[0] = new File("/");

		return (f);
	}

	/*************************************************************************/

	/*
	 * Constructors
	 */

	/**
	 * This method initializes a new <code>File</code> object to represent
	 * a file in the specified directory.  If the <code>directory</code>
	 * argument is <code>null</code>, the file is assumed to be in the
	 * current directory as specified by the <code>user.dir</code> system
	 * property
	 *
	 * @param directory The directory this file resides in
	 * @param name The name of the file
	 */
	public File(File directory, String name) {
		
		if (directory == null) {
			String dirname = System.getProperty("user.dir");
			if (dirname == null)
				throw new IllegalArgumentException("Cannot determine default user directory");
			directory = new File(dirname);
		}

		String dirpath = directory.getPath();
		if (PlatformHelper.isRootDirectory(dirpath))
			path = dirpath + name;
		else
			path = dirpath + separator + name;
	}

	/*************************************************************************/

	/**
	 * This method initializes a new <code>File</code> object to represent
	 * a file in the specified named directory.  The path name to the file
	 * will be the directory name plus the separator string plus the file
	 * name.  If the directory path name ends in the separator string, another
	 * separator string will still be appended.
	 *
	 * @param dirname The path to the directory the file resides in
	 * @param name The name of the file
	 */
	public File(String dirname, String name) {
		this(dirname == null ? (File) null : new File(dirname), name);
	}

	/*************************************************************************/

	/**
	 * This method initializes a new <code>File</code> object to represent
	 * a file with the specified path.
	 *
	 * @param name The path name of the file
	 */
	public File(String name) {
		path = name;

		// Per the spec
		if (path == null)
			throw new NullPointerException("File name is null");

		while (!PlatformHelper.isRootDirectory(path) && PlatformHelper.endWithSeparator(path))
			path = PlatformHelper.removeTailSeparator(path);
	}

	/*************************************************************************/

	/*
	 * Instance Methods
	 */

	/**
	 * This method returns the name of the file.  This is everything in the
	 * complete path of the file after the last instance of the separator
	 * string.
	 *
	 * @return The file name
	 */
	public String getName() {
		int pos = PlatformHelper.lastIndexOfSeparator(path);
		if (pos == -1)
			return (path);

		if (PlatformHelper.endWithSeparator(path))
			return ("");

		return (path.substring(pos + separator.length()));
	}

	/*************************************************************************/

	/**
	 * Returns the path name that represents this file.  May be a relative
	 * or an absolute path name
	 *
	 * @return The pathname of this file
	 */
	public String getPath() {
		return (path);
	}

	/*************************************************************************/

	/**
	 * This method returns the path of this file as an absolute path name.
	 * If the path name is already absolute, then it is returned.  Otherwise
	 * the value returned is the current directory plus the separatory
	 * string plus the path of the file.  The current directory is determined
	 * from the <code>user.dir</code> system property.
	 *
	 * @return The absolute path of this file
	 */
	public String getAbsolutePath() {
		if (isAbsolute())
			return path;

		String dir = System.getProperty("user.dir");
		if (dir == null)
			return path;

		if (PlatformHelper.endWithSeparator(dir))
			return dir + path;

		return dir + separator + path;
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>File</code> object representing the
	 * absolute path of this object.
	 *
	 * @return A <code>File</code> with the absolute path of the object.
	 */
	public File getAbsoluteFile() {
		return (new File(getAbsolutePath()));
	}

	/*************************************************************************/

	/**
	 * This method returns a canonical representation of the pathname of
	 * this file.  The actual form of the canonical representation is
	 * different.  On the GNU system, the canonical form differs from the
	 * absolute form in that all relative file references to "." and ".."
	 * are resolved and removed.
	 * <p>
	 * Note that this method, unlike the other methods which return path
	 * names, can throw an IOException.  This is because native method 
	 * might be required in order to resolve the canonical path
	 *
	 * @exception IOException If an error occurs
	 */
	public String getCanonicalPath() throws IOException {
		String abspath = getAbsolutePath();
		return PlatformHelper.toCanonicalForm(abspath);
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>File</code> object representing the
	 * canonical path of this object.
	 *
	 * @return A <code>File</code> instance representing the canonical path of
	 * this object.
	 *
	 * @exception IOException If an error occurs.
	 */
	public File getCanonicalFile() throws IOException {
		return (new File(getCanonicalPath()));
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>String</code> the represents this file's
	 * parent.  <code>null</code> is returned if the file has no parent.  The
	 * parent is determined via a simple operation which removes the
	 *
	 * @return The parent directory of this file
	 */
	public String getParent() {
		if (PlatformHelper.isRootDirectory(path))
			return null;

		String par_path = path;

		int pos = PlatformHelper.lastIndexOfSeparator(par_path);
		if (pos == -1)
			return null;

		return (par_path.substring(0, pos));
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>File</code> object representing the parent
	 * file of this one.
	 *
	 * @param A <code>File</code> for the parent of this object.  <code>null</code>
	 * will be returned if this object does not have a parent.
	 */
	public File getParentFile() {
		String parent = getParent();
		if (parent == null)
			return (null);

		return (new File(parent));
	}

	/*************************************************************************/

	/**
	 * This method returns true if this object represents an absolute file
	 * path and false if it does not.  The definition of an absolute path varies
	 * by system.  As an example, on GNU systems, a path is absolute if it starts
	 * with a "/".
	 *
	 * @return <code>true</code> if this object represents an absolute file name, <code>false</code> otherwise.
	 */
	public boolean isAbsolute() {
		if (PlatformHelper.beginWithRootPathPrefix(path) > 0)
			return (true);
		else
			return (false);
	}

	/*************************************************************************/

	/**
	 * This method tests whether or not the current thread is allowed to
	 * to read the file pointed to by this object.  This will be true if and
	 * and only if 1) the file exists and 2) the <code>SecurityManager</code>
	 * (if any) allows access to the file via it's <code>checkRead</code>
	 * method 3) the file is readable.
	 *
	 * @return <code>true</code> if reading is allowed, <code>false</code> otherwise
	 *
	 * @exception SecurityException If the <code>SecurityManager</code> does not allow access to the file
	 */
	public boolean canRead() throws SecurityException {
		// Test for existence. This also does the SecurityManager check
		if (!exists())
			return (false);

		return (canReadInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method checks file permissions for reading
	 */
	private synchronized native boolean canReadInternal(String path);

	/*************************************************************************/

	/**
	 * This method test whether or not the current thread is allowed to
	 * write to this object.  This will be true if and only if 1) The
	 * <code>SecurityManager</code> (if any) allows write access to the
	 * file and 2) The file exists and 3) The file is writable.  To determine
	 * whether or not a non-existent file can be created, check the parent
	 * directory for write access.
	 *
	 * @return <code>true</code> if writing is allowed, <code>false</code> otherwise
	 *
	 * @exception SecurityException If the <code>SecurityManager</code> does not allow access to the file
	 */
	public boolean canWrite() throws SecurityException {

		// Test for existence.  This is required by the spec
		if (!exists())
			return (false);

		if (!isDirectory())
			return (canWriteInternal(path));
		else
			try {
				File test = createTempFile("test-dir-write", null, this);
				return (test != null && test.delete());
			} catch (IOException ioe) {
				return (false);
			}
	}

	/*************************************************************************/

	/**
	 * This native method checks file permissions for writing
	 */
	private synchronized native boolean canWriteInternal(String path);

	/*************************************************************************/

	/**
	 * This method sets the file represented by this object to be read only.
	 * A read only file or directory cannot be modified.  Please note that 
	 * GNU systems allow read only files to be deleted if the directory it
	 * is contained in is writable.
	 *
	 * @return <code>true</code> if the operation succeeded, <code>false</code>
	 * otherwise.
	 *
	 * @exception SecurityException If the <code>SecurityManager</code> does
	 * not allow this operation.
	 */
	public boolean setReadOnly() throws SecurityException {
		// Test for existence.
		if (!exists())
			return (false);

		return (setReadOnlyInternal(path));
	}

	/*************************************************************************/

	/*
	 * This native method sets the permissions to make the file read only.
	 */
	private native boolean setReadOnlyInternal(String path);

	/*************************************************************************/

	/**
	 * This method tests whether or not the file represented by the object
	 * actually exists on the filesystem.
	 *
	 * @return <code>true</code> if the file exists, <code>false</code>otherwise.
	 *
	 * @exception SecurityException If reading of the file is not permitted
	 */
	public boolean exists() throws SecurityException {

		return (existsInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method does the actual checking of file existence.
	 */
	private native boolean existsInternal(String path);

	/*************************************************************************/

	/**
	 * This method tests whether or not the file represented by this object
	 * is a "plain" file.  A file is a plain file if and only if it 1) Exists,
	 * 2) Is not a directory or other type of special file.
	 *
	 * @return <code>true</code> if this is a plain file, <code>false</code> otherwise
	 *
	 * @exception SecurityException If reading of the file is not permitted
	 */
	public boolean isFile() throws SecurityException {

		return (isFileInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method does the actual check of whether or not a file
	 * is a plain file or not.  It also handles the existence check to
	 * eliminate the overhead of a call to exists()
	 */
	private native boolean isFileInternal(String path);

	/*************************************************************************/

	/**
	 * This method tests whether or not the file represented by this object
	 * is a directory.  In order for this method to return <code>true</code>,
	 * the file represented by this object must exist and be a directory.
	 * 
	 * @return <code>true</code> if this file is a directory, <code>false</code> otherwise
	 *
	 * @exception SecurityException If reading of the file is not permitted
	 */
	public boolean isDirectory() throws SecurityException {

		return (isDirectoryInternal(path));
	}

	/*************************************************************************/

	/**
	 * This method does the actual check of whether or not a file is a
	 * directory or not.  It also handle the existence check to eliminate
	 * the overhead of a call to exists()
	 */
	private native boolean isDirectoryInternal(String path);

	/*************************************************************************/

	/**
	 * This method tests whether or not this file represents a "hidden" file.
	 * On GNU systems, a file is hidden if its name begins with a "."
	 * character.  Files with these names are traditionally not shown with
	 * directory listing tools.
	 *
	 * @return <code>true</code> if the file is hidden, <code>false</code>
	 * otherwise.
	 */
	public boolean isHidden() {
		if (getName().startsWith("."))
			return (true);
		else
			return (false);
	}

	/*************************************************************************/

	/**
	 * This method returns the length of the file represented by this object,
	 * or 0 if the specified file does not exist.
	 *
	 * @return The length of the file
	 *
	 * @exception SecurityException If reading of the file is not permitted
	 */
	public long length() throws SecurityException {

		return (lengthInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method actually determines the length of the file and
	 * handles the existence check
	 */
	private native long lengthInternal(String path);

	/*************************************************************************/

	/**
	 * This method returns the last modification time of this file.  The
	 * time value returned is an abstract value that should not be interpreted
	 * as a specified time value.  It is only useful for comparing to other
	 * such time values returned on the same system.  In that case, the larger
	 * value indicates a more recent modification time. 
	 * <p>
	 * If the file does not exist, then a value of 0 is returned.
	 *
	 * @return The last modification time of the file
	 *
	 * @exception SecurityException If reading of the file is not permitted
	 */
	public long lastModified() throws SecurityException {

		return (lastModifiedInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method does the actual work of getting the last file
	 * modification time.  It also does the existence check to avoid the
	 * overhead of a call to exists()
	 */
	private native long lastModifiedInternal(String path);

	/*************************************************************************/

	/**
	 * This method sets the modification time on the file to the specified
	 * value.  This is specified as the number of seconds since midnight
	 * on January 1, 1970 GMT.
	 *
	 * @param time The desired modification time.
	 *
	 * @return <code>true</code> if the operation succeeded, <code>false</code>
	 * otherwise.
	 *
	 * @exception IllegalArgumentException If the specified time is negative.
	 * @exception SecurityException If the <code>SecurityManager</code> will
	 * not allow this operation.
	 */
	public boolean setLastModified(long time) throws IllegalArgumentException, SecurityException {
		if (time < 0)
			throw new IllegalArgumentException("Negative modification time: " + time);

		return (setLastModifiedInternal(path, time));
	}

	/*************************************************************************/

	/*
	 * This method does the actual setting of the modification time.
	 */
	private native boolean setLastModifiedInternal(String path, long time);

	/*************************************************************************/

	/**
	 * This method creates a new file of zero length with the same name as
	 * the path of this <code>File</code> object if an only if that file
	 * does not already exist.
	 * <p>
	 * A <code>SecurityManager</code>checkWrite</code> check is done prior
	 * to performing this action.
	 *
	 * @return <code>true</code> if the file was created, <code>false</code> if
	 * the file alread existed.
	 *
	 * @exception IOException If an I/O error occurs
	 * @exception SecurityException If the <code>SecurityManager</code> will
	 * not allow this operation to be performed.
	 */
	public boolean createNewFile() throws IOException, SecurityException {

		return (createInternal(getPath()));
	}

	/*************************************************************************/

	/**
	 * This method deletes the file represented by this object.  If this file
	 * is a directory, it must be empty in order for the delete to succeed.
	 *
	 * @return <code>true</code> if the file was deleted, <code>false</code> otherwise
	 *
	 * @exception SecurityException If deleting of the file is not allowed
	 */
	public synchronized boolean delete() throws SecurityException {
		return (deleteInternal(path));
	}

	/*************************************************************************/

	/**
	 * This native method handles the actual deleting of the file
	 */
	private native boolean deleteInternal(String path);

	/*************************************************************************/

	/**
	 * Calling this method requests that the file represented by this object
	 * be deleted when the virtual machine exits.  Note that this request cannot
	 * be cancelled.  Also, it will only be carried out if the virtual machine
	 * exits normally.
	 *
	 * @exception SecurityException If deleting of the file is not allowed
	 */
	public void deleteOnExit() throws SecurityException {

		// Sounds like we need to do some VM specific stuff here. We could delete
		// the file in finalize() and set FinalizeOnExit to true, but delete on
		// finalize != delete on exit and we should not be setting up system
		// parameters without the user's knowledge.
		//********IMPLEMENT ME!!!!!!***************
		return;
	}

	/*************************************************************************/

	/**
	 * This method creates a directory for the path represented by this object.
	 *
	 * @return <code>true</code> if the directory was created, <code>false</code> otherwise
	 *
	 * @exception SecurityException If write access is not allowed to this file
	 */
	public boolean mkdir() throws SecurityException {
	
		String mk_path;
		mk_path = PlatformHelper.removeTailSeparator(path);

		return (mkdirInternal(mk_path));
	}

	/*************************************************************************/

	/**
	 * This native method actually creates the directory
	 */
	private native boolean mkdirInternal(String path);

	/*************************************************************************/

	/**
	 * This method creates a directory for the path represented by this file.
	 * It will also create any intervening parent directories if necessary.
	 *
	 * @return <code>true</code> if the directory was created, <code>false</code> otherwise
	 *
	 * @exception SecurityException If write access is not allowed to this file
	 */
	public boolean mkdirs() throws SecurityException {
		String parent = getParent();
		if (parent == null) {
			return (mkdir());
		}

		File f = new File(parent);
		if (!f.exists()) {
			boolean rc = f.mkdirs();
			if (rc == false)
				return (false);
		}

		return (mkdir());
	}

	/*************************************************************************/

	/**
	 * This method renames the file represented by this object to the path
	 * of the file represented by the argument <code>File</code>.
	 *
	 * @param dest The <code>File</code> object representing the target name
	 *
	 * @return <code>true</code> if the rename succeeds, <code>false</code> otherwise.
	 *
	 * @exception SecurityException If write access is not allowed to the file by the <code>SecurityMananger</code>.
	 */
	public synchronized boolean renameTo(File dest) throws SecurityException {
		
		// Call our native rename method
		boolean rc = renameToInternal(path, dest.getPath());

		return (rc);
	}

	/*************************************************************************/

	/**
	 * This native method actually performs the rename.
	 */
	private native boolean renameToInternal(String target, String dest);

	/*************************************************************************/

	/**
	 * This method returns a array of <code>String</code>'s representing the
	 * list of files is then directory represented by this object.  If this
	 * object represents a non-directory file or a non-existent file, then
	 * <code>null</code> is returned.  The list of files will not contain
	 * any names such as "." or ".." which indicate the current or parent
	 * directory.  Also, the names are not guaranteed to be sorted.
	 * <p>
	 * A <code>SecurityManager</code> check is made prior to reading the
	 * directory.  If read access to the directory is denied, an exception
	 * will be thrown.
	 *
	 * @return An array of files in the directory, or <code>null</code> if this object does not represent a valid directory.
	 * 
	 * @exception SecurityException If read access is not allowed to the directory by the <code>SecurityManager</code>
	 */
	public String[] list() {
		return (list(null));
	}

	/*************************************************************************/

	/**
	 * This method returns a array of <code>String</code>'s representing the
	 * list of files is then directory represented by this object.  If this
	 * object represents a non-directory file or a non-existent file, then
	 * <code>null</code> is returned.  The list of files will not contain
	 * any names such as "." or ".." which indicate the current or parent
	 * directory.  Also, the names are not guaranteed to be sorted.
	 * <p>
	 * In this form of the <code>list()</code> method, a filter is specified
	 * that allows the caller to control which files are returned in the
	 * list.  The <code>FilenameFilter</code> specified is called for each
	 * file returned to determine whether or not that file should be included
	 * in the list.
	 * <p>
	 * A <code>SecurityManager</code> check is made prior to reading the
	 * directory.  If read access to the directory is denied, an exception
	 * will be thrown.
	 *
	 * @param filter An object which will identify files to exclude from the directory listing.
	 *
	 * @return An array of files in the directory, or <code>null</code> if this object does not represent a valid directory.
	 * 
	 * @exception SecurityException If read access is not allowed to the directory by the <code>SecurityManager</code>
	 */
	public String[] list(FilenameFilter filter) {

		// Get the list of files
		String list_path = PlatformHelper.removeTailSeparator(path);
		File dir = new File(list_path);

		if (!dir.exists() || !dir.isDirectory())
			return null;

		String files[] = listInternal(list_path);

		if (files == null)
			return new String[0];
		if (filter == null)
			return (files);

		// Apply the filter
		int count = 0;
		for (int i = 0; i < files.length; i++) {
			if (filter.accept(this, files[i]))
				++count;
			else
				files[i] = null;
		}

		String[] retfiles = new String[count];
		count = 0;
		for (int i = 0; i < files.length; i++)
			if (files[i] != null)
				retfiles[count++] = files[i];

		return (retfiles);
	}

	/*************************************************************************/

	/**
	 * This native function actually produces the list of file in this
	 * directory
	 */
	private native String[] listInternal(String dirname);

	/*************************************************************************/

	/**
	 * This method returns an array of <code>File</code> objects representing
	 * all the files in the directory represented by this object. If this
	 * object does not represent a directory, <code>null</code> is returned.
	 * Each of the returned <code>File</code> object is constructed with this
	 * object as its parent.
	 * <p>
	 * A <code>SecurityManager</code> check is made prior to reading the
	 * directory.  If read access to the directory is denied, an exception
	 * will be thrown.
	 *
	 * @return An array of <code>File</code> objects for this directory.
	 *
	 * @exception SecurityException If the <code>SecurityManager</code> denies
	 * access to this directory.
	 */
	public File[] listFiles() {
		return (listFiles((FilenameFilter) null));
	}

	/*************************************************************************/

	/**
	 * This method returns an array of <code>File</code> objects representing
	 * all the files in the directory represented by this object. If this
	 * object does not represent a directory, <code>null</code> is returned.
	 * Each of the returned <code>File</code> object is constructed with this
	 * object as its parent.
	 * <p> 
	 * In this form of the <code>listFiles()</code> method, a filter is specified
	 * that allows the caller to control which files are returned in the
	 * list.  The <code>FilenameFilter</code> specified is called for each
	 * file returned to determine whether or not that file should be included
	 * in the list.
	 * <p>
	 * A <code>SecurityManager</code> check is made prior to reading the
	 * directory.  If read access to the directory is denied, an exception
	 * will be thrown.
	 *
	 * @return An array of <code>File</code> objects for this directory.
	 *
	 * @exception SecurityException If the <code>SecurityManager</code> denies
	 * access to this directory.
	 */
	public File[] listFiles(FilenameFilter filter) {
		String[] filelist = list(filter);
		if (filelist == null)
			return (null);

		File[] fobjlist = new File[filelist.length];

		for (int i = 0; i < filelist.length; i++)
			fobjlist[i] = new File(this, filelist[i]);

		return (fobjlist);
	}

	/*************************************************************************/

//	/**
//	 * This method returns an array of <code>File</code> objects representing
//	 * all the files in the directory represented by this object. If this
//	 * object does not represent a directory, <code>null</code> is returned.
//	 * Each of the returned <code>File</code> object is constructed with this
//	 * object as its parent.
//	 * <p> 
//	 * In this form of the <code>listFiles()</code> method, a filter is specified
//	 * that allows the caller to control which files are returned in the
//	 * list.  The <code>FileFilter</code> specified is called for each
//	 * file returned to determine whether or not that file should be included
//	 * in the list.
//	 * <p>
//	 * A <code>SecurityManager</code> check is made prior to reading the
//	 * directory.  If read access to the directory is denied, an exception
//	 * will be thrown.
//	 *
//	 * @return An array of <code>File</code> objects for this directory.
//	 *
//	 * @exception SecurityException If the <code>SecurityManager</code> denies
//	 * access to this directory.
//	 */
//	public File[] listFiles(FileFilter filter) {
//		File[] fobjlist = listFiles((FilenameFilter) null);
//
//		if (fobjlist == null)
//			return (null);
//
//		if (filter == null)
//			return (fobjlist);
//
//		int count = 0;
//		for (int i = 0; i < fobjlist.length; i++)
//			if (filter.accept(fobjlist[i]) == true)
//				++count;
//
//		File[] final_list = new File[count];
//		count = 0;
//		for (int i = 0; i < fobjlist.length; i++)
//			if (filter.accept(fobjlist[i]) == true) {
//				final_list[count] = fobjlist[i];
//				++count;
//			}
//
//		return (final_list);
//	}

	/*************************************************************************/

	/**
	 * This method compares the specified <code>Object</code> to this one
	 * to test for equality.  It does this by comparing the canonical path names
	 * of the files.  This method is identical to <code>compareTo(File)</code>
	 * except that if the <code>Object</code> passed to it is not a 
	 * <code>File</code>, it throws a <code>ClassCastException</code>
	 * <p>
	 * The canonical paths of the files are determined by calling the
	 * <code>getCanonicalPath</code> method on each object.
	 * <p>
	 * This method returns a 0 if the specified <code>Object</code> is equal
	 * to this one, a negative value if it is less than this one 
	 * a positive value if it is greater than this one.
	 *
	 * @return An integer as described above
	 *
	 * @exception ClassCastException If the passed <code>Object</code> is not a <code>File</code>
	 */
	public int compareTo(Object obj) throws ClassCastException {
		return (compareTo((File) obj));
	}

	/*************************************************************************/

	/**
	 * This method compares the specified <code>File</code> to this one
	 * to test for equality.  It does this by comparing the canonical path names
	 * of the files. 
	 * <p>
	 * The canonical paths of the files are determined by calling the
	 * <code>getCanonicalPath</code> method on each object.
	 * <p>
	 * This method returns a 0 if the specified <code>Object</code> is equal
	 * to this one, a negative value if it is less than this one 
	 * a positive value if it is greater than this one.
	 *
	 * @return An integer as described above
	 */
	public int compareTo(File file) {
		String p1, p2;
		try {
			p1 = getCanonicalPath();
			p2 = file.getCanonicalPath();
		} catch (IOException e) {
			// What do we do here?  The spec requires the canonical path.  Even
			// if we don't call the method, we must replicate the functionality
			// which per the spec can fail.  What happens in that situation?
			// I just assume the files are equal!
			//
			return (0);
		}

		return (p1.compareTo(p2));
	}

	/*************************************************************************/

	/**
	 * This method tests two <code>File</code> objects for equality by 
	 * comparing the path of the specified <code>File</code> against the path
	 * of this object.  The two objects are equal if an only if 1) The
	 * argument is not null 2) The argument is a <code>File</code> object and
	 * 3) The path of the <code>File</code>argument is equal to the path
	 * of this object.
	 * <p>
	 * The paths of the files are determined by calling the <code>getPath()</code>
	 * method on each object.
	 *
	 * @return <code>true</code> if the two objects are equal, <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return (false);

		if (!(obj instanceof File))
			return (false);

		File f = (File) obj;

		return (f.getPath().equals(getPath()));
	}

	/*************************************************************************/

	/**
	 * This method returns a hash code representing this file.  It is the
	 * hash code of the path of this file (as returned by <code>getPath()</code>)
	 * exclusived or-ed with the value 1234321.
	 *
	 * @return The hash code for this object
	 */
	public int hashCode() {
		return (getPath().hashCode() ^ 1234321);
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>String</code> that is the path name of the
	 * file as returned by <code>getPath</code>.
	 *
	 * @return A <code>String</code> representation of this file
	 */
	public String toString() {
		return (path);
	}

	/*************************************************************************/

	/**
	 * This method returns a <code>URL</code> with the <code>file:</code>
	 * protocol that represents this file.  The exact form of this URL is
	 * system dependent.
	 *
	 * @return A <code>URL</code> for this object.
	 *
	 * @exception MalformedURLException If the URL cannot be created successfully.
	 */
	public String toURL() {
		String abspath = getAbsolutePath();
		try {
			if (new File(abspath).isDirectory())
				abspath = abspath + separator;
		} catch (Exception _) {
		}

		String url_string = "file://" + abspath;

		return url_string;
	}

} // class File

