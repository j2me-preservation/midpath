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
* Created on Jul 17, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.l2cap;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;

import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.RemoteDevice;

/** 
 * This class is used by the Connector.open(String url) method. It encapsulates
 * the L2CAPChannel in an L2CAPConnection, which can be used by the JSR-82 implementation.
 * @see	javax.bluetooth.L2CAPConnection
 * @see org.javabluetooth.stack.l2cap.L2CAPChannel
 * @author Christian Lorenz
 */
public class JSR82Channel extends L2CAPChannel implements L2CAPConnection {
    private RemoteDevice remoteDevice;
    private Vector incommingPackets;

    public JSR82Channel() { incommingPackets = new Vector(); }

    public void wasDisconnected() { }

    /** @see org.javabluetooth.stack.l2cap.L2CAPChannel#receivePacket(byte[]) */
    public void receiveL2CAPPacket(byte[] dataPacket) { incommingPackets.addElement(dataPacket); }

    /** @see javax.bluetooth.L2CAPConnection#getTransmitMTU() */
    public int getTransmitMTU() throws IOException {
        // TODO getTransmitMTU
        return 0;
    }

    /** @see javax.bluetooth.L2CAPConnection#getReceiveMTU() */
    public int getReceiveMTU() throws IOException {
        // TODO getReceiveMTU
        return 0;
    }

    /** @see javax.bluetooth.L2CAPConnection#send(byte[]) */
    public void send(byte[] data) throws IOException {
        if (channelState == CLOSED) throw new IOException("L2CAP Channel is closed.");
        sendL2CAPPacket(data);
    }

    /** @see javax.bluetooth.L2CAPConnection#receive(byte[]) */
    public int receive(byte[] inBuf) throws IOException {
        while (incommingPackets.isEmpty()) {
            if (channelState == CLOSED) throw new IOException("L2CAP Channel is closed.");
            try { Thread.sleep(1000); }
            catch (InterruptedException e) { }
        }
        byte[] packet = (byte[]) incommingPackets.elementAt(0);
        if (packet != null) {
            incommingPackets.removeElementAt(0);
            int length = inBuf.length;
            if (packet.length < length) length = packet.length;
            System.arraycopy(packet, 0, inBuf, 0, length);
            return length;
        }
        throw new InterruptedIOException();
    }

    /** @see javax.bluetooth.L2CAPConnection#ready() */
    public boolean ready() throws IOException { return !incommingPackets.isEmpty(); }

    /** @see javax.bluetooth.L2CAPConnection#getRemoteDevice() */
    public RemoteDevice getRemoteDevice() { return remoteDevice; }

    public void setRemoteDevice(RemoteDevice remoteDevice) { this.remoteDevice = remoteDevice; }
}

