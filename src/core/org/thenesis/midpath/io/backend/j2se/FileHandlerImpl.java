/*
 * MIDPath - Copyright (C) 2006 Guillaume Legris, Mathieu Legris
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
package org.thenesis.midpath.io.backend.j2se;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import com.sun.midp.io.j2me.file.BaseFileHandler;
import com.sun.midp.io.j2me.file.RandomAccessStream;
import com.sun.midp.log.Logging;


public class FileHandlerImpl implements BaseFileHandler, RandomAccessStream {
	
	private File file;
	//private String rootName;
	//private String absFile;
	private RandomAccessFile randomAccessFile;
	
	public FileHandlerImpl() {}

	public long availableSize() {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.availableSize(): not implemented yet");
		return 200000;
		//return -1;
	}

	public boolean canRead() {
		return file.canRead();
	}

	public boolean canWrite() {
		return file.canWrite();
	}

	public void closeForRead() throws IOException {
		// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.closeForRead(): not implemented yet");
	}

	public void closeForWrite() throws IOException {
		// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.closeForRead(): not implemented yet");
	}

	public void connect(String rootName, String absFile) {
		//this.rootName = rootName;
		//this.absFile = absFile;
		file = new File(absFile);
	}

	public void create() throws IOException {
		if (!file.createNewFile()) {
			throw new IOException("Can't create file");
		}
	}

	public void createPrivateDir(String rootName) throws IOException {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.createPrivateDir(): not implemented yet");
	}

	public void delete() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
		
		file.delete();
	}

	public boolean exists() {
		return file.exists();
	}

	public long fileSize() throws IOException {
		return file.length();
	}

	public String illegalFileNameChars() {
		// TODO
		return "";
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public boolean isHidden() {
		return file.isHidden();
	}

	public long lastModified() {
		return file.lastModified();
	}

	public Vector list(final String filter, boolean includeHidden) throws IOException {
		
		if(!file.isDirectory()) {
			new IOException("File is not a directory");
		}
		
		FilenameFilter fileFilter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filterAccept(filter, filename);
			}
		};
		
		String[] filenames;
		if(filter == null) {
			filenames = file.list();
		} else {
			// TODO Doesn't work yet
			filenames = file.list(fileFilter);
		}
		
		Vector v = new Vector();
		for (int i = 0; i < filenames.length; i++) {
			v.addElement(filenames[i]);
		}
		
		return v;
	}
	

	public Vector listRoots() {
		
		File[] files = File.listRoots();
		Vector v = new Vector();
		for (int i = 0; i < files.length; i++) {
			// Add a path separator to the end of the file root if needed
			String rootFilename = files[i].getAbsolutePath();
			rootFilename = rootFilename.replace('\\', '/');
			if (!rootFilename.endsWith("/")) {
				rootFilename += "/";
			}
			v.addElement(rootFilename);
		}
		
		return v;
		
	}

	public void mkdir() throws IOException {
		file.mkdir();
		
	}

	public void openForRead() throws IOException {
		if (randomAccessFile == null) {
			//stream = new FileRandomAccessStream(file);
			randomAccessFile = new RandomAccessFile(file, "rws");
		}
	}

	public void openForWrite() throws IOException {
		if (randomAccessFile == null) {
			randomAccessFile = new RandomAccessFile(file, "rws");
		}
	}

	public void positionForWrite(long offset) throws IOException {
		if (randomAccessFile == null) {
			throw new IOException();
		}
		
		seek((int)offset);
	}

	public void rename(String newName) throws IOException {
		
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
		
		// FIXME Hack ?
		if (newName.startsWith("/")) {
			newName = newName.substring(1, newName.length());
		}
		File newFile = new File(newName);
		
//		int index = newName.lastIndexOf("/");
//		if (index != -1) {
//			newName = newName.substring(index + 1, newName.length());
//		}
//		
//		File newFile = new File(file.getParent(), newName);
		
		if(!file.renameTo(newFile)) {
			throw new IOException("Can't rename file");
		}
//		System.out.println("[DEBUG] FileHandlerImpl.rename(): " + file);
//		System.out.println("[DEBUG] FileHandlerImpl.rename(): " + newFile);
	}

	public void setHidden(boolean hidden) throws IOException {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.setHidden(): not implemented yet");
	}

	public void setReadable(boolean readable) throws IOException {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.setReadable: not implemented yet");
		
	}

	public void setWritable(boolean writable) throws IOException {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.setWritable: not implemented yet");
	}

	public long totalSize() {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.totalSize(): not implemented yet");
		return -1;
	}

	public void truncate(long byteOffset) throws IOException {
		if (randomAccessFile == null) {
			throw new IOException();
		}
		setLength((int)byteOffset);
	}

	public long usedSize() {
		// TODO Auto-generated method stub
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FileHandlerImpl.usedSize(): not implemented yet");
		return -1;
	}

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
    
    /*
     * Interface RandomAccessStream 
     */

	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		if (randomAccessFile == null) {
			throw new IOException();
		}
		//System.out.println("[DEBUG] FileHandlerImpl.read: file: " + file);
		return randomAccessFile.read(b, off, len);
	}

	public void seek(int pos) throws IOException {
		randomAccessFile.seek(pos);
		
	}

	public void setLength(int size) throws IOException {
		randomAccessFile.setLength(size);
		
	}
	
	public int write(byte[] b, int off, int len) throws IOException {
		randomAccessFile.write(b, off, len);
		return len;
	}

	public int write(byte[] buf) throws IOException {
		return write(buf, 0, buf.length);
	}
	
	public void flush() throws IOException {
		// TODO Auto-generated method stub
	}
	
	public void close() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
	}

	public RandomAccessStream getRandomAccessStream() {
		return this;
	}


}
