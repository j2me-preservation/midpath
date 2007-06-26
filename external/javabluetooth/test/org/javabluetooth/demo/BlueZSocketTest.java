package org.javabluetooth.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.javabluetooth.stack.hci.BlueZSocket;

public class BlueZSocketTest {

	private static final byte EVT_INQUIRY_RESULT = (byte) 0x02;
	private static final byte EVT_INQUIRY_COMPLETE = (byte) 0x01;
	private static final byte EVT_CMD_STATUS = (byte) 0x0F;
	
	private static final byte CMD_PKT = 0x01;
	
	private BlueZSocket socket;
	OutputStream os;
	InputStream is;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BlueZSocketTest testSuite = new BlueZSocketTest();
		try {
			testSuite.setUp();
			testSuite.test();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setUp() throws IOException {
		socket = new BlueZSocket();
		socket.open(0);
		os = socket.getOutputStream();
		is = socket.getInputStream();
	}

	public void test() throws IOException {
		byte[] resp = new byte[32];
		byte[] cmd_Inquiry = { (byte) 0x01, (byte) 0x01, (byte) 0x04, (byte) 0x05, (byte) 0x33, (byte) 0x8B, (byte) 0x9E,
				(byte) 0x08, (byte) 0x0A };
		 byte[] cmd_Write_Scan_Enable = { CMD_PKT, 0x1a, 0x0C, 0x01, 0x03 };
	
		os.write(cmd_Inquiry);
		//os.write(cmd_Write_Scan_Enable);
		System.out.println("Packet sent");
		os.flush();

		boolean running = true;
		while (running) {
			for (int i = 0; i < resp.length; i++) resp[i] = 0;
			//System.out.println("available: " + is.available());
			int val = is.read(resp);
			if (val == -1) {
				System.out.println("EOS");
				break;
			}
			//System.out.println("Packet of " + val + " bytes received");
			//printHex(resp, val);
			
			switch (resp[1]) {
			case EVT_CMD_STATUS:
				if (resp[3] != 0) {
					System.out.println("Error !");
					running = false;
				} else {
					System.out.println("Command pending");
				}
				break;
			case EVT_INQUIRY_RESULT:
				System.out.println("Device found:");
				System.out.println("  * Address : " + Integer.toHexString(resp[9]) + ":" + Integer.toHexString(resp[8])+ ":" + Integer.toHexString(resp[7])+ ":" + Integer.toHexString(resp[6])+ ":" + Integer.toHexString(resp[5])+ ":" + Integer.toHexString(resp[4]));
				System.out.println("  * Class : " + Integer.toHexString(resp[15])+ Integer.toHexString(resp[14])+ Integer.toHexString(resp[13]));
				break;
			case EVT_INQUIRY_COMPLETE:
				System.out.println("Inquiry completed:");
				running = false;
				break;
			default:
				System.out.println("Unknown command:");
				break;
			}
		}

	}
	
	private void printHex(byte[] buf, int len) {
		for (int i = 0; i < len; i++) {
			String val = Integer.toHexString(buf[i]);
			if (val.length() == 1) {
				val = "0" + val;
			}
			System.out.print(val + ":");
		}
		System.out.println();
	}

}
