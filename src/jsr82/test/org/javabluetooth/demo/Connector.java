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
* Created on Jul 6, 2003
* by Christian Lorenz
*/

package org.javabluetooth.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.l2cap.JSR82Channel;
import org.javabluetooth.stack.l2cap.JSR82ConnectionNotifier;

/** 
 * This Class is a very basic implementation of the <code>javax.microedition.io.Connector</code>
 * class. It should be used for Java Platforms that do not support the Java CDC, and
 * only provides minimalistic features, required by <code>javax.bluetooth</code>.
 * If possible the system native of <code>javax.microedition.io.Connector</code> shoult be used.
 * This class provides the static methods used to create all the connection objects.
 * The parameter string describing the target conforms to the URL format as described in RFC 1808. This takes the general
 * form: {scheme}:[{target}][{parms}] This implementation only supports connections of protocol scheme<code>btl2cap://</code>,
 * and ignores all parameters. The target is defined as bluetoothAddress:UUID.
 * A valid url would be <code>btl2cap://00112233445566:0001</code>.
 * @author Christian Lorenz
 */
public class Connector {
    public static final int READ       = 0;
    public static final int WRITE      = 0;
    public static final int READ_WRITE = 0;

    /**
     * Create and open a Connection. This Implementation only supports the <code>btl2cap://</code> protocol schema.
     * If url begins with <code>btl2cap://localhost</code>, this method call
     * will return a <code>javax.bluetooth.L2CAPConnectionNotifier</code>.
     * @param url The URL for the connection.
     * @return A new <code>javax.bluetooth.L2CAPConnection</code>  or a <code>javax.bluetooth.L2CAPConnectionNotifier</code>
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws IOException If some other kind of I/O error occurs.
     * @see javax.bluetooth.L2CAPConnection
     * @see javax.bluetooth.L2CAPConnectionNotifier
     */
    public static Connection open(String url) throws IOException {
        try {
            if (url.startsWith("btl2cap://localhost:")) {
                int endIndex     = url.indexOf(';');
                String psmString = url.substring(20, endIndex);
                Short psmShort   = Short.decode(psmString);
                short psm        = psmShort.shortValue();
                return new JSR82ConnectionNotifier(psm);
            }
            if (url.startsWith("btl2cap://")) {
                byte[] bdAddrBytes        = new byte[12];
                String bdAddrString       = url.substring(10, 22);
                Long bdAddrLong           = Long.decode("0x" + bdAddrString);
                long remoteAddrLong       = bdAddrLong.longValue();
                int endIndex              = url.indexOf(';', 22);
                String psmString          = url.substring(23, endIndex);
                Short psmShort            = Short.decode(psmString);
                short psm                 = psmShort.shortValue();
                BluetoothStack bluetooth  = BluetoothStack.getBluetoothStack();
                LocalDevice localDev      = LocalDevice.getLocalDevice();
                DiscoveryAgent discovery  = localDev.getDiscoveryAgent();
                RemoteDevice remoteDevice = discovery.getRemoteDevice(remoteAddrLong);
                if (remoteDevice != null) {
                    JSR82Channel channel = new JSR82Channel();
                    bluetooth.connectL2CAPChannel(channel, remoteDevice, psm);
                    return channel;
                }
                else throw new IllegalArgumentException("Unable to locate Bluetooth Device.");
            }
        }
        catch (BluetoothStateException e) { throw new IOException("" + e); }
        catch (HCIException e) { throw new IOException("" + e); }
        throw new IllegalArgumentException("This implementation of Connector only supports btl2cap:// Connections.");
    }

    /**
     * Create and open a Connection.
     * @param url The URL for the connection.
     * @param mode The access mode
     * @return A new Connection object.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs. Christian Lorenz: This implementation ignores access modes.
     * This call is equivilant to Connection.open(url).
     */
    public static Connection open(String url, int mode) throws IOException { return open(url); }

    /**
     * Create and open a Connection
     * @param url The URL for the connection.
     * @param mode The access mode
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * @return A new Connection object.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     * Christian Lorenz: This implementation ignores timeouts and access modes.
     * This call is equivilant to Connection.open(url).
     */
    public static Connection open(String url, int mode, boolean timeouts) throws IOException { return open(url); }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataInputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static DataInputStream openDataInputStream(String url) { //TODO openDataInputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }

    /**
     * Create and open a connection output stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataOutputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static DataOutputStream openDataOutputStream(String url) { //TODO openDataOutputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A InputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static InputStream openInputStream(String url) { //TODO openInputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }

    /**
     * Create and open a connection input stream. This method is not implemented and will throw a Runtime Exception.
     * @param url The URL for the connection.
     * @return A DataOutputStream.
     * @throws IllegalArgumentException If a parameter is invalid.
     * @throws ConnectionNotFoundException If the connection cannot be found.
     * @throws IOException If some other kind of I/O error occurs.
     */
    public static OutputStream openOutputStream(String url) { //TODO openOutputStream
        throw new RuntimeException("This implementation of javax.microedition.io.Connector only supports open(String url).");
    }
}

