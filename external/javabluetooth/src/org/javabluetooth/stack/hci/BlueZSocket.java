package org.javabluetooth.stack.hci;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BlueZSocket {

	private int handle;
	private boolean connectionOpen = false;
	private BlueZInputStream inputStream = new BlueZInputStream();
	private BlueZOutputStream outputStream = new BlueZOutputStream();

	public void open(int deviceNumber) throws IOException {
		handle = open0(deviceNumber);
		connectionOpen = true;
	}

	public void close() throws IOException {
		if (connectionOpen) {
			close0(handle);
			connectionOpen = false;
		}
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}

	public boolean isOpen() {
		return connectionOpen;
	}

	/**
	 * Ensure connection is open
	 */
	private void ensureOpen() throws IOException {
		if (!connectionOpen) {
			throw new IOException("Connection closed");
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
	private native int open0(int deviceNumber);

	/**
	 * Obtains the number of bytes of data that can be written to the buffer
	 * without blocking.
	 * 
	 * @return the amount of data available (in bytes) or a negative value if an error occurred
	 */
	private native int available0(int handle);

	/**
	 * Writes data to the socket
	 * 
	 * @param b the bytes to be written to the line
	 * @param off offset where to get data in the byte array
	 * @param len number of bytes to write to the line
	 * @return the number of bytes actually written or a negative value if an error occurred
	 */
	private native int write0(int handle, byte[] b, int offset, int frames);

	private native int read0(int handle, byte[] b, int offset, int len);

	/**
	 * Closes the socket
	 */
	private native void close0(int handle);

	
	class BlueZInputStream extends InputStream {
		
	    private boolean eof = false;
	    private byte[] inBuffer = new byte[1];

		public int read() throws IOException {
			ensureOpen();
	        if (eof) {
	            return -1;
	        }
	        int res = read0(handle, inBuffer, 0, 1);
	        if (res == -1) {
	            eof = true;
	        }
	        return inBuffer[0];
		}
		
		public int read(byte[] b) throws IOException { 
			return read(b, 0, b.length);
		}
		
		 /**
	     * Reads up to <code>len</code> bytes of data from the input stream into
	     * an array of bytes.
	     * <p>
	     * Polling the native code is done here to allow for simple
	     * asynchronous native code to be written. Not all implementations
	     * work this way (they block in the native code) but the same
	     * Java code works for both.
	     *
	     * @param      b     the buffer into which the data is read.
	     * @param      off   the start offset in array <code>b</code>
	     *                   at which the data is written.
	     * @param      len   the maximum number of bytes to read.
	     * @return     the total number of bytes read into the buffer, or
	     *             <code>-1</code> if there is no more data because the end of
	     *             the stream has been reached.
	     * @exception  IOException  if an I/O error occurs.
	     */
	    synchronized public int read(byte b[], int off, int len)
	            throws IOException {
	        ensureOpen();
	        if (eof) {
	            return -1;
	        }
	        if (len == 0) {
	            return 0;
	        }
	        // Check for array index out of bounds, and NullPointerException,
	        // so that the native code doesn't need to do it
	        int test = b[off] + b[off + len - 1];

	        int n = 0;
	        while (n < len) {
	            int count = read0(handle, b, off + n, len - n);
	            if (count == -1) {
	                eof = true;
	                if (n == 0) {
	                    n = -1;
	                }
	                break;
	            }
	            n += count;
	            if (n == len) {
	                break;
	            }
	        }
	        
	        return n;
	    }
	    
	    public int available() throws IOException {
			ensureOpen();
			return available0(handle);
		}
	    
	    /**
	     * Close the stream.
	     *
	     * @exception  IOException  if an I/O error occurs
	     */
	    public void close() throws IOException {
	        BlueZSocket.this.close();
	    }
		
	}
	
	/**
	 * Output stream for the connection
	 */
	class BlueZOutputStream extends OutputStream {

		private byte[] outBuffer = new byte[1];
		
	    /**
	     * Writes the specified byte to this output stream.
	     * <p>
	     * Polling the native code is done here to allow for simple
	     * asynchronous native code to be written. Not all implementations
	     * work this way (they block in the native code) but the same
	     * Java code works for both.
	     *
	     * @param      b   the <code>byte</code>.
	     * @exception  IOException  if an I/O error occurs. In particular,
	     *             an <code>IOException</code> may be thrown if the
	     *             output stream has been closed.
	     */
	    synchronized public void write(int b) throws IOException {
	        ensureOpen();
	        outBuffer[0] = (byte)b;
	        while (true) {
	            int res = write0(handle, outBuffer, 0, 1);
	            if (res != 0) {
	                return;
	            }
	        }
	    }
	    
	    synchronized public void write(byte[] b) throws IOException {
	    	write(b, 0, b.length);
	    }

	    /**
	     * Writes <code>len</code> bytes from the specified byte array
	     * starting at offset <code>off</code> to this output stream.
	     * <p>
	     * Polling the native code is done here to allow for simple
	     * asynchronous native code to be written. Not all implementations
	     * work this way (they block in the native code) but the same
	     * Java code works for both.
	     *
	     * @param      b     the data.
	     * @param      off   the start offset in the data.
	     * @param      len   the number of bytes to write.
	     * @exception  IOException  if an I/O error occurs. In particular,
	     *             an <code>IOException</code> is thrown if the output
	     *             stream is closed.
	     */
	    synchronized public void write(byte b[], int off, int len)
	            throws IOException {
	        ensureOpen();
	        if (len == 0) {
	            return;
	        }

	        // Check for array index out of bounds, and NullPointerException,
	        // so that the native code doesn't need to do it
	        int test = b[off] + b[off + len - 1];

	        int n = 0;
	        while (true) {
	            n += write0(handle, b, off + n, len - n);
	            if (n == len) {
	                break;
	            }
	        }
	    }

	    /**
	     * Close the stream.
	     *
	     * @exception  IOException  if an I/O error occurs
	     */
	    public void close() throws IOException {
	        BlueZSocket.this.close();
	    }

	}

	
}
