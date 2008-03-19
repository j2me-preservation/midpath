/* 
 * MIDPath - Copyright (C) 2006-2008 Guillaume Legris, Mathieu Legris
 *
 * GNU Classpath - Copyright (C) 1998, 2001, 2002, 2003, 2006 
 * Free Software Foundation, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details. 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA   */

package org.thenesis.m3g.engine.util;

import java.io.IOException;
import java.io.InputStream;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * plus online API docs for JDK 1.2 beta from http://www.javasoft.com.
 * Status:  Believed complete and correct.
 */

/**
 * This is the common superclass of all standard classes that filter 
 * input.  It acts as a layer on top of an underlying <code>InputStream</code>
 * and simply redirects calls made to it to the subordinate InputStream
 * instead.  Subclasses of this class perform additional filtering
 * functions in addition to simply redirecting the call.
 * <p>
 * This class is not abstract.  However, since it only redirects calls
 * to a subordinate <code>InputStream</code> without adding any functionality
 * on top of it, this class should not be used directly.  Instead, various
 * subclasses of this class should be used.  This is enforced with a
 * protected constructor.  Do not try to hack around it.
 * <p>
 * When creating a subclass of <code>FilterInputStream</code>, override the
 * appropriate methods to implement the desired filtering.  However, note
 * that the <code>read(byte[])</code> method does not need to be overridden
 * as this class redirects calls to that method to 
 * <code>read(byte[], int, int)</code> instead of to the subordinate
 * <code>InputStream read(byte[])</code> method.
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @author Warren Levy (warrenl@cygnus.com)
 */
public class FilterInputStream extends InputStream {
	/**
	 * This is the subordinate <code>InputStream</code> to which method calls
	 * are redirected
	 */
	protected InputStream in;

	/**
	 * Create a <code>FilterInputStream</code> with the specified subordinate
	 * <code>InputStream</code>.
	 *
	 * @param in The subordinate <code>InputStream</code>
	 */
	protected FilterInputStream(InputStream in) {
		this.in = in;
	}

	/**
	 * Calls the <code>in.mark(int)</code> method.
	 *
	 * @param readlimit The parameter passed to <code>in.mark(int)</code>
	 */
	public void mark(int readlimit) {
		in.mark(readlimit);
	}

	/**
	 * Calls the <code>in.markSupported()</code> method.
	 *
	 * @return <code>true</code> if mark/reset is supported, <code>false</code>
	 *         otherwise
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/**
	 * Calls the <code>in.reset()</code> method.
	 *
	 * @exception IOException If an error occurs
	 */
	public void reset() throws IOException {
		in.reset();
	}

	/**
	 * Calls the <code>in.available()</code> method.
	 *
	 * @return The value returned from <code>in.available()</code>
	 *
	 * @exception IOException If an error occurs
	 */
	public int available() throws IOException {
		return in.available();
	}

	/**
	 * Calls the <code>in.skip(long)</code> method
	 *
	 * @param numBytes The requested number of bytes to skip. 
	 *
	 * @return The value returned from <code>in.skip(long)</code>
	 *
	 * @exception IOException If an error occurs
	 */
	public long skip(long numBytes) throws IOException {
		return in.skip(numBytes);
	}

	/**
	 * Calls the <code>in.read()</code> method
	 *
	 * @return The value returned from <code>in.read()</code>
	 *
	 * @exception IOException If an error occurs
	 */
	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Calls the <code>read(byte[], int, int)</code> overloaded method.  
	 * Note that 
	 * this method does not redirect its call directly to a corresponding
	 * method in <code>in</code>.  This allows subclasses to override only the
	 * three argument version of <code>read</code>.
	 *
	 * @param buf The buffer to read bytes into
	 *
	 * @return The value retured from <code>in.read(byte[], int, int)</code>
	 *
	 * @exception IOException If an error occurs
	 */
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	/**
	 * Calls the <code>in.read(byte[], int, int)</code> method.
	 *
	 * @param buf The buffer to read bytes into
	 * @param offset The index into the buffer to start storing bytes
	 * @param len The maximum number of bytes to read.
	 *
	 * @return The value retured from <code>in.read(byte[], int, int)</code>
	 *
	 * @exception IOException If an error occurs
	 */
	public int read(byte[] buf, int offset, int len) throws IOException {
		return in.read(buf, offset, len);
	}

	/**
	 * This method closes the input stream by closing the input stream that
	 * this object is filtering.  Future attempts to access this stream may
	 * throw an exception.
	 * 
	 * @exception IOException If an error occurs
	 */
	public void close() throws IOException {
		in.close();
	}
}
