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
* Created on Jul 7, 2003
* by Christian Lorenz
*/

package org.javabluetooth.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.hci.HCIReceiver;
import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPLink;
import org.javabluetooth.stack.sdp.SDPServer;
import org.javabluetooth.util.Debug;

/** 
 * This thread manages a TCP Connection to a BluetoothTCPClient. It multipexes
 * HCI Commands supported by HCIReceiver and L2CAPChannels over this Connection.
 * @see org.javabluetooth.stack.hci.HCIReceiver
 * @see org.javabluetooth.stack.l2cap.L2CAPChannel
 * @see org.javabluetooth.stack.distributed.BluetoothTCPChannel
 * @see org.javabluetooth.stack.distributed.BluetoothTCPClient
 * @author Christian Lorenz
 */
public class BluetoothTCPServerThread extends Thread implements HCIReceiver {
    private static final byte HCI_COMMAND_PACKET               = 0x01;
    private static final byte L2CAP_CREATE_CONNECTION_REQUEST  = (byte)0xf0;
    private static final byte L2CAP_CREATE_CONNECTION_RESPONSE = (byte)0xf1;
    private static final byte L2CAP_DISCONNECT_CHANNEL_REQUEST = (byte)0xf2;
    private static final byte SDP_REGISTER_SERVICE_REQUEST     = (byte)0xf3;
    private static final byte L2CAP_PACKET                     = (byte)0xff;
    private HCIDriver hciTransport;
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private boolean isConnected;
    private L2CAPChannel[] channels;

    public BluetoothTCPServerThread(Socket socket, HCIDriver hciTransport) throws IOException {
        Debug.println(7, "BluetoothTCPServer: BluetoothTCPClient Connected.");
        channels = new L2CAPChannel[16];
        this.hciTransport = hciTransport;
        this.socket = socket;
        socketIn = socket.getInputStream();
        socketOut = socket.getOutputStream();
        isConnected = true;
        hciTransport.registerHCIReceiver(this);
        this.start();
    }

    public void run() {
        byte[] headerBuffer     = new byte[4];
        short headerBufferIndex = 0;
        byte[] packetBuffer     = { };
        int packetBufferIndex   = 0;
        try {
            while (isConnected) {
                byte[] incomingBytes = new byte[32];
                int incomingLength = socketIn.read(incomingBytes);
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
                        if (headerBufferIndex == headerBuffer.length) //header is complete
                        { //creates Packet and copy header into packet.
                            switch (headerBuffer[0]) {
                                case L2CAP_CREATE_CONNECTION_REQUEST:
                                case L2CAP_DISCONNECT_CHANNEL_REQUEST:
                                case SDP_REGISTER_SERVICE_REQUEST:
                                case L2CAP_PACKET:
                                    packetBuffer = new byte[3 + (short)
                                        (((short)headerBuffer[2] & 0xff) << 8 | ((short)headerBuffer[1] & 0xff))];
                                    System.arraycopy(headerBuffer, 0, packetBuffer, 0, headerBuffer.length);
                                    packetBufferIndex = headerBuffer.length;
                                    break;
                                case HCI_COMMAND_PACKET:
                                    packetBuffer = new byte[4 + (short)(headerBuffer[3] & 0xff)];
                                    System.arraycopy(headerBuffer, 0, packetBuffer, 0, headerBuffer.length);
                                    packetBufferIndex = headerBuffer.length;
                                    break;
                                default:
                                    headerBufferIndex = 0; //reset packet parser
                                    incomingBytesIndex -= (headerBuffer.length - 1);
                                    System.err.println("BluetoothTCPServerThread: Received Invalid Packet Header" +
                                        headerBuffer);
                            }
                        }
                    }
                    if (headerBufferIndex == headerBuffer.length) //header is complete. copy data to packet.
                    {
                        int length = packetBuffer.length - packetBufferIndex;
                        if (length > incomingLength - incomingBytesIndex) length = incomingLength - incomingBytesIndex;
                        if (length > 0) {
                            System.arraycopy(incomingBytes, incomingBytesIndex, packetBuffer, packetBufferIndex, length);
                            incomingBytesIndex += length;
                            packetBufferIndex += length;
                        }
                        else incomingBytesIndex++;
                        if (packetBufferIndex == packetBuffer.length) { //packet is complete
                            dispatchPacket(packetBuffer);
                            headerBufferIndex = 0;
                        }
                    }
                }
            }
        }
        catch (IOException e) { }
        cleanExit();
    }

    private void cleanExit() {
        Debug.println(7, "BluetoothTCPServer: Disconnecting BluetoothClient.");
        hciTransport.unregisterHCIReceiver(this);
        isConnected = false;
        try { socketIn.close(); }
        catch (IOException e) { }
        try { socketOut.close(); }
        catch (IOException e) { }
        try { socket.close(); }
        catch (IOException e) { }
    }

    private void dispatchPacket(byte[] packet) throws IOException {
        switch (packet[0]) {
            case HCI_COMMAND_PACKET: {
                    try {
                        Debug.println(7, "BluetoothTCPServer: Received HCI Command Packet from BluetoothTCPClient.");
                        byte[] result = hciTransport.send_HCI_Command_Packet(packet);
                        Debug.println(7, "BluetoothTCPServer: Sending HCI Command Complete or Command Status Packet to BluetoothTCPClient.");
                        sendPacketToBluetoothTCPClient(result);
                    }
                    catch (HCIException e) { System.err.println("BluetoothTCPServerThread: HCIException: " + e); }
                    break;
                }
            case L2CAP_CREATE_CONNECTION_REQUEST: {
                    processL2CAP_Create_Connection_Request(packet);
                    break;
                }
            case L2CAP_DISCONNECT_CHANNEL_REQUEST: {
                    processL2CAP_Disconnect_Channel_Request(packet);
                    break;
                }
            case SDP_REGISTER_SERVICE_REQUEST: {
                    processSDP_Register_Service_Request(packet);
                    break;
                }
            case L2CAP_PACKET: {
                    processL2CAP_Packet(packet);
                    break;
                }
            default:
                System.err.println("BluetoothTCPServerThread: Received Unknown Packet Type.");
        }
    }

    /** @param packet */
    private void processL2CAP_Packet(byte[] packet) {
        Debug.println(7, "BluetoothTCPServer: Received L2CAP Packet from BluetoothTCPClient.");
        short channelHandel  = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        int length           = ((packet[1] & 0xff) | (packet[2] & 0xff) << 8) - 2;
        L2CAPChannel channel = channels[channelHandel];
        if (channel != null) {
            byte[] l2capPacket = new byte[length];
            System.arraycopy(packet, 5, l2capPacket, 0, l2capPacket.length);
            try { channel.sendL2CAPPacket(l2capPacket); }
            catch (IOException e) { System.err.println("BluetoothTCPServerThread: Error while sending L2CAP Packet: " + e); }
        }
    }

    /** @param packet */
    private void processL2CAP_Create_Connection_Request(byte[] packet) throws IOException {
        Debug.println(7, "BluetoothTCPServer: Received L2CAP Connection Request from BluetoothTCPClient.");
        short channelHandel = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        short psm = (short)((((short)packet[5]) & 0xff) | (((short)packet[6]) & 0xff) << 8);
        long remoteAddress = (((long)packet[7]) & 0xff) | (((long)packet[8]) & 0xff) << 8 | (((long)packet[9]) & 0xff) << 16 |
            (((long)packet[10]) & 0xff) << 24 | (((long)packet[11]) & 0xff) << 32 | (((long)packet[12]) & 0xff) << 40;
        byte pageScanRepMode             = packet[13];
        byte pageScanMode                = packet[14];
        short clockOffset                = (short)((((short)packet[15]) & 0xff) | (((short)packet[16]) & 0xff) << 8);
        BluetoothTCPChannel l2capChannel = new BluetoothTCPChannel(this, channelHandel);
        if (channels[channelHandel] != null) channels[channelHandel].close();
        channels[channelHandel] = l2capChannel;
        try {
            L2CAPLink link = hciTransport.getL2CAPLink(remoteAddress, pageScanRepMode, pageScanMode, clockOffset);
            link.connectL2CAPChannel(l2capChannel, psm);
        }
        catch (HCIException e) {
            l2capChannel.channelState = L2CAPChannel.FAILED;
            System.err.println("BluetoothTCPServerThread: Failed to Connect L2CAP Channel. " + e);
        }
        byte[] createConnectionResponse = {
            L2CAP_CREATE_CONNECTION_RESPONSE, 0x07, 0x00, //fixed length 7
            packet[3], packet[4], //the channelHandle
            l2capChannel.channelState, (byte)((l2capChannel.localChannelID) & 0xff), (byte)((l2capChannel.localChannelID >> 8) & 0xff),
                (byte)((l2capChannel.remoteChannelID) & 0xff), (byte)((l2capChannel.remoteChannelID >> 8) & 0xff)
        };
        Debug.println(7, "BluetoothTCPServer: Sending L2CAP Connection Response to BluetoothTCPClient.");
        sendPacketToBluetoothTCPClient(createConnectionResponse);
    }

    private void processL2CAP_Disconnect_Channel_Request(byte[] packet) {
        Debug.println(7, "BluetoothTCPServer: Received L2CAP Disconnection Request from BluetoothTCPClient.");
        short channelHandel = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        L2CAPChannel channel = channels[channelHandel];
        channels[channelHandel] = null;
        if (channel != null) { channel.close(); }
    }

    /** @param packet */
    private void processSDP_Register_Service_Request(byte[] packet) throws IOException {
        Debug.println(7, "BluetoothTCPServer: Received SDP Register Service Request from BluetoothTCPClient.");
        short length         = (short)((((short)packet[1]) & 0xff) | (((short)packet[2]) & 0xff) << 8);
        short channelHandel  = (short)((((short)packet[3]) & 0xff) | (((short)packet[4]) & 0xff) << 8);
        short serviceUUID    = (short)((((short)packet[5]) & 0xff) | (((short)packet[6]) & 0xff) << 8);
        short browseUUID     = (short)((((short)packet[7]) & 0xff) | (((short)packet[8]) & 0xff) << 8);
        byte[] serviceRecord = new byte[length - 6];
        System.arraycopy(packet, 9, serviceRecord, 0, serviceRecord.length);
        BluetoothTCPChannel l2capChannel = new BluetoothTCPChannel(this, channelHandel);
        if (channels[channelHandel] != null) channels[channelHandel].close();
        channels[channelHandel] = l2capChannel;
        BluetoothTCPChannelFactory channelFactory = new BluetoothTCPChannelFactory(l2capChannel);
        SDPServer sdpServer = SDPServer.getSDPServer();
        sdpServer.registerService(channelFactory, serviceUUID, browseUUID, serviceRecord);
    }

    public void sendPacketToBluetoothTCPClient(byte[] eventPacket) throws IOException {
        socketOut.write(eventPacket);
        socketOut.flush();
    }

    /** @see org.javabluetooth.stack.hci.HCIInquiryResultReceiver#receive_HCI_Event_Inquiry_Result(byte[]) */
    public void receive_HCI_Event_Inquiry_Result(byte[] eventPacket) {
        Debug.println(7, "BluetoothTCPServer: Sending HCI Inquiry Result Event to BluetoothTCPClient.");
        try { sendPacketToBluetoothTCPClient(eventPacket); }
        catch (IOException e) { cleanExit(); }
    }
    
    /** @see org.javabluetooth.stack.hci.HCIInquiryResultReceiver#receive_HCI_Event_Inquiry_Result_With_RSSI(byte[]) */
    public void receive_HCI_Event_Inquiry_Result_With_RSSI(byte[] eventPacket) {
        Debug.println(7, "BluetoothTCPServer: Sending HCI Inquiry Result With RSSI Event to BluetoothTCPClient.");
        try { sendPacketToBluetoothTCPClient(eventPacket); }
        catch (IOException e) { cleanExit(); }
    }

    /** @see org.javabluetooth.stack.hci.HCIInquiryReceiver#receive_HCI_Event_Inquiry_Complete(byte[]) */
    public void receive_HCI_Event_Inquiry_Complete(byte[] eventPacket) {
        Debug.println(7, "BluetoothTCPServer: Sending HCI Inquiry Complete Event to BluetoothTCPClient.");
        try { sendPacketToBluetoothTCPClient(eventPacket); }
        catch (IOException e) { cleanExit(); }
    }

    /** @see org.javabluetooth.stack.hci.HCIReceiver#receive_HCI_Event_Remote_Name_Request_Complete(byte[]) */
    public void receive_HCI_Event_Remote_Name_Request_Complete(byte[] eventPacket) {
        Debug.println(7, "BluetoothTCPServer: Sending HCI Remote Name Request Complete Event to BluetoothTCPClient.");
        try { sendPacketToBluetoothTCPClient(eventPacket); }
        catch (IOException e) { cleanExit(); }
    }

	
}

