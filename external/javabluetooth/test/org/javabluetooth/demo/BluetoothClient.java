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
*/

package org.javabluetooth.demo;

import java.io.*;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import org.javabluetooth.distributed.BluetoothTCPClient;
import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.sdp.SDPClientChannel;

/** @author Christian Lorenz */
public class BluetoothClient implements DiscoveryListener {
    private static RemoteDevice remoteDevice;
    private static ServiceRecord[] serviceRecords;

    public static final void main(String[] args) throws Exception {
        /*DataElement resultAttributes = new DataElement(DataElement.DATSEQ);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,0));
        resultAttributes.addElement(new DataElement(DataElement.U_INT_4,0x0100));
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,1));
        DataElement serviceClassIDList = new DataElement(DataElement.DATSEQ);
        UUID serviceClassUUID = new UUID(0x1111);
        serviceClassIDList.addElement(new DataElement(DataElement.UUID,serviceClassUUID));
        resultAttributes.addElement(serviceClassIDList);		
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,4));
        DataElement protocolDescriptorList = new DataElement(DataElement.DATSEQ);
        DataElement l2capDescriptor = new DataElement(DataElement.DATSEQ);
        l2capDescriptor.addElement(new DataElement(DataElement.UUID,new UUID(0x1111)));
        protocolDescriptorList.addElement(l2capDescriptor);
        resultAttributes.addElement(protocolDescriptorList);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,5));
        DataElement browseClassIDList = new DataElement(DataElement.DATSEQ);
        UUID browseClassUUID = new UUID(0x1002);
        browseClassIDList.addElement(new DataElement(DataElement.UUID,browseClassUUID));
        resultAttributes.addElement(browseClassIDList);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,6));
        DataElement languageBaseAttributeIDList = new DataElement(DataElement.DATSEQ);
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2,25966));
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2,106));
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2,256));
        resultAttributes.addElement(languageBaseAttributeIDList);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,9));
        DataElement profileDescriptorList = new DataElement(DataElement.DATSEQ);
        DataElement profileDescriptor = new DataElement(DataElement.DATSEQ);
        profileDescriptor.addElement(new DataElement(DataElement.UUID,new UUID(0x1111)));
        profileDescriptor.addElement(new DataElement(DataElement.U_INT_2,256));
        profileDescriptorList.addElement(profileDescriptor);
        resultAttributes.addElement(profileDescriptorList);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,256));
        resultAttributes.addElement(new DataElement(DataElement.STRING,"Tini Service"));
        System.out.println("Result Attributes="+resultAttributes);
        byte[] attrBytes = resultAttributes.toByteArray();
        Debug.println(6,"Result Bytes",attrBytes);
        DataElement reparsed = new DataElement(attrBytes);
        System.out.println("Reparsed Attributes="+reparsed);
        */

        BluetoothStack.init(new BluetoothTCPClient("192.168.10.2", 2600));
        BluetoothStack bluetooth = BluetoothStack.getBluetoothStack();
        bluetooth.send_HCI_HC_Change_Local_Name("TINI BLUE");
        bluetooth.send_HCI_HC_Write_Event_Filter_Connection_Setup((byte)0x02);
        bluetooth.send_HCI_HC_Write_Event_Filter_Inquiry_Result();
        bluetooth.send_HCI_HC_Write_Scan_Enable((byte)0x03);
        BluetoothClient blue = new BluetoothClient();
        while (remoteDevice == null) { Thread.sleep(1000); }
        if (remoteDevice != null) { //System.out.println("bdAddr:"+remoteDevice.getBluetoothAddress());
            //System.out.println("Name:"+remoteDevice.getFriendlyName(false));
            //System.out.println("major dev class :"+remoteDevice.deviceClass.getMajorDeviceClass());
            //System.out.println("minor dev class :"+remoteDevice.deviceClass.getMinorDeviceClass());
            //System.out.println("service classes :"+remoteDevice.deviceClass.getServiceClasses());
            SDPClientChannel sdpChannel = new SDPClientChannel(remoteDevice, blue);
            bluetooth.connectL2CAPChannel(sdpChannel, remoteDevice, (short)0x0001);
            byte[] uuidList = { 0x35, 0x03, 0x19, 0x10, 0x02 };
            DataElement uuidListElement = new DataElement(uuidList);
            sdpChannel.send_SDP_ServiceSearchRequest((short)1, (short)14, uuidListElement);
        }
        while (serviceRecords == null) { Thread.sleep(1000); }
        for (int i = 0; i < serviceRecords.length; i++) {
            System.out.println("	" + serviceRecords[i]);
            int[] attrIDs = { 0xff00 }; //reversed the values for range... this avoids ranges starting with 00 to be tructated.
            try { serviceRecords[i].populateRecord(attrIDs); }
            catch (IOException e) { System.out.print("ServiceRecord.populateRecord(attrIDs) failed. " + e); }
        }
    }

    public BluetoothClient() throws BluetoothStateException {
        LocalDevice localDev = LocalDevice.getLocalDevice();
        System.out.println("Local Bluetooth Name is " + localDev.getFriendlyName());
        localDev.setDiscoverable(DiscoveryAgent.GIAC);
        DiscoveryAgent discovery = localDev.getDiscoveryAgent();
        discovery.startInquiry(DiscoveryAgent.GIAC, this);
    }

    /** @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass) */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device Discovered: " + btDevice.bdAddrLong);
        remoteDevice = btDevice;

        /*		try {
        System.out.println("Device Name: "+btDevice.getFriendlyName(false));
        } catch (IOException e) {
        e.printStackTrace();
        } */
    }

    /** @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[]) */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        System.out.println("Services Discovered: transID" + transID);
        serviceRecords = servRecord;
    }

    /** @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int) */
    public void serviceSearchCompleted(int transID, int respCode) {
        System.out.println("Services Search Complete: transID" + transID + " respCode:" + respCode);
    }

    /** @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int) */
    public void inquiryCompleted(int discType) { System.out.println("Inquiry Complete: discType" + discType); }
}

