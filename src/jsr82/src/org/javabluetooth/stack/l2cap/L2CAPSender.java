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
* Created on Jul 16, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.l2cap;

import java.io.IOException;

/** 
 * The L2CAPSender interface is implemented by classes which manage L2CAPChannels.
 * Implementing classes are expected to dispatch received L2CAPPackets to L2CAPChannel.receiveL2CAPPacket(byte[] dataPacket).
 * Implementing classes are also expected to call the L2CAPChannel.wasDisconnected()
 * method in case the L2CAPChannel or one of its underlying transports was disconnected
 * unexpectedly and will no longer be able to receive or send further L2CAPPackets.
 * @author Christian Lorenz
 */
public interface L2CAPSender {
    /** This method should package the byte array in a proper L2CAPPacket and dispatch it. */
    public void sendL2CAPPacket(L2CAPChannel channel, byte[] packet) throws IOException;

    /** This method is usually called from L2CAPChannel.close(), and is expected to properly close the L2CAPChannel. */
    public void closeL2CAPChannel(L2CAPChannel channel);
}

