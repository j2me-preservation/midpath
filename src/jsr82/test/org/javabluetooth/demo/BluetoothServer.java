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

import org.javabluetooth.distributed.BluetoothTCPServer;
import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.UARTTransport;

/** @author Christian Lorenz */
public class BluetoothServer {
    public static final void main(String[] args) throws Exception {
        HCIDriver.init(new UARTTransport("serial0"));
        HCIDriver hciDriver = HCIDriver.getHCIDriver();
        hciDriver.send_HCI_HC_Reset();
        //SDPServer sddb = SDPServer.getSDPServer();	
        //byte[] dummyHandle={(byte) 0x00,(byte) 0x01,(byte)0x00,(byte)0x00};
        //UUID browseUUID=new UUID(0x1002);
        //UUID socketUUID=new UUID(0x1101);
        //sddb.registerServiceHandle(browseUUID,dummyHandle);
        //sddb.registerServiceHandle(socketUUID,dummyHandle);
        BluetoothTCPServer server = new BluetoothTCPServer(2600);
        while (true) { Thread.sleep(10000); }
    }
}

