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
* Created on Jun 1, 2003
*
* To change the template for this generated file go to
* Window>Preferences>Java>Code Generation>Code and Comments
*/

package org.javabluetooth.stack.hci;

/** 
 * This Exception indicated some error on the HCI Layer.
 * @author Christian Lorenz
 */
public class HCIException extends Exception {
    public HCIException() { super(); }

    public HCIException(String message) { super(message); }

    /* The TINIConverter has issues with Throwable
    public HCIException(Throwable cause) {
    super(cause);
    }
    public HCIException(String message, Throwable cause) {
    super(message, cause);
    }
    */
}

