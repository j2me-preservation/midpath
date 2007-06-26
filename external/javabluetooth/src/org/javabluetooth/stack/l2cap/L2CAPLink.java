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
 * Created on Jun 22, 2003
 *	by Christian Lorenz
 */

package org.javabluetooth.stack.l2cap;

import java.io.IOException;

import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.sdp.SDPServer;
import org.javabluetooth.util.Debug;

/** 
 * This class represents a link between two Bluetooth Devices. It parses
 * data packets into their appropriate L2CAP Channels, and manages the Control Channel for each link.
 * @author Christian Lorenz
 */
public class L2CAPLink implements L2CAPSender {
	private static final byte L2CAP_PACKET_BOUNDRY_FLAG_FIRST = 0x02;
	private static final byte L2CAP_PACKET_BOUNDRY_FLAG_CONTINUING = 0x01;
	private static final byte L2CAP_COMMAND_REJECT = 0x01;
	private static final byte L2CAP_CONNECTION_REQUEST = 0x02;
	private static final byte L2CAP_CONNECTION_RESPONSE = 0x03;
	private static final byte L2CAP_CONFIGURE_REQUEST = 0x04;
	private static final byte L2CAP_CONFIGURE_RESPONSE = 0x05;
	private static final byte L2CAP_DISCONNECTION_REQUEST = 0x06;
	private static final byte L2CAP_DISCONNECTION_RESPONSE = 0x07;
	private static final byte L2CAP_ECHO_REQUEST = 0x08;
	private static final byte L2CAP_ECHO_RESPONSE = 0x09;
	private static final byte L2CAP_INFORMATION_REQUEST = 0x0A;
	private static final byte L2CAP_INFORMATION_RESPONSE = 0x0B;
	private static final byte L2CAP_LINK_CLOSED = 0x00;
	private static final byte L2CAP_LINK_OPEN = 0x01;
	private HCIDriver hciDriver;
	public short connectionHandle;
	public long remoteAddress;
	private byte linkState;
	private SDPServer sddb;
	private L2CAPChannel[] channels;
	private int CHANNEL_OFFSET = 0x40;
	private byte[] packetBuffer;
	private int packetBufferIndex;
	private short packetBufferChannelID;
	private byte identifierCount;

	/**
	 * @param hciTransport The HCITransport associated with this link.
	 * @param data The Connection Complete HCI Event Packet, which triggered the creation of this object.
	 */
	public L2CAPLink(HCIDriver hciDriver, byte[] data) {
		this.hciDriver = hciDriver;
		this.sddb = SDPServer.getSDPServer();
		channels = new L2CAPChannel[256];
		connectionHandle = (short) (((short) data[4] & 0xff) | (((short) data[5] & 0x0f) << 8));
		remoteAddress = (((long) data[6]) & 0xff) | (((long) data[7]) & 0xff) << 8 | (((long) data[8]) & 0xff) << 16
				| (((long) data[9]) & 0xff) << 24 | (((long) data[10]) & 0xff) << 32 | (((long) data[11]) & 0xff) << 40;
		//linkType = data[12];
		//encryptionMode = data[13];
		identifierCount = 1;
		linkState = L2CAP_LINK_OPEN;
		hciDriver.registerL2CAPLink(this);
	}

	public void close() {
		if (linkState == L2CAP_LINK_OPEN) {
			linkState = L2CAP_LINK_CLOSED;
			try {
				hciDriver.send_HCI_LC_Disconnect(connectionHandle);
			} catch (HCIException e) {
				wasDisconnected();
			}
		}
	}

	/**
	 * Called by HCITransport when an Disconnect Complete Event was received.
	 * This method closes all L2CAPChannels associated with this link, and ensures a propper cleanup.
	 */
	public void wasDisconnected() {
		linkState = L2CAP_LINK_CLOSED;
		hciDriver.unregisterL2CAPLink(this);
		for (int i = 0; i < channels.length; i++) {
			if (channels[i] != null) {
				channels[i].channelState = L2CAPChannel.CLOSED;
				channels[i] = null;
			}
		}
	}

	/**
	 * Receives, parses, and dispatches HCI Data Packets.
	 * @param data An ACL Data Packet received by the HCITransport.
	 */
	public void receiveData(byte[] data) {
		switch ((byte) (data[2] & 0x30) >>> 4) {
		case L2CAP_PACKET_BOUNDRY_FLAG_FIRST:
			packetBufferChannelID = (short) (((short) data[8] & 0xff) << 8 | ((short) data[7] & 0xff));
			int packetLength = (short) (((short) data[6] & 0xff) << 8 | ((short) data[5] & 0xff));
			packetBuffer = new byte[packetLength];
			packetBufferIndex = data.length - 9;
			System.arraycopy(data, 9, packetBuffer, 0, packetBufferIndex);
			break;
		case L2CAP_PACKET_BOUNDRY_FLAG_CONTINUING:
			int length = data.length - 5;
			if (length > packetBuffer.length - packetBufferIndex)
				length = packetBuffer.length - packetBufferIndex;
			System.arraycopy(data, 5, packetBuffer, packetBufferIndex, length);
			packetBufferIndex += length;
			break;
		default:
			System.err.println("L2CAP: Received Packet with invalid Boundry Flag : " + Debug.printByteArray(data));
		}
		if (packetBufferIndex == packetBuffer.length) {
			if (packetBufferChannelID == 1) //signalling channel
				dispatchSignallingPacket(packetBuffer);
			else {
				L2CAPChannel channel = channels[packetBufferChannelID];
				if (channel != null) {
					Debug.println(2, "L2CAP: Received Packet for Channel " + channel.localChannelID + " from "
							+ remoteAddress + ":" + channel.remoteChannelID);
					channel.receiveL2CAPPacket(packetBuffer);
				} else
					System.err.println("L2CAP: Unable to deliver packet. Local Channel " + packetBufferChannelID
							+ " not found.  " + Debug.printByteArray(packetBuffer));
			}
		}
	}

	public void sendL2CAPPacket(L2CAPChannel channel, byte[] packet) throws IOException {
		sendL2CAPPacket(channel.remoteAddress, channel.remoteChannelID, packet);
	}

	public void sendL2CAPPacket(long remoteAddress, short remoteChannelID, byte[] packet) throws IOException {
		if (linkState != L2CAP_LINK_OPEN)
			throw new IOException("Failed to send L2CAP Packet. L2CAPLink is not open.");
		Debug.println(2, "L2CAP: Sending Packet to " + remoteAddress + ":" + remoteChannelID);
		short l2capLength = (short) packet.length;
		short hciLength = (short) (l2capLength + 4);
		byte[] hciPacket = new byte[hciLength + 5];
		hciPacket[0] = 0x02; //ACL DataPacket
		hciPacket[1] = (byte) ((connectionHandle) & 0xff);
		hciPacket[2] = (byte) (((connectionHandle >>> 8) & 0xff) | ((2 & 0x03) << 4) //packetboundry new l2cap packet
		| ((0 & 0x03) << 6)); //no broadcast = point to point
		hciPacket[3] = (byte) ((hciLength) & 0xff);
		hciPacket[4] = (byte) ((hciLength >> 8) & 0xff);
		hciPacket[5] = (byte) ((l2capLength) & 0xff);
		hciPacket[6] = (byte) ((l2capLength >> 8) & 0xff);
		hciPacket[7] = (byte) ((remoteChannelID) & 0xff);
		hciPacket[8] = (byte) ((remoteChannelID >> 8) & 0xff);
		System.arraycopy(packet, 0, hciPacket, 9, packet.length);
		try {
			hciDriver.send_HCI_Data_Packet(hciPacket);
		} catch (HCIException e) {
			throw new IOException("L2CAP: Error sending L2CAP Packet." + e);
		}
	}

	/**
	 * Process packets received for the L2CAP Signalling Channel.
	 * @param packet Byte Array containing a L2CAP Packet.
	 */
	private void dispatchSignallingPacket(byte[] packet) {
		Debug.println(2, "L2CAP: Received Packet on the Signalling Channel from " + remoteAddress);
		switch (packet[0]) {
		case L2CAP_COMMAND_REJECT:
			receive_L2CAP_Command_Reject(packet);
			break;
		case L2CAP_CONNECTION_REQUEST:
			receive_L2CAP_Connection_Request(packet);
			break;
		case L2CAP_CONNECTION_RESPONSE:
			receive_L2CAP_Connection_Response(packet);
			break;
		case L2CAP_CONFIGURE_REQUEST:
			receive_L2CAP_Configuration_Request(packet);
			break;
		case L2CAP_CONFIGURE_RESPONSE:
			receive_L2CAP_Configuration_Response(packet);
			break;
		case L2CAP_DISCONNECTION_REQUEST:
			receive_L2CAP_Disconnection_Request(packet);
			break;
		case L2CAP_DISCONNECTION_RESPONSE:
			receive_L2CAP_Disconnection_Response(packet);
			break;
		case L2CAP_ECHO_REQUEST:
			receive_L2CAP_Echo_Request(packet);
			break;
		case L2CAP_ECHO_RESPONSE:
			receive_L2CAP_Echo_Response(packet);
			break;
		case L2CAP_INFORMATION_REQUEST:
			receive_L2CAP_Information_Request(packet);
			break;
		case L2CAP_INFORMATION_RESPONSE:
			receive_L2CAP_Information_Response(packet);
			break;
		default:
			receive_L2CAP_Unknown_Signalling_Packet(packet);
		}
	}

	private void receive_L2CAP_Command_Reject(byte[] packet) {
		System.err.println("L2CAP: Received Command Reject Signal from " + remoteAddress + "  :  "
				+ Debug.printByteArray(packet));
	}

	private void receive_L2CAP_Connection_Request(byte[] packet) {
		Debug.println(3, "L2CAP: Received Connection Request Signal from " + remoteAddress);
		short psm = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		short remoteCID = (short) (((short) packet[7] & 0xff) << 8 | ((short) packet[6] & 0xff));
		short localCID = -1;
		for (short i = 0; i < channels.length; i++) {
			if (channels[i] == null) {
				localCID = i;
				break;
			}
		}
		int resultCode = 0x0000;
		short localChannelID = (short) (localCID);
		if (localCID == -1) {
			resultCode = 0x0004; //Error: No Resources
		} else {
			L2CAPChannel channel = sddb.resolveAndCreateL2CAPChannel(psm, this, localChannelID, remoteCID);
			if (channel == null) {
				resultCode = 0x0002; //Error: No Psm
			} else {
				channels[localCID] = channel;
				channel.channelState = L2CAPChannel.CLOSED;
				channel.l2capSender = this;
				channel.localChannelID = localChannelID;
				channel.remoteChannelID = remoteCID;
			}
		}
		byte[] connectionResponse = { 0x03, packet[1], 0x08, 0x00, (byte) (localChannelID & 0xff),
				(byte) ((localChannelID >> 8) & 0xff), packet[6], packet[7], (byte) ((resultCode) & 0xff),
				(byte) ((resultCode >> 8) & 0xff), 0x00, 0x00 };
		try {
			Debug.println(3, "L2CAP: Sending Connection Response Signal to " + remoteAddress);
			sendL2CAPPacket(remoteAddress, (short) 0x0001, connectionResponse);
		} catch (IOException e) {
			this.close();
		}
	}

	private void receive_L2CAP_Connection_Response(byte[] packet) {
		Debug.println(3, "L2CAP: Received Connection Response Signal from " + remoteAddress);
		short remoteCID = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		short localCID = (short) (((short) packet[7] & 0xff) << 8 | ((short) packet[6] & 0xff));
		short result = (short) (((short) packet[9] & 0xff) << 8 | ((short) packet[8] & 0xff));
		short status = (short) (((short) packet[11] & 0xff) << 8 | ((short) packet[10] & 0xff));
		L2CAPChannel channel = channels[localCID];
		if (channel != null) {
			channel.remoteChannelID = remoteCID;
			channel.channelState = L2CAPChannel.CONFIG;
			try {
				send_L2CAP_Configuration_Request(channel);
			} catch (IOException e) {
				this.close();
			}
		}
	}

	private void receive_L2CAP_Configuration_Request(byte[] packet) {
		Debug.println(3, "L2CAP: Received Configuration Request Signal from " + remoteAddress);
		short localCID = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		short optionLength = (short) ((((short) packet[3] & 0xff) << 8 | ((short) packet[2] & 0xff)) - 4);
		//TODO parse configuration options in a propper way... this implementation just acknowledges but never challenges the proposed authentication	
		L2CAPChannel channel = channels[localCID];
		if (channel != null) {

			Debug.println(3, "L2CAP: Received Configuration Request Signal from " + remoteAddress + "2");
			byte[] configurationResponse = new byte[/*optionLength+*/10];
			configurationResponse[0] = 0x05;
			configurationResponse[1] = packet[1];
			configurationResponse[2] = (byte) ((/*optionLength+*/6) & 0xff);
			configurationResponse[3] = (byte) (((/*optionLength+*/6) >> 8) & 0xff);
			configurationResponse[4] = (byte) ((channel.remoteChannelID) & 0xff);
			configurationResponse[5] = (byte) ((channel.remoteChannelID >> 8) & 0xff);
			configurationResponse[6] = 0x00; //no flags
			configurationResponse[7] = 0x00; //no flags
			configurationResponse[8] = 0x00; //result ok
			configurationResponse[9] = 0x00; //result ok
			//System.arraycopy(packet,8,configurationResponse,10,optionLength);
			//configurationResponse[12]=0x00;//new mtu
			//configurationResponse[13]=(byte) 0xff;//new mtu						
			try {
				Debug.println(3, "L2CAP: Sending Configuration Response Signal to " + remoteAddress);
				sendL2CAPPacket(remoteAddress, (short) 0x0001, configurationResponse);
				if (channel.channelState == L2CAPChannel.CLOSED) {
					channel.channelState = L2CAPChannel.CONFIG;
					send_L2CAP_Configuration_Request(channel);
				}
			} catch (IOException e) {
				this.close();
			}
		}
	}

	private void receive_L2CAP_Configuration_Response(byte[] packet) {
		Debug.println(3, "L2CAP: Received Configuration Response Signal from " + remoteAddress);
		short localCID = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		short result = (short) (((short) packet[9] & 0xff) << 8 | ((short) packet[8] & 0xff));
		if (result == 0) {
			L2CAPChannel channel = channels[localCID];
			if (channel != null) {
				if (channel.channelState == L2CAPChannel.CLOSED)
					channel.channelState = L2CAPChannel.CONFIG;
				else if (channel.channelState == L2CAPChannel.CONFIG)
					channel.channelState = L2CAPChannel.OPEN;
			}
		}
		//TODO Z: add possible challenge to the response... this implementation just nods and agrees...
	}

	private void receive_L2CAP_Disconnection_Request(byte[] packet) {
		Debug.println(3, "L2CAP: Received Disconnection Request Signal from " + remoteAddress);
		short localCID = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		//short remoteCID=(short)(((short) packet[7] & 0xff) << 8 | ((short) packet[6]& 0xff));
		L2CAPChannel channel = channels[localCID];
		channels[localCID] = null;
		if (channel != null) {
			channel.wasDisconnected();
		}
		byte[] disconnectionResponse = { 0x07, packet[1], 0x04, 0x00, packet[4], packet[5], packet[6], packet[7] };
		try {
			Debug.println(3, "L2CAP: Sending Disconnection Response Signal to " + remoteAddress);
			sendL2CAPPacket(remoteAddress, (short) 0x0001, disconnectionResponse);
		} catch (IOException e) {
			wasDisconnected();
		}
	}

	private void receive_L2CAP_Disconnection_Response(byte[] packet) {
		Debug.println(3, "L2CAP: Received Disconnection Response Signal from " + remoteAddress);
		//short localCID = (short) (((short) packet[8] & 0xff) << 8 | ((short) packet[7] & 0xff));
		short localCID = (short) (((short) packet[5] & 0xff) << 8 | ((short) packet[4] & 0xff));
		//Debug.println(3, "L2CAP: Received Disconnection Response Signal from " + remoteAddress + " localCID: " + localCID);
		L2CAPChannel channel = channels[localCID];
		channels[localCID] = null;
		if (channel != null) {
			channel.wasDisconnected();
		}
	}

	private void receive_L2CAP_Echo_Request(byte[] packet) {
		Debug.println(3, "L2CAP: Received Echo Request Signal from " + remoteAddress);
	}

	private void receive_L2CAP_Echo_Response(byte[] packet) {
		Debug.println(3, "L2CAP: Received Echo Response Signal from " + remoteAddress);
	}

	private void receive_L2CAP_Information_Request(byte[] packet) {
		Debug.println(3, "L2CAP: Received Information Request Signal from " + remoteAddress);
	}

	private void receive_L2CAP_Information_Response(byte[] packet) {
		Debug.println(3, "L2CAP: Received Information Response Signal from " + remoteAddress);
	}

	private void receive_L2CAP_Unknown_Signalling_Packet(byte[] packet) {
		System.err.println("L2CAP: Received unknown signalling packet:" + Debug.printByteArray(packet));
		send_L2CAP_Command_Reject();
	}

	public void send_L2CAP_Command_Reject() {
		Debug.println(3, "L2CAP: Sending Command Rejected Signal from " + remoteAddress);
	}

	public void send_L2CAP_Connection_Request(L2CAPChannel channel, short psm) throws HCIException, IOException {
		channel.channelState = L2CAPChannel.CLOSED;

		// TO REMOVE		
		//		boolean exist = false;
		//		for (short i = 0; i < channels.length; i++) {
		//			if (channels[i] == channel) {
		//				exist = true;
		//				break;
		//			}
		//		}

		for (short i = 0; i < channels.length; i++) {
			if (channels[CHANNEL_OFFSET + i] == null) {
				channels[CHANNEL_OFFSET + i] = channel;
				channel.channelState = L2CAPChannel.CLOSED;
				channel.remoteAddress = remoteAddress;
				channel.l2capSender = this;
				channel.localChannelID = (short) (CHANNEL_OFFSET + i); //(short)(i + 2); // FIXME ??
				break;
			}
		}

		if (channel.localChannelID == -1)
			throw new HCIException("L2CAP Connection Request failed. No Local Channels available.");
		byte[] connectionRequest = { 0x02, identifierCount++,
				0x04, //fixed length
				0x00, //fixed length
				(byte) ((psm) & 0xff), (byte) ((psm >> 8) & 0xff), (byte) ((channel.localChannelID) & 0xff),
				(byte) ((channel.localChannelID >> 8) & 0xff) };
		Debug.println(3, "L2CAP: Sending Connection Request Signal to " + remoteAddress);
		sendL2CAPPacket(remoteAddress, (short) 0x0001, connectionRequest);
	}

	public void send_L2CAP_Configuration_Request(L2CAPChannel channel) throws IOException {
		byte[] configurationRequest = { 0x04, identifierCount++, 0x04, //fixed length
				0x00, //fixed length
				(byte) ((channel.remoteChannelID) & 0xff), (byte) ((channel.remoteChannelID >> 8) & 0xff), 0x00, //no flags
				0x00 };
		Debug.println(3, "L2CAP: Sending Configuration Request Signal to " + remoteAddress);
		sendL2CAPPacket(remoteAddress, (short) 0x0001, configurationRequest);
	}

	public void send_L2CAP_Disconnection_Request(L2CAPChannel channel) throws IOException {
		byte[] disconnectionRequest = { 0x06, identifierCount++, 0x04, 0x00, (byte) ((channel.remoteChannelID) & 0xff),
				(byte) ((channel.remoteChannelID >> 8) & 0xff), (byte) ((channel.localChannelID) & 0xff),
				(byte) ((channel.localChannelID >> 8) & 0xff) };
		channel.channelState = L2CAPChannel.CLOSED;
		Debug.println(3, "L2CAP: Sending Disconnection Request Signal to " + remoteAddress);
		
		// TODO remove me
//		Throwable e= new Throwable();
//		e.fillInStackTrace();
//		e.printStackTrace();
		
		sendL2CAPPacket(remoteAddress, (short) 0x0001, disconnectionRequest);
	}

	public void send_L2CAP_Echo_Request(L2CAPChannel channel) { //Debug.println("L2CAP: Sending Echo Request to
		// ("+remoteAddress+"): "+Debug.printByteArray(packet));
		//TODO send echo request
	}

	public void send_L2CAP_Information_Request(L2CAPChannel channel) { //Debug.println("L2CAP: Sending Information Request to
		// ("+remoteAddress+"): "+Debug.printByteArray(packet));
		//TODO send information request
	}

	/**
	 * @param psm
	 * @return
	 */
	public void connectL2CAPChannel(L2CAPChannel channel, short psm) throws HCIException {
		try {
			send_L2CAP_Connection_Request(channel, psm);
		} catch (IOException e) {
			throw new HCIException("L2CAPChannel Connection Request Failed.");
		}
		int timeout = 0;
		while (channel.channelState != L2CAPChannel.OPEN) {
			try {
				Thread.sleep(1000);
				timeout++;
			} catch (InterruptedException e) {
			}
			if (timeout > 50)
				throw new HCIException("L2CAPChannel Connection Request timed out.");
		}
	}

	/** @see org.javabluetooth.stack.l2cap.L2CAPSender#closeL2CAPChannel(org.javabluetooth.stack.l2cap.L2CAPChannel) */
	public void closeL2CAPChannel(L2CAPChannel channel) {
		try {
			send_L2CAP_Disconnection_Request(channel);
		} catch (IOException e) {
		}
	}
}
