/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
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
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.thenesis.midpath.sound;

import java.io.IOException;

public interface SoundBackend {
	
	
	/**
	 * Opens the line with default value,
	 * causing the line to acquire any required system resources.
	 */
	public void open() throws IOException;
	
	public boolean isOpen();
	
	public AudioFormat getAudioFormat();
	
	public int getBufferSize();
	
//	/**
//	 * Opens the line with the specified buffer size and sample rate,
//	 * causing the line to acquire any required system resources.
//	 * 
//	 * @param bufferSize
//	 * @param sampleRate
//	 */
//	public void open(String deviceName, int bufferSize, int sampleRate) throws IOException;

	/**
	 * Obtains the number of bytes of data that can be written to the buffer
	 * without blocking.
	 * 
	 * @return the amount of data available (in bytes)
	 */
	public int available();

	/**
	 * Writes data to the line
	 * 
	 * @param b the bytes to be written to the line
	 * @param off offset where to get data in the byte array
	 * @param len number of bytes to write to the line
	 * @return the number of bytes actually written
	 */
	public int write(byte[] buf, int offset, int len);


	/**
	 * Closes the line and released system resources it used.
	 */
	public void close();
	
	public Mixer getMixer();

}