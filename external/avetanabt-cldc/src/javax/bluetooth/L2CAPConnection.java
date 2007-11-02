/**
*  (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED.
*
* This file is part of the Avetana bluetooth API for Linux.
*
* The Avetana bluetooth API for Linux is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 of
* the License, or (at your option) any later version.
*
* The Avetana bluetooth API is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* The development of the Avetana bluetooth API is based on the work of
* Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
* on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
* on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
* Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
* are explicitly mentioned.
*
* @author Julien Campana
*/

package javax.bluetooth;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connection;

/**
 * The <code>L2CAPConnection</code> interface represents a connection-oriented L2CAP channel.  This  interface is to be
 * used as part of the CLDC Generic Connection Framework. <P> To create a client connection, the protocol is <code>btl2cap</code>.
 * The target is the combination of the address of the Bluetooth device to connect to and the Protocol
 * Service Multiplexor (PSM) of the service. The PSM value is used by the
 * L2CAP to determine which higher level protocol or application is the recipient of the messages the layer receives. <P>
 * The parameters defined specific to L2CAP are ReceiveMTU (Maximum
 * Transmission Unit (MTU)) and TransmitMTU.  The ReceiveMTU and TransmitMTU parameters are optional. ReceiveMTU
 * specifies the maximum payload size this connection can accept, and
 * TransmitMTU specifies the maximum payload size this connection can
 * send. An example of a valid L2CAP client connection string is:<BR>
 * <code>btl2cap://0050CD00321B:1003;ReceiveMTU=512;TransmitMTU=512</code>

 */
public interface L2CAPConnection extends Connection {
    /**
     * Default MTU value for connection-oriented channels is 672 bytes. <P>
     * The value of <code>DEFAULT_MTU</code> is 0x02A0 (672).
     */
    public static final int DEFAULT_MTU = 672;

    /**
     * Minimum MTU value for connection-oriented channels is 48 bytes. <P> The value of <code>MINIMUM_MTU</code> is 0x30 (48).
     */
    public static final int MINIMUM_MTU = 48;

    /**
     * Returns the MTU that the remote device supports. This value
     * is obtained after the connection has been configured. If the
     * application had specified TransmitMTU in the <code>Connector.open()</code>
     * string then this value should be equal to that. If the application did
     * not specify any TransmitMTU, then this value should be  less than or
     * equal to the ReceiveMTU the remote device advertised during channel configuration.
     * @return the maximum number of bytes that can be sent in a single call to <code>send()</code> without losing any data
     * @exception IOException if the connection is closed
     */
    public int getTransmitMTU() throws IOException;

    /**
     * Returns the ReceiveMTU that the connection supports. If the
     * connection string did not specify a ReceiveMTU, the value returned will be
     * less than or equal to the <code>DEFAULT_MTU</code>. Also, if the connection
     * string did specify an MTU, this value will be less than or equal to the value specified in the connection string.
     * @return the maximum number of bytes that can be read in a single call to <code>receive()</code>
     * @exception IOException if the connection is closed
     */
    public int getReceiveMTU() throws IOException;

    /**
     * Requests that data be sent to the remote device. The TransmitMTU
     * determines the amount of data that can be successfully sent in
     * a single send operation. If the size of <code>data</code> is
     * greater than the TransmitMTU, then only the first TransmitMTU bytes
     * of the packet are sent, and the rest will be discarded.  If
     * <code>data</code> is of length 0, an empty L2CAP packet will be sent.
     * @param data data to be sent
     * @exception IOException if <code>data</code> cannot be sent successfully or if the connection is closed
     * @exception NullPointerException if the <code>data</code> is <code>null</code>
     */
    public void send(byte[] data) throws IOException;

    /**
     * Reads a packet of data. The amount of data received in this operation is related to the value of ReceiveMTU.  If
     * the size of <code>inBuf</code> is greater than or equal to ReceiveMTU, then
     * no data will be lost. Unlike  <code>read()</code> on an
     * <code>java.io.InputStream</code>, if the size of <code>inBuf</code> is
     * smaller than ReceiveMTU, then the portion of the L2CAP payload that will
     * fit into <code>inBuf</code> will be placed in <code>inBuf</code>, the
     * rest will be discarded. If the application is aware of the number of
     * bytes (less than ReceiveMTU) it will receive in any transaction, then
     * the size of <code>inBuf</code> can be less than ReceiveMTU and no data
     * will be lost.  If <code>inBuf</code> is of length 0, all data sent in
     * one packet is lost unless the length of the packet is 0.
     * @param inBuf byte array to store the received data
     * @return the actual number of bytes read; 0 if a zero length packet is received; 0 if <code>inBuf</code> length is zero
     * @exception IOException if an I/O error occurs or the connection has been closed
     * @exception InterruptedIOException if the request timed out
     * @exception NullPointerException if <code>inBuf</code> is <code>null</code>
     */
    public int receive(byte[] inBuf) throws IOException;

    /**
     * Determines if there is a packet that can be read via a call to <code>receive()</code>.  If <code>true</code>, a call to
     * <code>receive()</code> will not block the application.
     * @see #receive
     * @return <code>true</code> if there is data to read; <code>false</code> if there is no data to read
     * @exception IOException if the connection is closed
     */
    public boolean ready() throws IOException;
}

/*  End of the L2CAPConnection interface    */

