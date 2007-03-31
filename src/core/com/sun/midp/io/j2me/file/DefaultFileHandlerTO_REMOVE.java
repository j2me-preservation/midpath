/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

package  com.sun.midp.io.j2me.file;

import java.io.IOException;
import java.util.Vector;

import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midlet.MIDletSuite;


/**
 * Default file handler.
 */
class DefaultFileHandlerTO_REMOVE implements BaseFileHandler {

    /*
     * Placeholder for the file handle from the native layer.
     * Value of <code>0</code> indicates that file is either closed or
     * does not exists
     */
    /** Read handle. */
    private int readHandle  = 0;
    /** Write handle. */
    private int writeHandle = 0;

    /** File name. */
    private long fileName   = 0;
    // The dir name is required when checking the fileSystem sizes
    /** Root directory. */
    private long rootDir    = 0;

    /** Illegal file name characters. */
    private static String illegalChars = null;

    /**
     * The flag is used for performance optimization.
     * <code>true</code> stands for it was detected
     * what private directory exists.
     */
    private static boolean privateDirExists = false;

    /**
     * Init class info in the native code
     */
    static {
        initialize();

        illegalChars = illegalFileNameChars0();
        if (illegalChars == null) {
            illegalChars = "";
        }
    }

    // JAVADOC COMMENT ELIDED
    public void connect(String rootName, String fileName) {
        String rootPath = getRootPath(rootName);
        if (rootPath != null) {
            StringBuffer name = new StringBuffer(rootPath);
            int curr = name.length();
            this.rootDir = getNativeName(name.toString());

            //  we add '1' because 'fileName' unlike 'rootName'
            //  has leading '/'
            if (fileName != null && fileName.length() != 0) {
                fileName = fileName.substring(rootName.length() + 1);
                name.append(fileName);
            }

            pathToNativeSeparator(name, curr, name.length() - curr);
            this.fileName = getNativeName(name.toString());
        }
    }

    /**
     * Determines whether specified root is a private root.
     *
     * @param rootName the name of file root
     * @return <code>true</code> if the root is a private root,
     *                <code>false</code> otherwise.
     */
    private boolean isPrivateRoot(String rootName) {
        String privateRootURL = System.getProperty("fileconn.dir.private");
        String rootURL = "file:///" + rootName;
        return rootURL.equalsIgnoreCase(privateRootURL);
    }

    /**
     * Retrieves native path for the root.
     * If specified root is a private root, composes dedicated path to
     * the suite's private work directory.
     *
     * @param rootName the name of file root
     * @return native path for the root
     */
    private String getRootPath(String rootName) {
        if (rootName == null || rootName.length() == 0) {
            return null;
        }

        String rootPath = getNativePathForRoot(rootName);

        if (isPrivateRoot(rootName)) {
            MIDletSuite midletSuite =
                MIDletStateHandler.getMidletStateHandler().getMIDletSuite();
            rootPath += midletSuite.getID() + getFileSeparator();
        }

        return rootPath;
    }

    // JAVADOC COMMENT ELIDED
    public void createPrivateDir(String rootName) throws IOException {
        // create private directory if necessary
        if (isPrivateRoot(rootName) && !privateDirExists) {
            BaseFileHandler fh = new DefaultFileHandlerTO_REMOVE();
            fh.connect(rootName, "");
            if (!fh.exists()) {
                fh.mkdir();
            }
            privateDirExists = true;
        }
    }

    // JAVADOC COMMENT ELIDED
    public Vector list(String filter, boolean includeHidden)
            throws IOException {

        Vector list = new Vector();

        long dirHandler = openDir();

        String fname = dirGetNextFile(dirHandler, includeHidden);
        while (fname != null) {
            // cleanname is passed to the filter and does not contain trailing
            // slash denoting directory
            String cleanname;
            if (fname.charAt(fname.length() - 1) == '/') {
                cleanname = fname.substring(0, fname.length() - 1);
            } else {
                cleanname = fname;
            }

            if (filterAccept(filter, cleanname)) {
                list.addElement(fname);
            }
            fname = dirGetNextFile(dirHandler, includeHidden);
        }

        closeDir(dirHandler);

        return list;
    }

    // JAVADOC COMMENT ELIDED
    public Vector listRoots() {
    	
    	return null;
    	
//        Vector roots = new Vector();
//        String s = getMountedRoots();
//        if (s != null) {
//            String[] rs = com.sun.kvem.midp.pim.formats.FormatSupport
//                .split(s, '\n', 0);
//            for (int i = 0; i < rs.length; i++) {
//                roots.addElement(rs[i]);
//            }
//        }
//        return roots;
    }

    // JAVADOC COMMENT ELIDED
    public native void create() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native boolean exists();

    // JAVADOC COMMENT ELIDED
    public native boolean isDirectory();

    // JAVADOC COMMENT ELIDED
    public native void delete() throws IOException;

    // JAVADOC COMMENT ELIDED
    public void rename(String newName) throws IOException {
        // we start search of '/' from '1' to skip leading '/'
        // that means locale machine in URL specification
        int rootEnd = newName.indexOf('/', 1);
        String rootName = newName.substring(1, rootEnd + 1);
        String rootPath = getRootPath(rootName);
        if (rootPath != null)
        {
            StringBuffer name = new StringBuffer(rootPath);
            int curr = name.length();

            String newNameWORoot = newName.substring(rootEnd + 1);
            name.append(newNameWORoot);

            pathToNativeSeparator(name, curr, name.length() - curr);
            rename0(name.toString());
        }
    }

    /**
     * Helper method that renames the file.
     * @param newName new name for the file
     * @throws IOException if any error occurs
     */
    private native void rename0(String newName) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void truncate(long byteOffset) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native long fileSize() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native boolean canRead();

    // JAVADOC COMMENT ELIDED
    public native boolean canWrite();

    // JAVADOC COMMENT ELIDED
    public boolean isHidden() {
        // Note: ANSI C does not define hidden files.
        // Sure, on UNIX systems a file is considered to be hidden
        // if its name begins with a period character ('.'), but we can not
        // rename files during setHidden() method, so we consider
        // what hidden files are not supported on UNIX systems, and this method
        // always returns false on UNIX as it's required by JSR 75 spec.
        return isHidden0();
    }

    /**
     * Helper method that checks if the file is visible.
     * @return <code>true</code> if the file is not visible
     */
    private native boolean isHidden0();

    // JAVADOC COMMENT ELIDED
    public native void setReadable(boolean readable) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void setWritable(boolean writable) throws IOException;

    // JAVADOC COMMENT ELIDED
    public void setHidden(boolean hidden) throws IOException {
        setHidden0(hidden);
    }

    /**
     * Helper method that marks the file hidden flag.
     * @param hidden <code>true</code> to make file as not visible
     * @throws IOException if any error occurs
     */
    private native void setHidden0(boolean hidden) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void mkdir() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native long availableSize();

    // JAVADOC COMMENT ELIDED
    public native long totalSize();

    // JAVADOC COMMENT ELIDED
    public long usedSize() {
        return totalSize() - availableSize();
    }

    // JAVADOC COMMENT ELIDED
    public String illegalFileNameChars() {
        return illegalChars;
    }

    // JAVADOC COMMENT ELIDED
    public native long lastModified();

    // JAVADOC COMMENT ELIDED
    public native void openForRead() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void closeForRead() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void openForWrite() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void closeForWrite() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native int read(byte b[], int off, int len) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native int write(byte b[], int off, int len) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void positionForWrite(long offset) throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void flush() throws IOException;

    // JAVADOC COMMENT ELIDED
    public native void close() throws IOException;

    /**
     * Replace all entries of the "//" with "/" (multiple separators
     * with single separator) and all "/" with the native separator.
     *
     * @param name StringBuffer to process
     * @param off offset from where to start the conversion
     * @param len length to convert
     *
     * @return the same SringBuffer after the process
     */
    private StringBuffer pathToNativeSeparator(StringBuffer name,
                            int off, int len) {

        int  length = off + len;
        int  curr   = off;
        char sep    = getFileSeparator();

        while ((curr + 1) < length) {
            if (name.charAt(curr) == '/' && name.charAt(curr+1) == '/') {
                name.deleteCharAt(curr);
                length--;
                continue;
            } else if (name.charAt(curr) == '/') {
                name.setCharAt(curr, sep);
            }
            curr++;
        }

        // trim trailing slash if it exists
        if (name.charAt(length - 1) == '/') {
            name.deleteCharAt(length - 1);
        }

        return name;
    }

    // JAVADOC COMMENT ELIDED
    private boolean filterAccept(String filter, String str) {

        if (filter == null) {
            return true;
        }

        if (filter.length() == 0) {
            return false;
        }

        int  currPos = 0;
        int currComp = 0, firstSigComp = 0;
        int idx;

        // Splitted string does not contain separators themselves
        String components[] = split(filter, '*', 0);

        // if filter does not begin with '*' check that string begins with
        // filter's first component
        if (filter.charAt(0) != '*') {
            if (!str.startsWith(components[0])) {
                return false;
            } else {
                currPos += components[0].length();
                currComp++;
                firstSigComp = currComp;
            }
        }

        // Run on the string and check that it contains all filter
        // components sequentially
        for (; currComp < components.length; currComp++) {
            if ((idx = str.indexOf(components[currComp], currPos)) != -1) {
                currPos = idx + components[currComp].length();
            } else {
                // run out of the string while filter components remain
                return false;
            }
        }

        // At this point we run out of filter. First option is that
        // filter ends with '*', or string is finished,
        // we are fine then, and accept the string.
        //
        // In the other case we check that string ends with the last component
        // of the filter (given that there was an asterisk before the last
        // component
        if (!(filter.charAt(filter.length() - 1) == '*'
                || currPos == str.length())) {
            if (components.length > firstSigComp) {
                // does string end with the last filter component?
                if (!str.endsWith(components[components.length - 1])) {
                    return false;
                }
            } else {
                // there was no asteric before last filter component
                return false;
            }
        }

        // If we got here string is accepted
        return true;
    }

    /**
     * Parses a separated list of strings into a string array.
     * An escaped separator (backslash followed by separatorChar) is not
     * treated as a separator.
     * @param data string to be processed
     * @param separatorChar the character used to separate items
     * @param startingPoint Only use the part of the string that follows this
     * index
     *
     * @return a non-null string array containing string elements
     */
    private static String[] split(String data, char separatorChar,
            int startingPoint) {

        if (startingPoint == data.length()) {
            return new String[0];
        }
        Vector elementList = new Vector();

        if (data.charAt(startingPoint) == separatorChar) {
            startingPoint++;
        }

        int startSearchAt = startingPoint;
        int startOfElement = startingPoint;

        for (int i; (i = data.indexOf(separatorChar, startSearchAt)) != -1; ) {
            if (i != 0 && data.charAt(i - 1) == '\\') {
                // escaped semicolon. don't treat it as a separator
                startSearchAt = i + 1;
            } else {
                String element = data.substring(startOfElement, i);
                elementList.addElement(element);
                startSearchAt = startOfElement = i + 1;
            }
        }

        if (data.length() > startOfElement) {
            if (elementList.size() == 0) {
                return new String[] { data.substring(startOfElement) };
            }
            elementList.addElement(data.substring(startOfElement));
        }

        String[] elements = new String[elementList.size()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = (String) elementList.elementAt(i);
        }
        return elements;
    }

    /**
     * Return pointer to the system-dependent file name stored in the native
     * code.
     *
     * @param name a string representing the filename to convert to native
     *             the form
     * @return A pointer to the system-dependent file name
     */
    private native static long getNativeName(String name);

    /**
     * Gets the system-dependent file separator character.
     * @return The file separator character.
     */
    private native static char getFileSeparator();

    /**
     * Opens the directory.
     * @return native pointer to an opaque filelist structure used by
     *         methods iterating over file list.
     */
    private native long openDir();

    /**
     * Closes the directory.
     * @param dirHandle native pointer to an opaque filelist structure
     *         returned by openDir method.
     */
    private native void closeDir(long dirHandle);

    /**
     * Gets the next file in directory.
     * @param dirHandle native pointer to a filelist structure
     *                  returned by <code>openDir</code>
     * @param includeHidden determines whether it's necessary
     *                      to include hidden files and directories
     * @return the name of the file.
     */
    private native String dirGetNextFile(long dirHandle, boolean includeHidden);

    /**
     * Gets the mounted root file systems.
     * @return A string containing currently mounted roots
     *          separated by '\n' character
     */
    private native String getMountedRoots();

    /**
     * Gets OS path for the specified file system root.
     * @param root root name
     * @return The path to access the root
     */
    private native String getNativePathForRoot(String root);

    /**
     * Gets the list of illegal characters in file names.
     * @return A string containing the characters
     *         that are not allowed inside file names.
     */
    private native static String illegalFileNameChars0();

    /**
     * Initializes native part of file handler.
     */
    private native static void initialize();

	public RandomAccessStream getRandomAccessStream() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Cleanup after garbage collected instance
     */
    //private native void finalize();
}
