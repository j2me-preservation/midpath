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
* Created on Jul 22, 2003
*
* To change the template for this generated file go to
* Window>Preferences>Java>Code Generation>Code and Comments
*/

package org.javabluetooth.demo;

import java.io.*;
import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.util.Debug;

/** 
 * Demo Class, demonstrating how to implement L2CAP Channels. This implementation simply echos back any bytes received.
 * @author Christian Lorenz
 */
public class EchoChannel extends L2CAPChannel {
    /** @see org.javabluetooth.stack.l2cap.L2CAPChannel#wasDisconnected() */
    public void wasDisconnected() { }

    /** @see org.javabluetooth.stack.l2cap.L2CAPChannel#receiveL2CAPPacket(byte[]) */
    public void receiveL2CAPPacket(byte[] dataPacket) {
        Debug.println(2, "EchoChannel bounced:", dataPacket);
        try { this.sendL2CAPPacket(dataPacket); }
        catch (IOException e) { e.printStackTrace(); }
    }
}

