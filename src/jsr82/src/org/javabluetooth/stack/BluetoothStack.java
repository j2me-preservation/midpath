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
* Created on Jun 12, 2003
* by Christian Lorenz
*
*/

package org.javabluetooth.stack;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.RemoteDevice;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.l2cap.L2CAPChannel;

/** 
 * This abstract class provides the upper half of the bluetooth stack.
 * It provides implementations for HCI Commands, common entry
 * points for HCIEvents.	This abstract class expects all implementing
 * methods to connect to a <code>HCITransport</code>. This may
 * happen locally (<code>BluetoothLocal</code>) or remotely (<code>BluetoothTCPClient</code> and
 * <code>BluetoothTCPServer</code>. The HCI Command methods provided by this class are called from
 * this <code>javax.bluetooth</code> implementation.
 * @see javax.bluetooth
 * @see org.javabluetooth.stack.BluetoothLocal
 * @see org.javabluetooth.stack.BluetoothTCPClient
 * @see org.javabluetooth.stack.hci.HCIReceiver
 * @see org.javabluetooth.stack.hci.HCITransport
 * @author Christian Lorenz
 */
public abstract class BluetoothStack {
    private static final byte CMD_PKT = 0x01;
    private static BluetoothStack bluetoothStack;
    private DiscoveryAgent discoveryAgent;

    protected abstract byte[] send_HCI_Command_Packet(byte[] cmdPacket) throws HCIException;

    public abstract void connectL2CAPChannel(L2CAPChannel channel, RemoteDevice remoteDevice, short psm) throws HCIException;

    public abstract void registerL2CAPService(L2CAPChannel channel, short serviceUUID, short browseUUID,
        byte[] serviceRecord) throws HCIException;

    /**
     * Sets which instance of <code>BluetoothStack</code> should be returned by
     * <code>BluetoothStack.getBluetoothStack()</code>. This allows easy switching
     * between <code>BluetoothLocal</code> and <code>BluetoothTCPClient</code>.
     * @param bluetoothStack
     */
    public static void init(BluetoothStack bluetoothStack) { BluetoothStack.bluetoothStack = bluetoothStack; }

    /**
     * Returns the <code>BluetoothStack</code> instance set by <code>BluetoothStach.init(BluetoothStack)</code>. This
     * method is used by <code>javax.bluetooth.LocalDevice</code>
     * to determine which implementation of <code>BluetoothStack</code> to use.
     * @return BluetoothStack instance set by BluetoothStack.init(BluetoothStack)
     * @throws HCIException if no BluetoothStack was initialized via BluetoothStack.init(BluetoothStack)
     */
    public static BluetoothStack getBluetoothStack() throws HCIException {
        if (bluetoothStack == null) throw new HCIException("BluetoothStack not initalized. ");
        return bluetoothStack;
    }

    /**
     * Registers a <code>DiscoveryListener</code> which will be notified of all Inquiry Results.
     * This will usually be the <code>DiscoveryAgent</code>.
     * @see javax.bluetooth.DiscoveryListener
     * @see javax.bluetooth.DiscoveryAgent
     * @param listener
     */
    public void registerDiscoveryAgent(DiscoveryAgent discoveryAgent) { this.discoveryAgent = discoveryAgent; }

    /**
     * Receives an HCI Inquiry Complete Packet. If a <code>javax.bluetooth.DiscoveryAgent</code>
     * was registered, the packed will be passed along.
     * @see javax.bluetooth.DiscoveryAgent
     * @param eventPacket
     */
    protected void receive_HCI_Event_Inquiry_Complete(byte[] eventPacket) { //Debug.println("Inquiry Complete");
        if (discoveryAgent != null) { discoveryAgent.receive_HCI_Event_Inquiry_Complete(eventPacket); }
    }

    /**
     * Receives an Inquiry Result Event Packet. If a <code>javax.bluetooth.DiscoveryAgent</code>
     * is registered the packet will be passed along.
     * @see javax.bluetooth.DiscoveryAgent
     * @param eventPacket
     */
    protected void receive_HCI_Event_Inquiry_Result(byte[] eventPacket) { //Debug.println("Inquiry Result");
        if (discoveryAgent != null) { discoveryAgent.receive_HCI_Event_Inquiry_Result(eventPacket); }
    }

    /**
     * Receives a Remote Name Request Event Packet. If a <code>javax.bluetooth.DiscoveryAgent</code> was registered the
     * packet will be passed along.
     * @see javax.bluetooth.DiscoveryAgent
     * @param eventPacket
     */
    protected void receive_HCI_Event_Remote_Name_Request_Complete(byte[] eventPacket) { //Debug.println("Remote Name Request Complete");
        if (discoveryAgent != null) { discoveryAgent.receive_HCI_Event_Remote_Name_Request_Complete(eventPacket); }
    }

    /**
     *	This command will cause the Bluetooth device to enter Inquiry Mode. Inquiry
     * Mode is used to discover other nearby Bluetooth devices.   When the Inquiry
     * process is completed, the Host Controller will send an Inquiry Complete event
     * to the Host indicating that the Inquiry has finished.  When a Bluetooth device responds to the Inquiry message, an
     * Inquiry Result event will occur to notify the Host of the discovery.
     * For details see Page 561 of the Bluetooth Core Specification Version 1.1
     * @param accessCode is the LAP from which the inquiry access code should be derived.
     * Valid values are DiscoveryAgent.GIAC and DiscoveryAgent.LIAC.
     * @param inquiryLength specifies the total duration of the Inquiry Mode. When this time expires, Inquiry will be halted.
     * @param numResponses specifies the number of responses that can be received before the Inquiry is halted.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.	
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_LC_Inquiry(int accessCode, int inquiryLength, byte numResponses) throws HCIException {
        byte time = (byte)(inquiryLength / 1.28);
        if (inquiryLength > 61.44) time = (byte)0xff;
        byte[] data = {
            CMD_PKT, 0x01, 0x04, 0x05, (byte)((accessCode) & 0xff), (byte)((accessCode >> 8) & 0xff), (byte)((accessCode >> 16) & 0xff), time, numResponses
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[3];
    }

    /**
     * This command will cause the Bluetooth device to stop the current Inquiry if the Bluetooth device is in Inquiry Mode.
     * For details see Page 563 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.	
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_LC_Inquiry_Cancel() throws HCIException {
        byte[] data = { CMD_PKT, 0x02, 0x04, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[3];
    }

    /**
     * The Remote_Name_Request command is used to obtain the user-friendly name of another Bluetooth device.
     * The user-friendly name is used to enable the user to distinguish one Bluetooth device from another. The BD_ADDR command
     * parameter is used to identify the device for which the user-friendly
     * name is to be obtained. The Page_Scan_Repetition_Mode and Page_Scan_Mode command parameters specify the page scan modes
     * supported by the remote device with the BD_ADDR. This is the information that was acquired during the inquiry process.
     * The Clock_Offset parameter is the difference between its own clock and the clock of the remote device with BD_ADDR.
     * Only bits 2 through 16 of the difference are used and they are mapped to this parameter as bits 0 through 14
     * respectively. A Clock_Offset_Valid_Flag, located in bit 15 of the Clock_Offset command parameter, is used to indicate
     * if the Clock Offset is valid or not. Note: if no connection exists between the local device and the device
     * corresponding to the BD_ADDR, a temporary link layer connection will
     * be established to obtain the name of the remote device.
     * For details see Page 590 of the Bluetooth Core Specification Version 1.1
     * @param bd_addr BD_ADDR for the device whose name is requested.
     * @param pageScanRepetitionMode 0x00 = R0 0x01 = R1 0x02 = R2
     * @param pageScanMode 0x00                 Mandatory Page Scan Mode. 0x01                 Optional Page Scan Mode I.
     * 0x02                 Optional Page Scan Mode II. 0x03                 Optional Page Scan Mode III.
     * @param clockOffset Bit 14.0             Bit 16.2 of CLKslave-CLKmaster.
     * Bit 15               Clock_Offset_Valid_Flag  ( Invalid Clock Offfset = 0  Valid Clock Offset = 1 )
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_LC_Remote_Name_Request(long bd_addr, byte pageScanRepetitionMode, byte pageScanMode,
        short clockOffset) throws HCIException {
            byte[] data = {
                CMD_PKT, 0x19, 0x04, 0x0a, (byte)((bd_addr) & 0xff), (byte)((bd_addr >> 8) & 0xff), (byte)((bd_addr >> 16) & 0xff),
                    (byte)((bd_addr >> 24) & 0xff), (byte)((bd_addr >> 32) & 0xff), (byte)((bd_addr >> 40) & 0xff),
                    pageScanRepetitionMode, pageScanMode, (byte)((clockOffset) & 0xff), (byte)((clockOffset >> 8) & 0xff)
            };
            byte[] resultData = send_HCI_Command_Packet(data);
            return resultData[3];
    }

    /**
     * Clears all Event Filters. For details see Page 623 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Set_Event_Filter_Clear() throws HCIException {
        byte[] data = { CMD_PKT, 0x05, 0x0c, 0x01, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Adds Connection Setup Filters for Connections from all devices.
     * @param condition 0x01	Do NOT Auto accept the connection. (Auto accept is off)
     * 0x02	Do Auto accept the connection with role switch disabled. (Auto accept is on).
     * 0x03	Do Auto accept the connection with role switch enabled. (Auto accept is on). Note: When auto accepting an incoming
     * SCO connection, no	role switch will be performed. The value 0x03 of the Auto_Accept_Flag will then get the same
     * effect as if the value had been 0x02. For details see Page 623 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Connection_Setup(byte condition) throws HCIException {
        byte[] data = { CMD_PKT, 0x05, 0x0c, 0x03, 002, 0x00, condition };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Adds Connection Setup Filters for Connections from a device with a specific BD_ADDR.
     * @param bd_addr specifies the Bluetooth Address of the device to be filtered.
     * @param condition 0x01	Do NOT Auto accept the connection. (Auto accept is off)
     * 0x02	Do Auto accept the connection with role switch disabled. (Auto accept is on).
     * 0x03	Do Auto accept the connection with role switch enabled. (Auto accept is on). Note: When auto accepting an incoming
     * SCO connection, no	role switch will be performed. The value 0x03 of the Auto_Accept_Flag will then get the same
     * effect as if the value had been 0x02. For details see Page 623 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Connection_Setup(long bd_addr, byte condition) throws HCIException {
        byte[] data = {
            CMD_PKT, 0x05, 0x0c, 0x09, 0x02, 0x02, (byte)((bd_addr) & 0xff), (byte)((bd_addr >> 8) & 0xff), (byte)((bd_addr >> 16) & 0xff),
                (byte)((bd_addr >> 24) & 0xff), (byte)((bd_addr >> 32) & 0xff), (byte)((bd_addr >> 40) & 0xff), condition
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Adds Connection Setup Filters for Connections from devices with a specific Class Of Device.
     * @param classOfDevice specifies the Class Of Device of the devices to be filtered.
     * @param mask Bit Mask used to determine which bits of the Class of Device parameter
     * are `don't care'. Zero-value bits in the mask indicate the `don't care' bits of the Class of Device.
     * @param condition 0x01	Do NOT Auto accept the connection. (Auto accept is off)
     * 0x02	Do Auto accept the connection with role switch disabled. (Auto accept is on).
     * 0x03	Do Auto accept the connection with role switch enabled. (Auto accept is on). Note: When auto accepting an incoming
     * SCO connection, no	role switch will be performed. The value 0x03 of the Auto_Accept_Flag will then get the same
     * effect as if the value had been 0x02. For details see Page 623 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Connection_Setup(int classOfDevice, int mask,
        byte condition) throws HCIException {
            byte[] data = {
                CMD_PKT, 0x05, 0x0c, 0x09, 0x02, 0x01, (byte)((classOfDevice) & 0xff), (byte)((classOfDevice >> 8) & 0xff),
                    (byte)((classOfDevice >> 16) & 0xff), (byte)((mask) & 0xff), (byte)((mask >> 8) & 0xff), (byte)((mask >> 16) & 0xff), condition
            };
            byte[] resultData = send_HCI_Command_Packet(data);
            return resultData[6];
    }

    /**
     * Adds Inquiry Result Filter for all new Devices responding to the Inquiry Process. For details see Page 623 of the
     * Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Inquiry_Result() throws HCIException {
        byte[] data = { CMD_PKT, 0x05, 0x0c, 0x02, 0x01, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Adds Inquiry Result Filter for devices with a specific Bluetooth Address. For details see Page 623 of the Bluetooth
     * Core Specification Version 1.1
     * @param bd_addr Bluetooth Address of the Device for which Inquiry Filters are being set.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Inquiry_Result(long bd_addr) throws HCIException {
        byte[] data = {
            CMD_PKT, 0x05, 0x0c, 0x08, 0x01, 0x02, (byte)((bd_addr) & 0xff), (byte)((bd_addr >> 8) & 0xff), (byte)((bd_addr >> 16) & 0xff),
                (byte)((bd_addr >> 24) & 0xff), (byte)((bd_addr >> 32) & 0xff), (byte)((bd_addr >> 40) & 0xff)
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Adds Inquiry Result Filter for devices with a specific Class Of Device. For details see Page 623 of the Bluetooth Core
     * Specification Version 1.1
     * @param classOfDevice specifies the Class Of Device of the devices to be filtered.
     * @param mask Bit Mask used to determine which bits of the Class of Device parameter
     * are `don't care'. Zero-value bits in the mask indicate the `don't care' bits of the Class of Device.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Event_Filter_Inquiry_Result(int classOfDevice, int mask) throws HCIException {
        byte[] data = {
            CMD_PKT, 0x05, 0x0c, 0x09, 0x01, 0x01, (byte)((classOfDevice) & 0xff), (byte)((classOfDevice >> 8) & 0xff),
                (byte)((classOfDevice >> 16) & 0xff), (byte)((mask) & 0xff), (byte)((mask >> 8) & 0xff),
                (byte)((mask >> 16) & 0xff)
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * The Change_Local_Name command provides the ability to modify
     * the user-friendly name for the Bluetooth device. A Bluetooth device
     * may send a request to get the user-friendly name of another Bluetooth
     * device. The user-friendly name provides the user with the ability to distinguish one Bluetooth device from another.
     * For details see Page 640 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Change_Local_Name(String localName) throws HCIException {
        byte[] data = new byte[252];
        data[0] = CMD_PKT;
        data[1] = (byte)0x13;
        data[2] = (byte)0x0c;
        data[3] = (byte)0xf8;
        int byteCount = 0;
        for ( ; (byteCount < localName.length()) && (byteCount < 247); byteCount++)
            data[byteCount + 4] = (byte)localName.charAt(byteCount);
        for ( ; byteCount < 248; byteCount++) data[byteCount + 4] = (byte)0x00;
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * Retrieves the name of the local device.  The Bluetooth specification calls this name the "Bluetooth device name" or the
     * "user-friendly name". For details see Page 641 of the Bluetooth Specification Version 1.1
     * @return the name of the local device; <code>null</code> if the name could not be retrieved
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public String send_HCI_HC_Read_Local_Name() throws HCIException {
        byte[] data = { CMD_PKT, 0x14, 0x0c, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        if (resultData[6] == 0x00) {
            int i = 0;
            while (i < 248 && resultData[i + 7] != 0x00) { i++; }
            return new String(resultData, 7, i);
        }
        else return null;
    }

    /**
     * This command will read the value for the Scan_Enable parameter. The
     * Scan_Enable parameter controls whether or not the Bluetoothdevice will periodically scan for page attempts and/or
     * inquiry requests from other Bluetooth devices. If Page_Scan is enabled, then the device will enter page
     * scan mode based on the value of the Page_Scan_Interval and Page_Scan_Window parameters. If Inquiry_Scan is enabled,
     * then the device will enter Inquiry Scan mode based on the value of the Inquiry_Scan_Interval and Inquiry_Scan_Window
     * parameters. For details see Page 646 of the Bluetooth Core Specification Version 1.1
     * @return 0x00 = No Scans enabled. 0x01 = Inquiry Scan enabled. Page Scan disabled.
     * 0x02 = Inquiry Scan disabled. Page Scan enabled. 0x03 = Inquiry Scan enabled.  Page Scan enabled.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Read_Scan_Enable() throws HCIException {
        byte[] data = { CMD_PKT, 0x19, 0x0C, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[7];
    }

    /**
     *	This command will write the value for the Scan_Enable parameter. The Scan_Enable parameter controls whether or not the
     * Bluetooth device will periodically scan for page attempts and/or inquiry requests
     * from other Bluetooth devices. If Page_Scan is enabled, then the device
     * will enter page scan mode based on the value of the Page_Scan_Interval
     * and Page_Scan_Window parameters. If Inquiry_Scan is enabled, then the device will enter Inquiry Scan mode based on the
     * value of the Inquiry_Scan_Interval and Inquiry_Scan_Window parameters.
     * For details see Page 647 of the Bluetooth Core Specification Version 1.1
     * @param scanType 0x00 = No Scans enabled. Default. 0x01 = Inquiry Scan enabled. Page Scan disabled.
     * 0x02 = Inquiry Scan disabled. Page Scan enabled. 0x03 = Inquiry Scan enabled.  Page Scan enabled.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Scan_Enable(byte scanType) throws HCIException {
        byte[] data = { CMD_PKT, 0x1a, 0x0C, 0x01, scanType };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * This command will read the value for the Class_of_Device parameter. The Class_of_Device parameter is used to indicate
     * the capabilities of the local device to other devices.
     * For details see Page 661 of the Bluetooth Core Specification Version 1.1
     * @return The Class of Device of the local Bluetooth Host Controller.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public int send_HCI_HC_Read_Class_Of_Device() throws HCIException {
        byte[] data = { CMD_PKT, 0x23, 0x0c, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        //TODO is this right? byte order?
        if (resultData[6] == 0x00) {
            int resultOut = ((int)(resultData[7] & 0xff)) | ((int)(resultData[8] & 0xff) << 8) |
                ((int)(resultData[9] & 0xff) << 16);
            return resultOut;
        }
        else { throw new HCIException("Read_Class_Of_Device Failed: status=" + resultData[6]); }
    }

    /**
     * This command will write the value for the Class_of_Device parameter.
     * The Class_of_Device parameter is used to indicate the capabilities of the local device to other devices.
     * For details see Page 662 of the Bluetooth Core Specification Version 1.1
     * @param classOfDevice the Class of Device for the device.
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public byte send_HCI_HC_Write_Class_Of_Device(int classOfDevice) throws HCIException {
        byte[] data = {
            CMD_PKT, 0x24, 0x0c, 0x03, (byte)((classOfDevice) & 0xff), (byte)((classOfDevice >> 8) & 0xff),
                (byte)((classOfDevice >> 16) & 0xff)
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * This command reads the LAP used to create the Inquiry Access Codes (IAC) that the local Bluetooth device is
     * simultaneously scanning for during Inquiry Scans. For details see Page 691 of the Bluetooth Core
     * Specification Version 1.1
     * @return
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public int send_HCI_HC_Read_Current_IAC_LAP() throws HCIException {
        byte[] data = { CMD_PKT, 0x39, 0x0c, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        if (resultData[6] == 0x00) {
            int resultOut = ((int)((resultData[8] & 0xff))) | ((int)((resultData[9] & 0xff) << 8)) |
                ((int)((resultData[10] & 0xff) << 16));
            return resultOut;
        }
        else throw new HCIException("HCI_HC_Read_Current_IAC_LAP returned invalid result.");
    }

    /**
     * This command writes the LAP(s) used to create the Inquiry Access Code
     * (IAC) that the local Bluetooth device is scanning for during Inquiry Scans.
     * All Bluetooth devices are required to support at least one IAC, the General Inquiry Access Code (the GIAC). Some
     * Bluetooth devices support additional IACs. For details see Page 692 of the Bluetooth Core Specification Version 1.1
     * @param iacLap
     * @return 0x00 if the command succeeded. 0x01-0xFF if the command failed. See Table 6.1 on page 766
     * for list of Error Codes.
     */
    public byte send_HCI_HC_Write_Current_IAC_LAP(int iacLap) throws HCIException {
        byte[] data = {
            CMD_PKT, 0x3a, 0x0c, 0x04, 0x01, (byte)((iacLap) & 0xff), (byte)((iacLap >> 8) & 0xff),
                (byte)((iacLap >> 16) & 0xff)
        };
        byte[] resultData = send_HCI_Command_Packet(data);
        return resultData[6];
    }

    /**
     * This command will read the value for the BD_ADDR parameter.
     * The BD_ADDR is a 48-bit unique identifier for a Bluetooth device.
     * For details see Page 706 of the Bluetooth Core Specification Version 1.1
     * @return Bluetooth Device Address
     * @throws org.javabluetooth.stack.bluetooth.hci.HCIException
     */
    public long send_HCI_IP_Read_BD_ADDR() throws HCIException {
        byte[] data = { CMD_PKT, 0x09, 0x10, 0x00 };
        byte[] resultData = send_HCI_Command_Packet(data);
        if (resultData[6] == 0x00) {
            return (((long)data[7]) & 0xff) | (((long)data[8]) & 0xff) << 8 | (((long)data[9]) & 0xff) << 16 |
                (((long)data[10]) & 0xff) << 24 | (((long)data[11]) & 0xff) << 32 | (((long)data[12]) & 0xff) << 40;
        }
        else throw new HCIException("Read_DB_ADDR Failed: Status=" + resultData[6]);
    }
}

