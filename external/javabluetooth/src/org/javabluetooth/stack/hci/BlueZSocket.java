package org.javabluetooth.stack.hci;

import java.io.IOException;

public class BlueZSocket {
	
	protected boolean isOpen = false;
	
	public void open(String deviceName, int bufferSize, int sampleRate) throws IOException {

		int rval = open0(deviceName, bufferSize, sampleRate);

		if (rval < 0) {
			throw new IOException("Can't open device. Reason: " + rval);
		}
	}
	
	public int available() throws IOException {
		int available = available0();

		if (available < 0) {
			throw new IOException();
		}
		
		return available;
		
	}

	public void close() throws IOException {
		close0();
		isOpen = false;
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void write(byte[] buf, int offset, int len) throws IOException {
		while (len > 0) {
			int writtenBytes = write0(buf, offset, len);
			if (writtenBytes < 0) {
				throw new IOException();
			}
			offset += writtenBytes;
			len -= writtenBytes;
		}
	}
	
	/* Native methods */

	/**
	 * Opens the socket with the specified buffer size and sample rate,
	 * causing the line to acquire any required system resources.
	 * 
	 * @param deviceName the name of the device to open
	 * @param bufferSize the bufferSize (in frames)
	 * @param sampleRate the sample rate
	 * @return a value > 0 if success, otherwise a value < 0
	 */
	private native int open0(String deviceName, int bufferSize, int sampleRate);

	/**
	 * Obtains the number of bytes of data that can be written to the buffer
	 * without blocking.
	 * 
	 * @return the amount of data available (in bytes) or a negative value if an error occurred
	 */
	private native int available0();

	/**
	 * Writes data to the socket
	 * 
	 * @param b the bytes to be written to the line
	 * @param off offset where to get data in the byte array
	 * @param len number of bytes to write to the line
	 * @return the number of bytes actually written or a negative value if an error occurred
	 */
	private native int write0(byte[] buf, int offset, int frames);

	/**
	 * Closes the socket
	 */
	private native void close0();

}
