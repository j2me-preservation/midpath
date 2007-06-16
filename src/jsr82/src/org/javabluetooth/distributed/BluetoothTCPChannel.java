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
* Created on Jul 20, 2003
* by Christian Lorenz
*/

package org.javabluetooth.distributed;

import java.io.IOException;

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.util.Debug;

/** 
 * An implementation of the L2CAPChannel interface used by the BluetoothTCPServer.
 * This implementation forwards any received L2CAP Packet over TCP/IP to the
 * corrosponding BluetoothTCPClient, where it is piped to the proper L2CAP Channel.
 * @see org.javabluetooth.stack.l2cap.L2CAPChannel
 * @author Christian Lorenz
 */
public class BluetoothTCPChannel extends L2CAPChannel {
    private static final byte L2CAP_PACKET = (byte)0xff;
    BluetoothTCPServerThread thread;
    short channelHandel;

    /**
     * @param thread The BluetoothTCPServerThread associated with this BluetoothTCPChannel.
     * @see org.javabluetooth.stack.distributed.BluetoothTCPServerThread
     */
    public BluetoothTCPChannel(BluetoothTCPServerThread thread, short channelHandel) {
        this.thread = thread;
        this.channelHandel = channelHandel;
    }

    /** @see org.javabluetooth.stack.l2cap.L2CAPChannel#receiveL2CAPPacket(byte[]) */
    public void receiveL2CAPPacket(byte[] dataPacket) {
        byte[] l2capPacket = new byte[5 + dataPacket.length];
        l2capPacket[0] = L2CAP_PACKET;
        l2capPacket[1] = (byte)((dataPacket.length + 2) & 0xff);;
        l2capPacket[2] = (byte)(((dataPacket.length + 2) >> 8) & 0xff);
        l2capPacket[3] = (byte)((channelHandel) & 0xff);
        l2capPacket[4] = (byte)((channelHandel >> 8) & 0xff);
        System.arraycopy(dataPacket, 0, l2capPacket, 5, dataPacket.length);
        Debug.println(7, "BluetoothTCPServer: Sending L2CAP Packet to BluetoothTCPClient.");
        try { thread.sendPacketToBluetoothTCPClient(l2capPacket); }
        catch (IOException e) { this.close(); }
    }

    public void wasDisconnected() {
        Debug.println(7, "BluetoothTCPServer: Sending L2CAP Disconnection Request to BluetoothTCPClient.");
        byte[] l2capPacket = { (byte)0xf2, 0x02, 0x00, (byte)((channelHandel) & 0xff), (byte)((channelHandel >> 8) & 0xff) };
        try { thread.sendPacketToBluetoothTCPClient(l2capPacket); }
        catch (IOException e) { }
    }
}

