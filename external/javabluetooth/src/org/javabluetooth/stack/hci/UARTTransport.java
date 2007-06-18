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
* Created on May 21, 2003
* by Christian Lorenz
*
*/

package org.javabluetooth.stack.hci;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.javabluetooth.util.Debug;

/** 
 * HCITransport Implementation of the UART Serial Protocol.
 * @see org.javabluetooth.stack.hci.HCITransport
 * @author Christian Lorenz
 */
public class UARTTransport extends HCIDriver implements SerialPortEventListener {
    private InputStream inStream;
    private OutputStream outStream;
    private byte[] readBuffer;

    /**
     * Initialized the UARTTransport; opens the specified SerialPort.
     * @param commPortName SerialPort on which the Bluetooth Host Controller is connected.
     * @throws NoSuchPortException
     * @throws UnsupportedCommOperationException
     * @throws PortInUseException
     */
    public UARTTransport(String commPortName) throws HCIException {
        try {
            CommPortIdentifier commPort = CommPortIdentifier.getPortIdentifier(commPortName);
            if (commPort.getPortType() != CommPortIdentifier.PORT_SERIAL)
                throw new HCIException(commPortName + " is not a serial port. ");
            SerialPort serial = (SerialPort)commPort.open("Bluetooth", 10000);
            serial.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            inStream = serial.getInputStream();
            outStream = serial.getOutputStream();
            readBuffer = new byte[32];
            serial.addEventListener(this);
            serial.notifyOnDataAvailable(true);
            serial.notifyOnBreakInterrupt(true);
            serial.notifyOnCarrierDetect(true);
            serial.notifyOnCTS(true);
            serial.notifyOnFramingError(true);
            //serial.notifyOnOutputEmpty(true);
            //serial.notifyOnOverrunError(true);
            serial.notifyOnParityError(true);
            //reset();
        }
        catch (NoSuchPortException e) { throw new HCIException("Port " + commPortName + " not found. " + e); }
        catch (PortInUseException e) {
            throw new HCIException("Port " + commPortName + " is in use by another application. " + e);
        }
        catch (IOException e) { throw new HCIException("Error opening IOStreams. " + e); }
        catch (TooManyListenersException e) {
            throw new HCIException("Couldn't register SerialPortEventListener. Too many Listeners. " + e);
        }
        catch (UnsupportedCommOperationException e) { throw new HCIException("Unsupported Comm Operation. " + e); }
    }

    /**
     * Sends an bytes to the HCI Host Controller. The bytes are send via the SerialPort to the Bluetooth Host Controller.
     * @see org.javabluetooth.stack.hci.HCITransport#sendPacket(byte[])
     */
    public synchronized void sendPacket(byte[] packet) throws HCIException {
        try {
            Debug.println(0, "HCI: Sending Packet:", packet);
            outStream.write(packet);
            outStream.flush();
        }
        catch (IOException e) { throw new HCIException("IO Error while sending Packet. " + e); }
    }

    /**
     * Registers an Event Listener for incomming Data Packets.
     * @see org.javabluetooth.stack.hci.HCITransport#addListener(com.org.javabluetooth.stack.hci.ConnectionReceiver)
     */

    /**
     * Registers an Event Listener for incomming Event Packets.
     * @see org.javabluetooth.stack.hci.HCITransport#addListener(com.org.javabluetooth.stack.hci.ConnectionReceiver)
     */

    /**
     * The entry point for javax.comm.SerialPortEvents.
     * @see javax.comm.SerialPortEventListener#serialEvent(javax.comm.SerialPortEvent)
     */
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
                Debug.println(0, "UART: Serial Port Event: Break Interrupt\n");
                break;
            case SerialPortEvent.CD:
                Debug.println(0, "UART:: Serial Port Event: Carrier Detect\n");
                break;
            case SerialPortEvent.CTS:
                Debug.println(0, "UART: Serial Port Event: Clear to send\n");
                break;
            case SerialPortEvent.DSR:
                Debug.println(0, "UART: Serial Port Event: Data Set Ready\n");
                break;
            case SerialPortEvent.FE:
                Debug.println(0, "UART: Serial Port Event: Framing Error\n");
                break;
            case SerialPortEvent.OE:
                Debug.println(0, "UART: Serial Port Event: Overrun Error\n");
                break;
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                Debug.println(0, "UART: Serial Port Event: Output Buffer Empty\n");
                break;
            case SerialPortEvent.PE:
                Debug.println(0, "UART: Serial Port Event: Parity Error\n");
                break;
            case SerialPortEvent.RI:
                Debug.println(0, "UART: Serial Port Event: Ring Indicator\n");
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                processAvailableData();
                break;
        }
    }

    /**
     * Reads all available data from the serialPort InputStream
     * and passes it on to the HCITransportListener. Triggered by SerialPortEvent.DATA_AVAILABLE. If no HCITransportListener
     * is registered all data in the buffer will be discarded.
     */
    private void processAvailableData() {
        try {
            while (inStream.available() > 0) {
                int byteCount = inStream.read(readBuffer);
                receiveData(readBuffer, byteCount);
            }
        }
        catch (IOException e) { Debug.println(0, "UART: Error while reading available data. " + e); }
    }

    /** @see org.javabluetooth.stack.hci.HCITransport#reset() */
    public synchronized void reset() throws HCIException {
        byte[] data = { 0x01, 0x03, 0x0C, 0x00 };
        try {
            outStream.write(data);
            outStream.flush();
        }
        catch (IOException e) { throw new HCIException("UART Reset Failed. " + e); }
    }
}

