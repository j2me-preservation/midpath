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
 * The <code>ServiceRegistrationException</code> is thrown when there is a failure to add
 * a service record to the local Service Discovery Database (SDDB) or to modify
 * an existing service record in the SDDB.  The failure could be because the
 * SDDB has no room for new records or because the modification being
 * attempted to a service record violated one of the rules about
 * service record updates.  This exception will also be thrown if it
 * was not possible to obtain an RFCOMM server channel needed for a <code>btspp</code> service record.
 * @author Christian Lorenz
 */
public class ServiceRegistrationException extends IOException {
    /** Creates a <code>ServiceRegistrationException</code> without a detailed message. */
    public ServiceRegistrationException() { throw new RuntimeException("Not Implemented! Used to compile Code"); }

    /**
     * Creates a <code>ServiceRegistrationException</code> with a detailed message.
     * @param msg the reason for the exception
     */
    public ServiceRegistrationException(String msg) { throw new RuntimeException("Not Implemented! Used to compile Code"); }
}

