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
*/


package javax.bluetooth;

import java.io.IOException;

/** 
 * The <code>BluetoothStateException</code> is thrown when a request is made to the Bluetooth system that
 * the system cannot support in its present state.  If, however, the
 * Bluetooth system was not in this state, it could support this operation.
 * For example, some Bluetooth systems do not allow the device to go into
 * inquiry mode if a connection is established.  This exception would be thrown if <code>startInquiry()</code> were called.
 * @author Christian Lorenz
 */
public class BluetoothStateException extends IOException {
    /** Creates a new <code>BluetoothStateException</code> without a detail message. */
    public BluetoothStateException() { super(); }

    /**
     * Creates a <code>BluetoothStateException</code> with the specified detail message.
     * @param msg the reason for the exception
     */
    public BluetoothStateException(String msg) { super(msg); }
}

