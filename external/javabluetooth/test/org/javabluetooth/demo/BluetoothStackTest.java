package org.javabluetooth.demo;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.BluetoothStackLocal;
import org.javabluetooth.stack.hci.BlueZTransport;
import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;

public class BluetoothStackTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		BluetoothStackTest testSuite = new BluetoothStackTest();
		try {
			testSuite.test();
		} catch (HCIException e) {
			e.printStackTrace();
		}

	}
	
	public void test() throws HCIException {
		HCIDriver.init(new BlueZTransport(0));
        BluetoothStack.init(new BluetoothStackLocal());
        
        //BluetoothStack.init(new BluetoothTCPClient("192.168.10.2", 2600));
        BluetoothStack bluetooth = BluetoothStack.getBluetoothStack();
        bluetooth.send_HCI_HC_Write_Scan_Enable((byte)0x03);
	}

}
