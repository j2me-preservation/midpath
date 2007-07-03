/*
 *
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.io.j2me.btl2cap;

import java.io.IOException;

import javax.bluetooth.BluetoothConnectionException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.l2cap.JSR82Channel;
import org.javabluetooth.stack.l2cap.JSR82ConnectionNotifier;

import com.sun.cldc.io.BluetoothProtocol;
import com.sun.cldc.io.BluetoothUrl;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * Provides "btl2cap" protocol implementation
 */
public class Protocol extends BluetoothProtocol {
	/** Keeps maximum MTU supported by the BT stack. */
	static final int MAX_STACK_MTU;

	static {
		int maxReceiveMTU;
		try {
			maxReceiveMTU = Integer.parseInt(System.getProperty("bluetooth.l2cap.receiveMTU.max"));
		} catch (NumberFormatException e) {
			maxReceiveMTU = L2CAPConnection.DEFAULT_MTU;
		}
		MAX_STACK_MTU = maxReceiveMTU;
	}

	/**
	 * Constructs an instance.
	 */
	public Protocol() {
		super(BluetoothUrl.L2CAP);
	}

	/**
	 * Cheks permissions and opens requested connection.
	 *
	 * @param token security token passed by calling class
	 * @param url <code>BluetoothUrl</code> instance that defines required 
	 *        connection stringname the URL without protocol name and colon
	 * @param mode Connector.READ_WRITE or Connector.READ or Connector.WRITE
	 *
	 * @return a notifier in case of server connection string, open connection
	 * in case of client one.
	 *
	 * @exception IOException if opening connection fails.
	 */
	public Connection openPrim(Object token, BluetoothUrl url, int mode) throws IOException {
		return openPrimImpl(token, url, mode);
	}

	/** 
	 * Ensures URL parameters have valid values. Sets receiveMTU if undefined.
	 * @param url URL to check
	 * @exception IllegalArgumentException if invalid url parameters found
	 * @exception BluetoothConnectionException if url parameters are not 
	 *            acceptable due to Bluetooth stack limitations
	 */
	protected void checkUrl(BluetoothUrl url) throws IllegalArgumentException, BluetoothConnectionException {

		if (url.receiveMTU == -1) {
			url.receiveMTU = L2CAPConnection.DEFAULT_MTU;
		}

		if (url.isSystem()) {
			return;
		}

		super.checkUrl(url);

		if (!url.isServer
				&& (url.port <= 0x1000 || url.port >= 0xffff || ((url.port & 1) != 1) || ((url.port & 0x100) != 0))) {
			throw new IllegalArgumentException("Invalid PSM: " + url.port);
		}

		// IMPL_NOTE BluetoothConnectionException should be thrown here
		// It is temporary substituted by IllegalArgumentException 
		// to pass TCK succesfully. To be changed back when fixed
		// TCK arrives. The erroneous TCK test is 
		// javasoft.sqe.tests.api.javax.bluetooth.Connector.L2Cap.
		//                             openClientTests.L2Cap1014()
		//
		// Correct code here is
		// throw new BluetoothConnectionException(
		//    BluetoothConnectionException.UNACCEPTABLE_PARAMS,
		//    <message>);
		if (url.receiveMTU < L2CAPConnection.MINIMUM_MTU) {
			throw new IllegalArgumentException("Receive MTU is too small");
		}

		if (url.receiveMTU > MAX_STACK_MTU) {
			throw new IllegalArgumentException("Receive MTU is too large");
		}

		if (url.transmitMTU != -1 && url.transmitMTU > MAX_STACK_MTU) {
			throw new BluetoothConnectionException(BluetoothConnectionException.UNACCEPTABLE_PARAMS,
					"Transmit MTU is too large");
		}
	}

	/**
	 * Ensures that permissions are proper and creates client side connection.
	 * @param token security token if passed by caller, or <code>null</code>
	 * @param mode       I/O access mode
	 * @return proper <code>L2CAPConnectionImpl</code> instance
	 * @exception IOException if openning connection fails.
	 */
	protected Connection clientConnection(SecurityToken token, int mode) throws IOException {
		checkForPermission(token, Permissions.BLUETOOTH_CLIENT);

//		Short psmShort = Short.decode(url.uuid);
//		short psm = psmShort.shortValue();
//		Long remoteAddr = Long.decode(url.address);
//		long remoteAddrLong = remoteAddr.longValue();
		
		short psm = Short.parseShort(url.uuid, 16);
		long remoteAddrLong = Long.parseLong(url.address, 16);

		try {
			BluetoothStack bluetooth = BluetoothStack.getBluetoothStack();
			LocalDevice localDev = LocalDevice.getLocalDevice();
			DiscoveryAgent discovery = localDev.getDiscoveryAgent();
			RemoteDevice remoteDevice = discovery.getRemoteDevice(remoteAddrLong);
			if (remoteDevice != null) {
				JSR82Channel channel = new JSR82Channel();
				bluetooth.connectL2CAPChannel(channel, remoteDevice, psm);
				return channel;
			} else {
				throw new IllegalArgumentException("Unable to locate Bluetooth Device.");
			}
		} catch (HCIException e) {
			throw new IOException(e.getMessage());
		}
		

		//      catch (BluetoothStateException e) { throw new IOException("" + e); }
		//      catch (HCIException e) { throw new IOException("" + e); }

		//        byte[] bdAddrBytes        = new byte[12];
		//        String bdAddrString       = url.substring(10, 22);
		//        Long bdAddrLong           = Long.decode("0x" + bdAddrString);
		//        long remoteAddrLong       = bdAddrLong.longValue();
		//        int endIndex              = url.indexOf(';', 22);
		//        String psmString          = url.substring(23, endIndex);
		//        Short psmShort            = Short.decode(psmString);
		//        short psm                 = psmShort.shortValue();
		//        BluetoothStack bluetooth  = BluetoothStack.getBluetoothStack();
		//        LocalDevice localDev      = LocalDevice.getLocalDevice();
		//        DiscoveryAgent discovery  = localDev.getDiscoveryAgent();
		//        RemoteDevice remoteDevice = discovery.getRemoteDevice(remoteAddrLong);
		//        if (remoteDevice != null) {
		//            JSR82Channel channel = new JSR82Channel();
		//            bluetooth.connectL2CAPChannel(channel, remoteDevice, psm);
		//            return channel;
		//        }

		//return new L2CAPConnectionImpl(url, mode);
	}

	/**
	 * Ensures that permissions are proper and creates required notifier at 
	 * server side.
	 * @param token security token if passed by caller, or <code>null</code>
	 * @param mode       I/O access mode
	 * @return proper <code>L2CAPNotifierImpl</code> instance
	 * @exception IOException if openning connection fails
	 */
	protected Connection serverConnection(SecurityToken token, int mode) throws IOException {
		checkForPermission(token, Permissions.BLUETOOTH_SERVER);

		short psm = (short) url.port;
		return new JSR82ConnectionNotifier(psm);

		//        int endIndex     = url.indexOf(';');
		//        String psmString = url.substring(20, endIndex);
		//        Short psmShort   = Short.decode(psmString);
		//        short psm        = psmShort.shortValue();
		//        return new JSR82ConnectionNotifier(psm);

		//return new L2CAPNotifierImpl(url, mode);
	}
}
