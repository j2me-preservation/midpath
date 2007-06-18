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

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPChannelFactory;

/** 
 * Demo Class, demonstrates how to write a ChannelFactory to provide Bluetooth Services.
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 * @author Christian Lorenz
 */
public class EchoChannelFactory implements L2CAPChannelFactory {
    /** @see org.javabluetooth.stack.l2cap.L2CAPChannelFactory#newL2CAPChannel() */
    public L2CAPChannel newL2CAPChannel() { return new EchoChannel(); }
}

