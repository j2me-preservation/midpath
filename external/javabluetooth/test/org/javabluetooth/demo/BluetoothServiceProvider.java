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
* Created on Jul 31, 2003
*
* To change the template for this generated file go to
* Window>Preferences>Java>Code Generation>Code and Comments
*/

package org.javabluetooth.demo;

import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.L2CAPConnectionNotifier;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import org.javabluetooth.distributed.BluetoothTCPClient;
import org.javabluetooth.stack.BluetoothStack;

/** 
 *	Demo Application, demonstrating the use of BluetoothTCPClient to provide services.
 * @author Christian Lorenz
 */
public class BluetoothServiceProvider {
    public static final void main(String[] args) throws Exception {
        BluetoothStack.init(new BluetoothTCPClient("192.168.10.2", 2600));
        LocalDevice localDev = LocalDevice.getLocalDevice();
        localDev.setDiscoverable(DiscoveryAgent.GIAC);
        L2CAPConnectionNotifier connNotifier = (L2CAPConnectionNotifier)Connector.open("btl2cap://localhost:3;");
        ServiceRecord serviceRecord = connNotifier.getRecord();
        serviceRecord.setAttributeValue(256, new DataElement(DataElement.STRING, "Tini Demo Service"));
        connNotifier.acceptAndOpen();
    }
}

