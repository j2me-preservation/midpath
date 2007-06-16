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
* by Christian Lorenz
*/

package org.javabluetooth.stack.l2cap;

import java.io.IOException;

import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.L2CAPConnectionNotifier;
import javax.bluetooth.ServiceRecord;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.sdp.SDPLocalServiceRecord;

/** 
 * This class is used by the Connecter.open(String url) method. It creates the
 * ServiceRecord for a Bluetooth Service and opens a listening JSR82Channel.
 * @see org.javabluetooth.stack.l2cap.JSR82Channel
 * @see org.javabluetooth.stack.sdp.SDPLocalServiceRecord
 * @author Christian Lorenz
 */
public class JSR82ConnectionNotifier implements L2CAPConnectionNotifier {
    SDPLocalServiceRecord serviceRecord;
    short psm;

    public JSR82ConnectionNotifier(short psm) {
        this.psm = psm;
        this.serviceRecord = new SDPLocalServiceRecord(psm);
    }

    /** @see javax.bluetooth.L2CAPConnectionNotifier#acceptAndOpen() */
    public L2CAPConnection acceptAndOpen() throws IOException {
        JSR82Channel jsr82Channel = new JSR82Channel();
        try {
            BluetoothStack bluetooth = BluetoothStack.getBluetoothStack();
            bluetooth.registerL2CAPService(jsr82Channel, psm, (short)0x1002, serviceRecord.toByteArray());
        }
        catch (HCIException e) { throw new IOException(e.toString()); }
        while (jsr82Channel.channelState != L2CAPChannel.OPEN) {
            try { Thread.sleep(1000); }
            catch (InterruptedException e1) { }
        }
        return jsr82Channel;
    }

    /** @see javax.bluetooth.L2CAPConnectionNotifier#getRecord() */
    public ServiceRecord getRecord() { return serviceRecord; }

    /** @see javax.microedition.io.Connection#close() */
    public void close() {
        // TODO unregister this service from SDPServer
    }
}

