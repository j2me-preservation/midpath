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
* Created on Jul 4, 2003
* by Christian Lorenz
*/

package org.javabluetooth.distributed;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;

/** 
 * This thread listens for incomming TCP Connections on the specified port and
 * creates new BluetoothTCPServerThread objects, which handle the connections once they are established.
 * @see org.javabluetooth.stack.distributed.BluetoothTCPServerThread
 * @author Christian Lorenz
 */
public class BluetoothTCPServer extends Thread {
    private HCIDriver hciTransport;
    private ServerSocket serverSocket;

    public BluetoothTCPServer(int port) throws HCIException {
        hciTransport = HCIDriver.getHCIDriver();
        try {
            serverSocket = new ServerSocket(port);
            this.start();
        }
        catch (IOException e) { throw new HCIException("HCIManagerRemoteServer: IOException: " + e); }
    }

    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                (new BluetoothTCPServerThread(socket, hciTransport)).start();
            }
            catch (IOException e) { System.err.println("HCIManagerRemoteServer: Error: " + e); }
        }
    }
}

