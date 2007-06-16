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
* Created on May 23, 2003
* by Christian Lorenz
*
*/

package org.javabluetooth.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.bluetooth.RemoteDevice;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPSender;
import org.javabluetooth.util.Debug;

/** 
 * This implementation of <code>BluetoothStack</code> connects via TCP
 * to a <code>BluetoothTCPServer</code>. This server provides the interface
 * to a remote <code>HCITransport</code> and allows multiple <code>BluetoothTCPClient</code> instances to
 * share one Bluetooth Device.
 * @see org.javabluetooth.stack.BluetoothStack
 * @see org.javabluetooth.stack.server.BluetoothTCPServer
 * @author Christian Lorenz
 */
public class BluetoothTCPClient extends BluetoothStack implements L2CAPSender, Runnable {
    //private static final byte PACKET_TYPE_ACL = 0x02;
    private static final byte PACKET_TYPE_EVENT                      = 0x04;
    private static final byte HCI_EVENT_INQUIRY_COMPLETE             = 0x01;
    private static final byte HCI_EVENT_INQUIRY_RESULT               = 0x02;
    private static final byte HCI_EVENT_REMOTE_NAME_REQUEST_COMPLETE = 0x07;
    private static final byte HCI_EVENT_COMMAND_COMPLETE             = 0x0E;
    private static final byte HCI_EVENT_COMMAND_STATUS               = 0x0F;
    private static final byte L2CAP_CREATE_CONNECTION_REQUEST        = (byte)0xf0;
    private static final byte L2CAP_CREATE_CONNECTION_RESPONSE       = (byte)0xf1;
    private static final byte L2CAP_DISCONNECT_CHANNEL_REQUEST       = (byte)0xf2;
    private static final byte SDP_REGISTER_SERVICE_REQUEST           = (byte)0xf3;
    private static final byte L2CAP_PACKET                           = (byte)0xff;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private boolean isConnected;
    private byte[] commandResponse;
    private short commandResponseOpCode;
    private L2CAPChannel[] channels;

    /**
     *	Connects to the <code>BluetoothTCPServer</code> at <code>remoteAddress</code>
     * and starts a new <code>Thread</code> receiving and parsing Event Packets.
     * @param remoteAddress
     * @param remotePort
     * @throws HCIException
     */
    public BluetoothTCPClient(String remoteAddress, int remotePort) throws HCIException {
        try {
            channels = new L2CAPChannel[16];
            socket = new Socket(remoteAddress, remotePort);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            isConnected = true;
            Thread thisThread = new Thread(this);
            thisThread.start();
        }
        catch (UnknownHostException e) {
            throw new HCIException("HCIManagerRemoteClient: Unknown Host " + remoteAddress + ":" + remotePort + ". " + e);
        }
        catch (IOException e) { throw new HCIException("HCIManagerRemoteClient: IOException: " + e); }
    }

    /** @see org.javabluetooth.stack.BluetoothStack#send_HCI_Command_Packet(byte[]) */
    public byte[] send_HCI_Command_Packet(byte[] cmdPacket) throws HCIException {
        short opCode = (short)((cmdPacket[2] << 8) | (cmdPacket[1] & 0xff));
        while (commandResponse != null) {
            try { this.wait(100); }
            catch (InterruptedException e) { }
        }
        commandResponseOpCode = opCode;
        try {
            Debug.println(6, "BluetoothTCPClient: Sending HCI Command:", cmdPacket);
            socketOut.write(cmdPacket);
            socketOut.flush();
        }
        catch (IOException e) {
            cleanExit();
            throw new HCIException("IOException: " + e);
        }
        int timer = 0;
        while (commandResponse == null) {
            try {
                Thread.sleep(500);
                timer++;
                if (timer == 100) { throw new HCIException("Command Packet Response Timed Out. "); }
            }
            catch (InterruptedException e) { }
        }
        byte[] result = commandResponse;
        commandResponseOpCode = 0;
        commandResponse = null;
        return result;
    }

    /**
     * Run Loop of the Client Thread. This loop reads data from the socket
     * and parses it into Event Packets which are then dispatched to their proper receive methods.
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            byte[] headerBuffer     = new byte[3];
            short headerBufferIndex = 0;
            byte[] packetBuffer     = { };
            int packetBufferIndex   = 0;
            while (isConnected) {
                byte[] incomingBytes = new byte[32];
                int incomingLength = socketIn.read(incomingBytes);
                //Debug.println("read"+Debug.printByteArray(incomingBytes));
                if (incomingLength == -1) break;
                int incomingBytesIndex = 0;
                while (incomingBytesIndex < incomingLength) //process all received bytes
                {
                    if (headerBufferIndex < headerBuffer.length) //header is still incomplete
                    {
                        int length = headerBuffer.length - headerBufferIndex;
                        if (length > incomingLength - incomingBytesIndex) length = incomingLength - incomingBytesIndex;
                        System.arraycopy(incomingBytes, incomingBytesIndex, headerBuffer, headerBufferIndex, length);
                        incomingBytesIndex += length;
                        headerBufferIndex += length;
                        if (headerBufferIndex == headerBuffer.length)
                        //header is complete
                        { //creates Packet and copy header into packet.
                            switch (headerBuffer[0]) {
                                case L2CAP_CREATE_CONNECTION_RESPONSE:
                                case L2CAP_DISCONNECT_CHANNEL_REQUEST:
                                case L2CAP_PACKET:
                                    packetBuffer = new byte[3 + (short)
                                        (((short)headerBuffer[2] & 0xff) << 8 | ((short)headerBuffer[1] & 0xff))];
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
                                    incomingBytesIndex -= (headerBuffer.length - 1);
                                    System.err.println("BluetoothTCPClient: Received Invalid Packet Header" + headerBuffer);
                            }
                        }
                    }
                    if (headerBufferIndex == headerBuffer.length) //header is complete. copy data to packet.
                    {
                        int length = packetBuffer.length - packetBufferIndex;
                        if (length > incomingLength - incomingBytesIndex) length = incomingLength - incomingBytesIndex;
                        System.arraycopy(incomingBytes, incomingBytesIndex, packetBuffer, packetBufferIndex, length);
                        incomingBytesIndex += length;
                        packetBufferIndex += length;
                        if (packetBufferIndex == packetBuffer.length) { //packet is complete
                            dispatchPacket(packetBuffer);
                            headerBufferIndex = 0;
                        }
                    }
                }
            }
        }
        catch (IOException e) { System.err.println("BluetoothTCPClient: IOException: " + e); }
        cleanExit();
    }

    /** Closes Sockets cleanly and causes the run() method to exit properly. Also changes all L2CAPChannels to CLOSED. */
    private void cleanExit() {
        Debug.println(6, "BluetoothTCPClient: Disconnecting.");
        isConnected = false;
        for (int i = 0; i < channels.length; i++) {
            if (channels[i] != null) {
                channels[i].channelState = L2CAPChannel.CLOSED;
                channels[i].wasDisconnected();
            }
        }
        try { socketIn.close(); }
        catch (IOException e) { }
        try { socketOut.close(); }
        catch (IOException e) { }
        try { socket.close(); }
        catch (IOException e) { }
    }

    /** Dispatches Event Packets to their proppe receive methods. */
    private void dispatchPacket(byte[] packet) {
        //Debug.println(7,"BluetoothTCPClient: Received: ",packet);
        switch (packet[0]) //Packet Type
        {
            case PACKET_TYPE_EVENT:
                switch (packet[1]) //Event Type
                {
                    case HCI_EVENT_INQUIRY_COMPLETE:
                        Debug.println(6, "BluetoothTCPClient: Received HCI Inquiry Complete Event:", packet);
                        receive_HCI_Event_Inquiry_Complete(packet);
                        break;
                    case HCI_EVENT_INQUIRY_RESULT:
                        Debug.println(6, "BluetoothTCPClient: Received HCI Inquiry Result Event:", packet);
                        receive_HCI_Event_Inquiry_Result(packet);
                        break;
                    case HCI_EVENT_REMOTE_NAME_REQUEST_COMPLETE:
                        Debug.println(6, "BluetoothTCPClient: Received HCI Remote Name Request Complete Event:", packet);
                        receive_HCI_Event_Remote_Name_Request_Complete(packet);
                        break;
                    case HCI_EVENT_COMMAND_COMPLETE:
                        Debug.println(6, "BluetoothTCPClient: Received HCI Command Complete Event:", packet);
                        receive_HCI_Event_Command_Complete(packet);
                        break;
                    case HCI_EVENT_COMMAND_STATUS:
                        Debug.println(6, "BluetoothTCPClient: Received HCI Command Status Event:", packet);
                        receive_HCI_Event_Command_Status(packet);
                        break;
                    default:
                        System.err.println("BluetoothTCPClient: Received Unknown HCI Event Packet:" +
                            Debug.printByteArray(packet));
                }
                break;
            case(byte)L2CAP_CREATE_CONNECTION_RESPONSE: {
                    Debug.println(6, "BluetoothTCPClient: Received L2CAP Create Connection Response:", packet);
                    receive_L2CAP_Create_Connection_Response(packet);
                    break;
                }
            case(byte)L2CAP_DISCONNECT_CHANNEL_REQUEST: {
                    Debug.println(6, "BluetoothTCPClient: Received L2CAP Disconnect Channel Request:", packet);
                    receive_L2CAP_Disconnect_Channel_Request(packet);
                    break;
                }
            case(byte)L2CAP_PACKET: {
                    Debug.println(6, "BluetoothTCPClient: Received L2CAP Packet:", packet);
                    receive_L2CAP_Packet(packet);
                    break;
                }

                /*case PACKET_TYPE_ACL :
                //Debug.println("HCITransport: Received Data Packet:" + Debug.printByteArray(hciPacket));
                //receive_HCI_Data_Packet(packet);
                break; */

            default:
                System.err.println("BluetoothTCPClient: Received Packet of unknown Type: " + Debug.printByteArray(packet));
        }
    }

    /**
     * Parses Command Complete Event Packet and makes the Command Response available to the
     * <code>send_HCI_Command_Packet(byte[])</code> method.
     */
    private synchronized void receive_HCI_Event_Command_Complete(byte[] packetData) {
        short opCode = (short)((packetData[5] << 8) | (packetData[4] & 0xff));
        //Debug.println("Command Complete Event: " + opCode);
        while (commandResponse != null) {
            try { this.wait(100); }
            catch (InterruptedException e) { }
        }
        if (opCode == commandResponseOpCode) { commandResponse = packetData; }
    }

    /**
     * Parses Command Status Event Packet and makes the Command Response available to the
     * <code>send_HCI_Command_Packet(byte[])</code> method.
     */
    private synchronized void receive_HCI_Event_Command_Status(byte[] packetData) {
        short opCode = (short)((packetData[6] << 8) | (packetData[5] & 0xff));
        //Debug.println("Command Status Event: " + opCode);
        while (commandResponse != null) {
            try { this.wait(100); }
            catch (InterruptedException e) { }
        }
        if (opCode == commandResponseOpCode) { commandResponse = packetData; }
    }

    private void receive_L2CAP_Create_Connection_Response(byte[] packet) {
        short channelHandel  = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        byte channelState    = packet[5];
        short localCID       = (short)((((short)packet[6]) & 0xff) | (((short)packet[7]) & 0xff) << 8);
        short remoteCID      = (short)((((short)packet[8]) & 0xff) | (((short)packet[9]) & 0xff) << 8);
        L2CAPChannel channel = channels[channelHandel];
        if (channel != null) {
            channel.channelState = channelState;
            channel.localChannelID = localCID;
            channel.remoteChannelID = remoteCID;
        }
    }

    private void receive_L2CAP_Disconnect_Channel_Request(byte[] packet) {
        short channelHandel = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        L2CAPChannel channel = channels[channelHandel];
        channels[channelHandel] = null;
        if (channel != null) {
            channel.channelState = L2CAPChannel.CLOSED;
            channel.wasDisconnected();
        }
    }

    private void receive_L2CAP_Packet(byte[] packet) {
        short channelHandel  = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        int length           = ((packet[1] & 0xff) | (packet[2] & 0xff) << 8) - 2;
        L2CAPChannel channel = channels[channelHandel];
        if (channel != null) {
            byte[] l2capPacket = new byte[length];
            System.arraycopy(packet, 5, l2capPacket, 0, l2capPacket.length);
            channel.receiveL2CAPPacket(l2capPacket);
        }
    }

    /**
     * @see org.javabluetooth.stack.BluetoothStack#connectL2CAPChannel(org.javabluetooth.stack.l2cap.L2CAPChannel,
     * javax.bluetooth.RemoteDevice, short)
     */
    public void connectL2CAPChannel(L2CAPChannel channel, RemoteDevice remoteDevice, short psm) throws HCIException {
        if (!isConnected) throw new HCIException("BluetoothTCPClient is not connected.");
        short channelHandle = -1;
        for (int i = 0; i < channels.length; i++) {
            if (channels[i] == null) {
                channels[i] = channel;
                channel.l2capSender = this;
                channel.remoteAddress = remoteDevice.bdAddrLong;
                channelHandle = (short)i;
                break;
            }
        }
        if (channelHandle == -1) throw new HCIException("Connect L2CAPChannel failed. No Open Channel Slots.");
        byte[] createL2CAPChannelRequest = {
            L2CAP_CREATE_CONNECTION_REQUEST, 0x0e, 0x00, //fixed length 14
            (byte)((channelHandle) & 0xff), (byte)((channelHandle >> 8) & 0xff), (byte)((psm) & 0xff), (byte)((psm >> 8) & 0xff),
                (byte)((remoteDevice.bdAddrLong) & 0xff), (byte)((remoteDevice.bdAddrLong >> 8) & 0xff),
                (byte)((remoteDevice.bdAddrLong >> 16) & 0xff), (byte)((remoteDevice.bdAddrLong >> 24) & 0xff),
                (byte)((remoteDevice.bdAddrLong >> 32) & 0xff), (byte)((remoteDevice.bdAddrLong >> 40) & 0xff),
                remoteDevice.pageScanRepMode, remoteDevice.pageScanMode, (byte)((remoteDevice.clockOffset) & 0xff),
                (byte)((remoteDevice.clockOffset >> 8) & 0xff)
        };
        try {
            Debug.println(6, "BluetoothTCPClient: Sending L2CAP Create Connection Request:", createL2CAPChannelRequest);
            socketOut.write(createL2CAPChannelRequest);
            socketOut.flush();
        }
        catch (IOException e) {
            cleanExit();
            throw new HCIException("IOException: " + e);
        }
        int timeout = 0;
        while (channel.channelState == L2CAPChannel.CLOSED) {
            try {
                Thread.sleep(1000);
                timeout++;
            }
            catch (InterruptedException e) { }
            if (timeout == 100) throw new HCIException("Connect L2CAPChannel timed out.");
        }
        if (channel.channelState == L2CAPChannel.FAILED) throw new HCIException("Connect L2CAPChannel failed.");
    }

    /** @see org.javabluetooth.stack.l2cap.L2CAPSender#sendL2CAPPacket(long, short, byte[]) */
    public void sendL2CAPPacket(L2CAPChannel channel, byte[] packet) throws IOException {
        byte channelHandel = -1;
        for (int i = 0; i < channels.length; i++) { if (channels[i] == channel) channelHandel = (byte)i; }
        if (channelHandel == -1) throw new IOException("Unable to send to channel.");
        byte[] l2capPacket = new byte[5 + packet.length];
        l2capPacket[0] = L2CAP_PACKET;
        l2capPacket[1] = (byte)((packet.length + 2) & 0xff);
        l2capPacket[2] = (byte)((packet.length + 2 >> 8) & 0xff);
        l2capPacket[3] = (byte)((channelHandel) & 0xff);
        l2capPacket[4] = (byte)((channelHandel >> 8) & 0xff);
        System.arraycopy(packet, 0, l2capPacket, 5, packet.length);
        try {
            Debug.println(6, "BluetoothTCPClient: Sending L2CAP Packet:", l2capPacket);
            socketOut.write(l2capPacket);
            socketOut.flush();
        }
        catch (IOException e) {
            cleanExit();
            throw e;
        }
    }

    /** @see org.javabluetooth.stack.l2cap.L2CAPSender#closeL2CAPChannel(org.javabluetooth.stack.l2cap.L2CAPChannel) */
    public void closeL2CAPChannel(L2CAPChannel channel) {
        byte channelHandel = -1;
        for (int i = 0; i < channels.length; i++) { if (channels[i] == channel) channelHandel = (byte)i; }
        if (channelHandel != -1) {
            byte[] l2capPacket = {
                L2CAP_DISCONNECT_CHANNEL_REQUEST, 0x02, 0x00, (byte)((channelHandel) & 0xff),
                    (byte)((channelHandel >> 8) & 0xff)
            };
            try {
                Debug.println(6, "BluetoothTCPClient: Sending L2CAP Disconnect Channel Request:", l2capPacket);
                socketOut.write(l2capPacket);
                socketOut.flush();
            }
            catch (IOException e) { cleanExit(); }
        }
    }

    /**
     * @see org.javabluetooth.stack.BluetoothStack#openL2CAPService(org.javabluetooth.stack.l2cap.L2CAPChannel, short,
     * short, byte[])
     */
    public void registerL2CAPService(L2CAPChannel channel, short serviceUUID, short browseUUID,
        byte[] serviceRecord) throws HCIException {
            if (!isConnected) throw new HCIException("BluetoothTCPClient is not connected.");
            short channelHandle = -1;
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] == null) {
                    channels[i] = channel;
                    channel.l2capSender = this;
                    channelHandle = (short)i;
                    break;
                }
            }
            if (channelHandle == -1) throw new HCIException("Register SDP Service failed. No Open Channel Slots.");
            short length = (short)(6 + serviceRecord.length);
            byte[] registerL2CAPServiceRequest = new byte[3 + length];
            registerL2CAPServiceRequest[0] = SDP_REGISTER_SERVICE_REQUEST;
            registerL2CAPServiceRequest[1] = (byte)((length) & 0xff);
            registerL2CAPServiceRequest[2] = (byte)((length >> 8) & 0xff);
            registerL2CAPServiceRequest[3] = (byte)((channelHandle) & 0xff);
            registerL2CAPServiceRequest[4] = (byte)((channelHandle >> 8) & 0xff);
            registerL2CAPServiceRequest[5] = (byte)((serviceUUID) & 0xff);
            registerL2CAPServiceRequest[6] = (byte)((serviceUUID >> 8) & 0xff);
            registerL2CAPServiceRequest[7] = (byte)((browseUUID) & 0xff);
            registerL2CAPServiceRequest[8] = (byte)((browseUUID >> 8) & 0xff);
            System.arraycopy(serviceRecord, 0, registerL2CAPServiceRequest, 9, serviceRecord.length);
            try {
                Debug.println(6, "BluetoothTCPClient: Sending SDP Register Service Request:", registerL2CAPServiceRequest);
                socketOut.write(registerL2CAPServiceRequest);
                socketOut.flush();
            }
            catch (IOException e) {
                cleanExit();
                throw new HCIException("IOException: " + e);
            }
    }
}

