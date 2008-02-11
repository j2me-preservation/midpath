/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
 * 
 * Tritonus - Copyright (C) 1999 - 2002 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
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
 */
package org.thenesis.midpath.sound.backend.esd;

import java.io.IOException;

import com.sun.cldchi.jvm.JVM;

public class EsdSink {
	
	public static final int ESD_BUF_SIZE = (4 * 1024);
	
	public static final int	ESD_STREAM	= 0x0000;
	public static final int	ESD_PLAY	= 0x1000;
	public static final int	ESD_BITS8	= 0x0000;
	public static final int	ESD_BITS16	= 0x0001;
	public static final int	ESD_MONO	= 0x0010;
	public static final int	ESD_STEREO	= 0x0020;
	
	static {
		JVM.loadLibrary("libmidpathesd.so");
		//System.loadLibrary("midpathesd");
	}
	
	private long fd;
	protected boolean isOpen = false;
	
	public void open(String deviceName, int bufferSize, int sampleRate) throws IOException {
		
		int format = ESD_BITS16 | ESD_STEREO | ESD_STREAM | ESD_PLAY;

		fd = open0(format, sampleRate);

		if (fd < 0) {
			throw new IOException("Can not create ESD stream. Reason: " + fd);
		}
	}
	
	public int available() {
		// ESD can't return available buffer size
		return ESD_BUF_SIZE;
	}

	public void close() {
		close0(fd);
		isOpen = false;
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public int write(byte[] buf, int offset, int len) {
		int writtenBytes = write0(fd, buf, offset, len);
		if (writtenBytes > 0)
			return writtenBytes;
		return 0;
	}
	
	/* Native methods */

	/**
	 * Opens the line with the specified buffer size and sample rate,
	 * causing the line to acquire any required system resources.
	 * 
	 * @param deviceName the name of the device to open
	 * @param bufferSize the bufferSize (in frames)
	 * @param sampleRate the sample rate
	 * @return a value > 0 if success, otherwise a value < 0
	 */
	private native long open0(int format, int sampleRate);

	/**
	 * Writes data to the line
	 * 
	 * @param b the bytes to be written to the line
	 * @param off offset where to get data in the byte array
	 * @param len number of bytes to write to the line
	 * @return the number of bytes actually written or a negative value if an error occurred
	 */
	private native int write0(long fd, byte[] buf, int offset, int frames);

	/**
	 * Closes the line and released system resources it used.
	 */
	private native void close0(long fd);
	

}
