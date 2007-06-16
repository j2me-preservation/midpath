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
* Created on May 23, 2003
* by Christian Lorenz
*
*/

package org.javabluetooth.stack;

import javax.bluetooth.RemoteDevice;
import org.javabluetooth.distributed.BluetoothTCPChannelFactory;
import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.hci.HCIReceiver;
import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPLink;
import org.javabluetooth.stack.sdp.SDPServer;

/** 
 * This class is a thin local implementation of <code>BluetoothStack</code>.
 * It connects directly to the local <code>HCITransport</code> instance.
 * It implements the <code>HCITransport</code>entry points defined in <code>HCIReceiver</code>.
 * @see org.javabluetooth.stack.BluetoothStack
 * @see org.javabluetooth.stack.hci.HCIReceiver
 * @see org.javabluetooth.stack.hci.HCITransport
 * @author Christian Lorenz
 */
public class BluetoothStackLocal extends BluetoothStack implements HCIReceiver {
    private HCIDriver hciTransport;

    public BluetoothStackLocal() throws HCIException {
        this.hciTransport = HCIDriver.getHCIDriver();
        hciTransport.registerHCIReceiver(this);
    }

    /** @see org.javabluetooth.stack.BluetoothStack#send_HCI_Command_Packet(byte[]) */
    protected byte[] send_HCI_Command_Packet(byte[] cmdPacket) throws HCIException {
        return hciTransport.send_HCI_Command_Packet(cmdPacket);
    }

    /** @see org.javabluetooth.stack.hci.HCIInquiryResultReceiver#receive_HCI_Event_Inquiry_Result(byte[]) */
    public void receive_HCI_Event_Inquiry_Result(byte[] eventPacket) { super.receive_HCI_Event_Inquiry_Result(eventPacket); }

    /** @see org.javabluetooth.stack.hci.HCIInquiryReceiver#receive_HCI_Event_Inquiry_Complete(byte[]) */
    public void receive_HCI_Event_Inquiry_Complete(byte[] eventPacket)
        { super.receive_HCI_Event_Inquiry_Complete(eventPacket); }

    /** @see org.javabluetooth.stack.hci.HCIInquiryReceiver#receive_HCI_Event_Remote_Name_Request_Complete(byte[]) */
    public void receive_HCI_Event_Remote_Name_Request_Complete(byte[] eventPacket) {
        super.receive_HCI_Event_Remote_Name_Request_Complete(eventPacket);
    }

    /** @see org.javabluetooth.stack.BluetoothStack#open_L2CAPChannel(long, short) */
    public void connectL2CAPChannel(L2CAPChannel channel, RemoteDevice remoteDevice, short psm) throws HCIException {
        L2CAPLink link = hciTransport.getL2CAPLink(remoteDevice.bdAddrLong, remoteDevice.pageScanRepMode,
            remoteDevice.pageScanMode, remoteDevice.clockOffset);
        link.connectL2CAPChannel(channel, psm);
    }

    /**
     * @see org.javabluetooth.stack.BluetoothStack#openL2CAPService(org.javabluetooth.stack.l2cap.L2CAPChannel, short,
     * short, byte[])
     */
    public void registerL2CAPService(L2CAPChannel channel, short serviceUUID, short browseUUID, byte[] serviceRecord) {
        SDPServer sdpServer = SDPServer.getSDPServer();
        BluetoothTCPChannelFactory channelFactory = new BluetoothTCPChannelFactory(channel);
        sdpServer.registerService(channelFactory, serviceUUID, browseUUID, serviceRecord);
    }
}

