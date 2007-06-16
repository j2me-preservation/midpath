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

package org.javabluetooth.distributed;

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPChannelFactory;

/** 
 * The L2CAPChannelFactory used by BluetoothTCPServer to create BluetoothTCPChannel objects.
 * @see org.javabluetooth.stack.l2cap.L2CAPChannelFactory
 * @see org.javabluetooth.stack.distributed.BluetoothTCPServer
 * @see org.javabluetooth.stack.distributed.BluetoothTCPChannel
 * @author Christian Lorenz
 */
public class BluetoothTCPChannelFactory implements L2CAPChannelFactory {
    L2CAPChannel tcpChannel;

    public BluetoothTCPChannelFactory(L2CAPChannel tcpChannel) { this.tcpChannel = tcpChannel; }

    /** @see org.javabluetooth.stack.l2cap.L2CAPChannelFactory#newL2CAPChannel() */
    public L2CAPChannel newL2CAPChannel() { return tcpChannel; }
}

