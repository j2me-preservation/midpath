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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javabluetooth.stack.l2cap.L2CAPLink;
import org.javabluetooth.util.Debug;

/** 
 * This interface is implemented by Drivers for specific HCI Transports.
 * Right now only UART is supported, but this interface will allow future drivers for other HCI Transports such as USB.
 * @see org.javabluetooth.stack.hci.UARTTransport
 * @author Christian Lorenz
 */
public abstract class HCIDriver {
    private static final byte PACKET_TYPE_ACL                        = 0x02;
    private static final byte PACKET_TYPE_EVENT                      = 0x04;
    private static final byte HCI_EVENT_INQUIRY_COMPLETE             = 0x01;
    private static final byte HCI_EVENT_INQUIRY_RESULT               = 0x02;
    private static final byte HCI_EVENT_CONNECTION_COMPLETE          = 0x03;
    private static final byte HCI_EVENT_DISCONNECTION_COMPLETE       = 0x05;
    private static final byte HCI_EVENT_REMOTE_NAME_REQUEST_COMPLETE = 0x07;
    private static final byte HCI_EVENT_COMMAND_COMPLETE             = 0x0E;
    private static final byte HCI_EVENT_COMMAND_STATUS               = 0x0F;
    private static final byte HCI_EVENT_NUMBER_OF_COMPLETED_PACKETS  = 0x13;
    private static HCIDriver hciDriver;
    private byte[] commandResponse;
    private short commandResponseOpCode = 0;
    private Hashtable connectionHandels = new Hashtable();
    private Hashtable remoteAddresses   = new Hashtable();
    private Vector hciReceivers         = new Vector();
    private byte[] headerBuffer         = new byte[5];
    private short headerBufferIndex     = 0;
    private byte[] packetBuffer;
    private int packetBufferIndex;

    public static void init(HCIDriver hciTransport) { HCIDriver.hciDriver = hciTransport; }

    public static HCIDriver getHCIDriver() throws HCIException {
        if (hciDriver == null) throw new HCIException("HCITransport not initalized. ");
        return hciDriver;
    }

    public void registerHCIReceiver(HCIReceiver receiver) {
        if (!hciReceivers.contains(receiver)) hciReceivers.addElement(receiver);
    }

    public void unregisterHCIReceiver(HCIReceiver receiver) { hciReceivers.removeElement(receiver); }

    public void registerL2CAPLink(L2CAPLink link) {
        Short handle = new Short(link.connectionHandle);
        connectionHandels.put(handle, link);
        Long remoteAddress = new Long(link.remoteAddress);
        remoteAddresses.put(remoteAddress, link);
    }

    public void unregisterL2CAPLink(L2CAPLink link) {
        connectionHandels.remove(new Short(link.connectionHandle));
        remoteAddresses.remove(new Long(link.remoteAddress));
    }

    public L2CAPLink getL2CAPLink(long remoteAddress, byte pageScanRepMode, byte pageScanMode,
        short clockOffset) throws HCIException {
            Long remoteAddressLong = new Long(remoteAddress);
            L2CAPLink link = (L2CAPLink)remoteAddresses.get(remoteAddressLong);
            if (link == null) {
                byte connResult = send_HCI_LC_Create_Connection(remoteAddress, (short)0x8000, pageScanRepMode, pageScanMode,
                    clockOffset, (byte)0x01);
                if (connResult != 0) throw new HCIException("Create Connection failed. (" + connResult + ")");
                short timeout = 0;
                while (link == null) {
                    try {
                        Thread.sleep(1000);
                        timeout++;
                    }
                    catch (InterruptedException e) { }
                    if (timeout == 50) throw new HCIException("Create Connection timed out.");
                    link = (L2CAPLink)remoteAddresses.get(remoteAddressLong);
                }
            }
            return link;
    }

    public synchronized byte[] send_HCI_Command_Packet(byte[] cmdPacket) throws HCIException {
        short opCode = (short)((cmdPacket[2] << 8) | (cmdPacket[1] & 0xff));
        Debug.println(1, "HCI: Sending Command: " + opCode);
        while (commandResponse != null) {
            try { this.wait(100); }
            catch (InterruptedException e) { }
        }
        commandResponseOpCode = opCode;
        hciDriver.sendPacket(cmdPacket);
        //int timer=0;
        while (commandResponse == null) {
            try {
                this.wait(500);

                /*timer++;
                if(timer==50)
                {	timer=0;
                Debug.println("HCI: Resending Command with opCode:" + opCode + ")");
                hciTransport.sendPacket(cmdPacket);
                }*/
            }
            catch (InterruptedException e) { }
        }
        byte[] result = commandResponse;
        commandResponseOpCode = 0;
        commandResponse = null;

        /* The Brainboxes Bluetooth Host Controller seems to have a bug
        * which causes the module to slow down to an unacceptable speed
        * when an Inquiry Complete Event is send. This is a cheap hack to
        * surpress the sending of this event by simply sending a Cancel
        * Inquiry Command before it naturally Completes.
        * before it completes.
        */

        if ((cmdPacket[1] == 0x01) && (cmdPacket[2] == 0x04)) {
            try { wait(10000); }
            catch (InterruptedException e) { }
            byte[] cancelInquiryPacket = { 0x01, 0x02, 0x04, 0x00 };
            short cancelOpCode = (short)((cancelInquiryPacket[2] << 8) | (cancelInquiryPacket[1] & 0xff));
            while (commandResponse != null) {
                try { wait(100); }
                catch (InterruptedException e) { }
            }
            commandResponseOpCode = cancelOpCode;
            hciDriver.sendPacket(cancelInquiryPacket);
            while (commandResponse == null) {
                try { wait(100); }
                catch (InterruptedException e) { }
            }
            commandResponseOpCode = 0;
            commandResponse = null;
            byte[] dummyPacket = { PACKET_TYPE_EVENT, HCI_EVENT_INQUIRY_COMPLETE, (byte)0x01, (byte)0x00 };
            receive_HCI_Event_Inquiry_Complete(dummyPacket);
        }

        /* end of Hack */

        return result;
    }

    public synchronized void send_HCI_Data_Packet(byte[] dataPacket) throws HCIException {
        Debug.println(1, "HCI: Sending Data Packet.");
        sendPacket(dataPacket);
    }

    /**
     * This command will cause the Link Manager to create a connection
     * to the Bluetooth device with the BD_ADDR specified by the command
     * parameters. It is triggered by <code>HCITransport.getL2CAPLink()</code>.  For details see Page 568 of the Bluetooth
     * Core Specification Version 1.1
     * @param bd_addr
     * @param packetType
     * @param clockOffset
     * @param allowRoleSwitching
     * @return return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws HCIException
     */
    public byte send_HCI_LC_Create_Connection(long bd_addr, short packetType, byte pageScanRepMode, byte pageScanMode,
        short clockOffset, byte allowRoleSwitching) throws HCIException {
            byte[] data = {
                0x01, 0x05, 0x04, 0x0d, (byte)((bd_addr) & 0xff), (byte)((bd_addr >> 8) & 0xff), (byte)((bd_addr >> 16) & 0xff),
                    (byte)((bd_addr >> 24) & 0xff), (byte)((bd_addr >> 32) & 0xff), (byte)((bd_addr >> 40) & 0xff), (byte)((packetType) & 0xff),
                    (byte)((packetType >> 8) & 0xff), (byte)pageScanRepMode, // Page scan repetition mode 1 byte
                    (byte)pageScanMode, // Page scan mode 1 byte
                    (byte)((clockOffset) & 0xff), // Clock offset 2 bytes
                    (byte)((clockOffset >> 8) & 0xff), (byte)allowRoleSwitching
            }; // Allow role switch
            byte[] resultData = send_HCI_Command_Packet(data);
            return resultData[3];
    }

    /**
     * The Disconnection command is used to terminate an existing connection.
     * The Connection Handle indicates which connection is to be disconnected.
     * It is triggered by <code>L2CAPLink.close()</code>. When the Host Controller receives the Disconnect command, it sends
     * the Command Status event to the Host. The Disconnection Complete
     * event will occur at each Host when the termination of the connection has
     * completed, and indicates that this command has been completed.
     * For details see Page 571 of the Bluetooth Core Specification Version 1.1
     * @param connectionHandle Connection Handle for the connection being disconnected.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws HCIException
     */
    public byte send_HCI_LC_Disconnect(short connectionHandle) throws HCIException {
        byte[] data =
            { 0x01, 0x06, 0x04, 03, (byte)((connectionHandle) & 0xff), (byte)((connectionHandle >> 8) & 0xff), 0x13 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[3];
    }

    /**
     * The Reset command will reset the Host Controller and the
     * Link Manager. After the reset is completed, the current operational state will be lost, the Bluetooth device will enter
     * standby mode and the Host Controller will automatically revert
     * to the default values for the parameters for which default values are defined in the specification.
     * For details see Page 622 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws HCIException
     */
    public byte send_HCI_HC_Reset() throws HCIException {
        byte[] data = { 0x01, 0x03, 0x0C, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Sends an HCIPacket. The Packet bytes are send over the Transport via the
     * underlying hardware to the Bluetooth Host Controller.
     * @param packet HCI Command or Data Packet.
     * @throws HCIException
     */
    protected abstract void sendPacket(byte[] packet) throws HCIException;

    /**
     * Resets the Bluetooth Host Controller.
     * @throws HCIException
     */
    protected abstract void reset() throws HCIException;

    protected void receiveData(byte[] incomingBytes, int incomingLength) {
        int incomingBytesIndex = 0;
        while (incomingBytesIndex < incomingLength) //process all available data
        {
            if (headerBufferIndex < headerBuffer.length) //header is still incomplete
            {
                int length = headerBuffer.length - headerBufferIndex;
                if (length > incomingLength - incomingBytesIndex) length = incomingLength - incomingBytesIndex;
                System.arraycopy(incomingBytes, incomingBytesIndex, headerBuffer, headerBufferIndex, length);
                incomingBytesIndex += length;
                headerBufferIndex += length;
                if (headerBufferIndex == headerBuffer.length) //header is complete
                {
                    parseHeader(); //creates Packet and copy header into packet.
                }
            }
            else //header is complete. copy data to packet.
            {
                int length = packetBuffer.length - packetBufferIndex;
                if (length > incomingLength - incomingBytesIndex) length = incomingLength - incomingBytesIndex;
                System.arraycopy(incomingBytes, incomingBytesIndex, packetBuffer, packetBufferIndex, length);
                incomingBytesIndex += length;
                packetBufferIndex += length;
                if (packetBufferIndex == packetBuffer.length) //packet is complete
                {
                    headerBufferIndex = 0; //this'll lead to a new packet creation
                    dispatchPacket(packetBuffer);
                }
            }
        }
    }

    private void parseHeader() {
        switch (headerBuffer[0]) {
            case PACKET_TYPE_ACL:
                packetBuffer = new byte[5 + (short)(((short)headerBuffer[4] & 0xff) << 8 | ((short)headerBuffer[3] & 0xff))];
                System.arraycopy(headerBuffer, 0, packetBuffer, 0, headerBuffer.length);
                packetBufferIndex = headerBuffer.length;
                break;
            case PACKET_TYPE_EVENT:
                packetBuffer = new byte[3 + (short)(((short)headerBuffer[2]) & 0xff)];
                System.arraycopy(headerBuffer, 0, packetBuffer, 0, headerBuffer.length);
                packetBufferIndex = headerBuffer.length;
                break;
            default:
                headerBufferIndex = 0; //reset packet parser
                System.err.println("HCI: Received Invalid Packet Header" + headerBuffer);
        }
        //Debug.println("HCI: Parsed Header ("+Debug.printByteArray(headerBuffer));
    }

    private void dispatchPacket(byte[] hciPacket) {
        Debug.println(0, "HCI: Received Packet: ", hciPacket);
        switch (hciPacket[0]) //Packet Type
        {
            case PACKET_TYPE_EVENT:
                switch (hciPacket[1]) //Event Type
                {
                    case HCI_EVENT_INQUIRY_COMPLETE:
                        receive_HCI_Event_Inquiry_Complete(hciPacket);
                        break;
                    case HCI_EVENT_INQUIRY_RESULT:
                        receive_HCI_Event_Inquiry_Result(hciPacket);
                        break;
                    case HCI_EVENT_COMMAND_COMPLETE:
                        receive_HCI_Event_Command_Complete(hciPacket);
                        break;
                    case HCI_EVENT_COMMAND_STATUS:
                        receive_HCI_Event_Command_Status(hciPacket);
                        break;
                    case HCI_EVENT_CONNECTION_COMPLETE:
                        receive_HCI_Event_Connection_Complete(hciPacket);
                        break;
                    case HCI_EVENT_DISCONNECTION_COMPLETE:
                        receive_HCI_Event_Disconnection_Complete(hciPacket);
                        break;
                    case HCI_EVENT_REMOTE_NAME_REQUEST_COMPLETE:
                        receive_HCI_Event_Remote_Name_Request_Complete(hciPacket);
                        break;
                    case HCI_EVENT_NUMBER_OF_COMPLETED_PACKETS:
                        break;
                    default:
                        System.err.println("HCI: Received Unknown Event Packet:" + Debug.printByteArray(hciPacket));
                }
                break;
            case PACKET_TYPE_ACL:
                //Debug.println("HCI: Received Data Packet:" + Debug.printByteArray(hciPacket));
                receive_HCI_Data_Packet(hciPacket);
                break;
            default:
                System.err.println("HCI: Received Packet of Unknown Type:" + Debug.printByteArray(hciPacket));
        }
    }

    private void receive_HCI_Event_Inquiry_Complete(byte[] eventPacket) {
        Debug.println(1, "HCI: Received Inquiry Complete Event.");
        Enumeration receivers = hciReceivers.elements();
        while (receivers.hasMoreElements()) {
            HCIReceiver receiver = (HCIReceiver)receivers.nextElement();
            receiver.receive_HCI_Event_Inquiry_Complete(eventPacket);
        }
    }

    private void receive_HCI_Event_Inquiry_Result(byte[] eventPacket) {
        Debug.println(1, "HCI: Received Inquiry Result Event.");
        Enumeration receivers = hciReceivers.elements();
        while (receivers.hasMoreElements()) {
            HCIReceiver receiver = (HCIReceiver)receivers.nextElement();
            receiver.receive_HCI_Event_Inquiry_Result(eventPacket);
        }
    }

    private void receive_HCI_Event_Connection_Complete(byte[] eventPacket) {
        if (eventPacket[3] == 0) //is connected
        {
            L2CAPLink conn = new L2CAPLink(this, eventPacket);
            Debug.println(1, "HCI: Received Connection Complete Event: " + conn.remoteAddress);
        }
        else Debug.println(1, "HCI: Received Connection Complete Event: Create Connection failed.");
    }

    private void receive_HCI_Event_Disconnection_Complete(byte[] eventPacket) {
        short connectionHandle = (short)(((short)eventPacket[5] & 0x0f) << 8 | ((short)eventPacket[4] & 0xff));
        L2CAPLink link = (L2CAPLink)connectionHandels.remove(new Short(connectionHandle));
        if (link != null) {
            Debug.println(1, "HCI: Received Disconnection Complete Event: " + link.remoteAddress);
            link.wasDisconnected();
        }
        else Debug.println(1, "HCI: Received Disconnection Complete Event.");
    }

    private void receive_HCI_Event_Remote_Name_Request_Complete(byte[] eventPacket) {
        Debug.println(1, "HCI: Remote Name Request Complete Event.");
        Enumeration receivers = hciReceivers.elements();
        while (receivers.hasMoreElements()) {
            HCIReceiver receiver = (HCIReceiver)receivers.nextElement();
            receiver.receive_HCI_Event_Remote_Name_Request_Complete(eventPacket);
        }
    }

    private synchronized void receive_HCI_Event_Command_Complete(byte[] packetData) {
        //TODO catch Reset Event and reset rest of stack
        short opCode = (short)((packetData[5] << 8) | (packetData[4] & 0xff));
        Debug.println(1, "HCI: Received Command Complete Event: " + opCode);
        while (commandResponse != null) {
            try { wait(100); }
            catch (InterruptedException e) { }
        }
        if (opCode == commandResponseOpCode) { commandResponse = packetData; }
    }

    private synchronized void receive_HCI_Event_Command_Status(byte[] packetData) {
        short opCode = (short)((packetData[6] << 8) | (packetData[5] & 0xff));
        Debug.println(1, "HCI: Received Command Status Event: " + opCode);
        while (commandResponse != null) {
            try { wait(100); }
            catch (InterruptedException e) { }
        }
        if (opCode == commandResponseOpCode) { commandResponse = packetData; }
    }

    private void receive_HCI_Data_Packet(byte[] data) {
        short handle = (short)(((short)data[2] & 0x0f) << 8 | ((short)data[1] & 0xff));
        L2CAPLink link = (L2CAPLink)connectionHandels.get(new Short(handle));
        if (link != null) {
            link.receiveData(data);
            Debug.println(1, "HCI: Received Data Packet from " + link.remoteAddress);
        }
        else System.err.println("HCI: Unable to deliver Data Packet. No open link with handle " + handle + ". : " +
                Debug.printByteArray(data));
    }
}

