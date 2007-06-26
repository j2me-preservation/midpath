/*
*  (c) Copyright 2003 Christian Lorenz  ALL RIGHTS RESERVED.
* 
* This file is part of the JavaBluetooth Stack.
* 
* The JavaBluetooth Stack is free software; you can redistribute it 
* and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.
* 
* The JavaBluetooth Stack is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
*/

package org.javabluetooth.demo;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.BluetoothStackLocal;
import org.javabluetooth.stack.hci.BlueZTransport;
import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.sdp.SDPClientChannel;
import org.javabluetooth.stack.sdp.SDPServer;

/** @author Christian Lorenz */
public class BluetoothLocalStack implements DiscoveryListener {
    private static RemoteDevice remoteDevice;
    private static BluetoothStack bluetooth;

    public static final void main(String[] args) throws Exception {
        //HCIDriver.init(new UARTTransport("serial0"));
        //HCIDriver.init(new UARTTransport("COM40"));
    	HCIDriver.init(new BlueZTransport(0));
        BluetoothStack.init(new BluetoothStackLocal());
        BluetoothLocalStack blue = new BluetoothLocalStack();
        while (remoteDevice == null) { Thread.sleep(1000); }
        System.out.println("Remote Name is " + remoteDevice.getFriendlyName(false));
        SDPClientChannel sdpChannel = new SDPClientChannel(remoteDevice, blue);
        bluetooth.connectL2CAPChannel(sdpChannel, remoteDevice, (short)0x0001);
        byte[] uuidList = { 0x35, 0x03, 0x19, 0x10, 0x02 };
        DataElement uuidListElement = new DataElement(uuidList);
        sdpChannel.send_SDP_ServiceSearchRequest((short)1, (short)14, uuidListElement);
        while (true) { Thread.sleep(600); }
    }

    BluetoothLocalStack() throws HCIException, BluetoothStateException {
        bluetooth = BluetoothStack.getBluetoothStack();
        bluetooth.send_HCI_HC_Change_Local_Name("TINI BLUE");
        bluetooth.send_HCI_HC_Write_Scan_Enable((byte)0x03);
        bluetooth.send_HCI_HC_Write_Event_Filter_Connection_Setup((byte)0x02);
        bluetooth.send_HCI_HC_Write_Event_Filter_Inquiry_Result();
        SDPServer sddb = SDPServer.getSDPServer();
        LocalDevice localDev = LocalDevice.getLocalDevice();
        System.out.println("Local Bluetooth Name is "+localDev.getFriendlyName());
        System.out.println("IAC is "+localDev.getDiscoverable());
        System.out.println("set Discoverable "+localDev.setDiscoverable(DiscoveryAgent.GIAC));
        System.out.println("IAC is "+localDev.getDiscoverable());
        DiscoveryAgent discovery = localDev.getDiscoveryAgent();
        System.out.println("Inquiry:" + discovery.startInquiry(DiscoveryAgent.GIAC, this));
    }

    /** @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass) */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device Discovered" + btDevice.bdAddrLong);
        remoteDevice = btDevice;

        /*		try {
        System.out.println("Device Name: "+btDevice.getFriendlyName(false));
        } catch (IOException e) {
        e.printStackTrace();
        } */
    }

    /** @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[]) */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) { System.err.println("Service Discovered"); }

    /** @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int) */
    public void serviceSearchCompleted(int transID, int respCode) { }

    /** @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int) */
    public void inquiryCompleted(int discType) { }
}

