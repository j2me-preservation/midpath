package org.javabluetooth.stack.hci;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.javabluetooth.util.Debug;

public class BlueZTransport extends HCIDriver {

	private byte[] readBuffer = new byte[32];
	private BlueZSocket socket;
	private int deviceNumber;
	private InputStream inputStream;
	private OutputStream outputStream;
	private PollThread thread = new PollThread();

	public BlueZTransport(int deviceNumber) throws HCIException {
		this.deviceNumber = deviceNumber;
		reset();
	}
	
	protected void close() throws IOException {
		if (socket != null) {
			socket.close();
		}
		if (thread != null) {
			thread.stop();
		}
	}

	protected void reset() throws HCIException {
		try {
			close();
			socket = new BlueZSocket();
			socket.open(deviceNumber);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			thread = new PollThread();
			thread.start();
		} catch (IOException e) {
			throw new HCIException("Can't open device hci" + deviceNumber);
		}
	}

	protected void sendPacket(byte[] packet) throws HCIException {
		try {
			Debug.println(0, "HCI: Sending Packet:", packet);
			outputStream.write(packet);
		} catch (IOException e) {
			throw new HCIException("IO Error while sending Packet. " + e);
		}
	}

	private class PollThread implements Runnable {

		private volatile Thread thread;

		public void start() {
			thread = new Thread(PollThread.this);
		}

		public void stop() {
			thread = null;
		}

		public void run() {

			try {
				while (thread == Thread.currentThread()) {
					int byteCount = inputStream.read(readBuffer);
					if (byteCount > 0) {
						receiveData(readBuffer, byteCount);
					} else {
						// EOF
						break;
					}
				}
			} catch (IOException e) {
				Debug.println(Debug.DEBUGLEVELMAX, e.getMessage());
			}

		}

	}

}
