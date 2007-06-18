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
* Created on Jun 23, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.l2cap;

import java.io.IOException;

/** 
 * This abstract class represents an L2CAP Channel between two Bluetooth Devices.
 * It provides the basic methods for opening and closing the connections as well
 * as sending packets. Implementations are expected to implement the nessesary methods to process any received packets.
 * @author Christian Lorenz
 */
public abstract class L2CAPChannel {
    public final static byte CLOSED = 0x00;
    public final static byte CONFIG = 0x01;
    public final static byte OPEN   = 0x02;
    public final static byte FAILED = 0x03;
    public L2CAPSender l2capSender  = null;
    public short localChannelID     = -1;
    public short remoteChannelID    = -1;
    public long remoteAddress       = -1;
    public byte channelState        = CLOSED;

    public L2CAPChannel() { }

    /** This method packages the byte array in a propper L2CAPPacket and sends it via the associated L2CAPSender. */
    public void sendL2CAPPacket(byte[] data) throws IOException {
        if (channelState != OPEN) throw new IOException("L2CAPChannel is not open.");
        if (l2capSender != null) l2capSender.sendL2CAPPacket(this, data);
    }

    /**
     * This method sets the state of this channel to CLOSED and calls
     * the L2CAPSender.closeL2CAPChannel(L2CAPChannel this) method.
     */
    public void close() {
        if (channelState != CLOSED) {
            channelState = CLOSED;
            if (l2capSender != null) l2capSender.closeL2CAPChannel(this);
        }
    }

    /**
     * This abstract method is called when this L2CAPChannel was disconnected
     * due to some outside error such as a lost connection. Implementors are expected to include nesseasry cleam up code here.
     */
    public abstract void wasDisconnected();

    /**
     * This method is called when an L2CAPPacket for this L2CAPChannel was
     * received. The implementation may deal with received data however it wishes.
     */
    public abstract void receiveL2CAPPacket(byte[] dataPacket);
}

