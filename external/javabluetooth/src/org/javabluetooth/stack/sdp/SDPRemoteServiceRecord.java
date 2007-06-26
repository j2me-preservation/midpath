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
* Created on Jul 17, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.sdp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;

/** 
 * This class implements the javax.bluetooth.ServiceRecord interface.
 * It is used to represent a Service found on a Remote Bluetooth Device, and
 * contains the service attributes and instructions on how to connect to the remote service.
 * @author Christian Lorenz
 */
public class SDPRemoteServiceRecord implements ServiceRecord {
    private RemoteDevice remoteDevice;
    private int serviceRecordHandle;
    public Hashtable attributes;
    public boolean isPopulated = false;


    public SDPRemoteServiceRecord(RemoteDevice remoteDevice, int serviceRecordHandle) {
        this.remoteDevice = remoteDevice;
        this.serviceRecordHandle = serviceRecordHandle;
        this.attributes = new Hashtable();
    }

    /** @see javax.bluetooth.ServiceRecord#getAttributeValue(int) */
    public DataElement getAttributeValue(int attrID) { return (DataElement)attributes.get(new Integer(attrID)); }

    /** @see javax.bluetooth.ServiceRecord#getHostDevice() */
    public RemoteDevice getHostDevice() { return remoteDevice; }

    /** @see javax.bluetooth.ServiceRecord#getAttributeIDs() */
    public int[] getAttributeIDs() {
        Enumeration keys = attributes.elements();
        Vector keyList = new Vector();
        while (keys.hasMoreElements()) { keyList.addElement(keys.nextElement()); }
        int[] returnArray = new int[keyList.size()];
        for (int i = 0; i < returnArray.length; i++) { returnArray[i] = ((Integer)keyList.elementAt(i)).intValue(); }
        return returnArray;
    }
    
    /** @see javax.bluetooth.ServiceRecord#populateRecord(int[]) */
    public boolean populateRecord(SDPClientChannel sdpChannel, int[] attrIDs) throws IOException {
        BluetoothStack bluetooth;
        try {
            bluetooth = BluetoothStack.getBluetoothStack();
//            SDPClientChannel sdpChannel = new SDPClientChannel(remoteDevice, null);
//            bluetooth.connectL2CAPChannel(sdpChannel, remoteDevice, (short)0x0001);
            //TODO fill propper attrIDs this ignores the parameter and gets all available
            byte[] attributeList = { 0x35, 0x05, 0x0a, 0x00, 0x00, (byte)0xff, (byte)0xff };
            DataElement attributeListElement = new DataElement(attributeList);
            isPopulated = false;
            sdpChannel.send_SDP_ServiceAttributeRequest((short)0x00, (short)0xff, serviceRecordHandle, attributeListElement);
            int timeout = 0;
            while (!isPopulated) {
                try {
                    Thread.sleep(1000);
                    timeout++;
                }
                catch (InterruptedException e) { }
                if (timeout == 100) throw new IOException("ServiceRecord.populateRecord(attrIDs) timed out.");
            }
            //sdpChannel.close();
            if (attributes.size() > 0) return true;
            else return false;
        }
        catch (HCIException e) { throw new IOException("ServiceRecord.populateRecord(attrIDs) failed. " + e); }
    }

//    /** @see javax.bluetooth.ServiceRecord#populateRecord(int[]) */
//    public boolean populateRecord(int[] attrIDs) throws IOException {
//        BluetoothStack bluetooth;
//        try {
//            bluetooth = BluetoothStack.getBluetoothStack();
//            SDPClientChannel sdpChannel = new SDPClientChannel(remoteDevice, null);
//            bluetooth.connectL2CAPChannel(sdpChannel, remoteDevice, (short)0x0001);
//            //TODO fill propper attrIDs this ignores the parameter and gets all available
//            byte[] attributeList = { 0x35, 0x05, 0x0a, 0x00, 0x00, (byte)0xff, (byte)0xff };
//            DataElement attributeListElement = new DataElement(attributeList);
//            isPopulated = false;
//            sdpChannel.send_SDP_ServiceAttributeRequest((short)0x00, (short)0xff, serviceRecordHandle, attributeListElement);
//            int timeout = 0;
//            while (!isPopulated) {
//                try {
//                    Thread.sleep(1000);
//                    timeout++;
//                }
//                catch (InterruptedException e) { }
//                if (timeout == 100) throw new IOException("ServiceRecord.populateRecord(attrIDs) timed out.");
//            }
//            sdpChannel.close();
//            if (attributes.size() > 0) return true;
//            else return false;
//        }
//        catch (HCIException e) { throw new IOException("ServiceRecord.populateRecord(attrIDs) failed. " + e); }
//    }

    /** @see javax.bluetooth.ServiceRecord#getConnectionURL(int, boolean) */
    public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
        //"btspp://0050CD00321B:3;authenticate=true;encrypt=false;master=true",	
        if (remoteDevice == null) throw new IllegalArgumentException("Service Record is not propperly populted.");
        String url = "btl2cap://" + remoteDevice.getBluetoothAddress() + ":";
        DataElement protocolDescriptorListElement = (DataElement)attributes.get(new Integer(4));
        if (protocolDescriptorListElement == null)
            throw new IllegalArgumentException("Service Record is not propperly populated. Protocol Descriptor is missing.");
        Enumeration protocolDescriptorList = (Enumeration)protocolDescriptorListElement.getValue();
        while (protocolDescriptorList.hasMoreElements()) {
            DataElement protocolDescriptorElement = (DataElement)protocolDescriptorList.nextElement();
            if (protocolDescriptorElement == null)
                throw new IllegalArgumentException("Service Record is not propperly populated. Protocol Descriptor is missing.");
            Enumeration protocolParameterList = (Enumeration)protocolDescriptorElement.getValue();
            if (protocolParameterList.hasMoreElements()) {
                DataElement protocolDescriptor = (DataElement)protocolParameterList.nextElement();
                if (protocolDescriptor != null) {
                    if (protocolDescriptor.getDataType() == DataElement.UUID) {
                        UUID protocolDescriptorUUID = (UUID)protocolDescriptor.getValue();
                        if (protocolDescriptorUUID.toLong() == 0x0100) //is L2CAP
                        {
                            if (protocolParameterList.hasMoreElements()) {
                                DataElement protocolPSMElement = (DataElement)protocolParameterList.nextElement();
                                {
                                    if (protocolPSMElement != null) {
                                        if (protocolPSMElement.getDataType() == DataElement.UUID) {
                                            UUID psm = (UUID)protocolPSMElement.getValue();
                                            url += psm.toString() + ";";
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        else //is not L2CAP
                        {
                            url += protocolDescriptorUUID.toString() + ";";
                            while (protocolParameterList.hasMoreElements()) {
                                DataElement parameter = (DataElement)protocolParameterList.nextElement();
                                url += parameter.toString() + ";";
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (requiredSecurity == ServiceRecord.AUTHENTICATE_ENCRYPT) url += "authenticate=true;encrypt=true;";
        else if (requiredSecurity == ServiceRecord.AUTHENTICATE_NOENCRYPT) url += "authenticate=true;encrypt=false;";
        if (requiredSecurity == ServiceRecord.NOAUTHENTICATE_NOENCRYPT) url += "authenticate=false;encrypt=false;";
        url += "master=" + mustBeMaster;
        return url;
    }

    /** @see javax.bluetooth.ServiceRecord#setDeviceServiceClasses(int) */
    public void setDeviceServiceClasses(int classes) {
        // TODO setDeviceServiceClass
    }

    /** @see javax.bluetooth.ServiceRecord#setAttributeValue(int, javax.bluetooth.DataElement) */
    public boolean setAttributeValue(int attrID, DataElement attrValue) {
        //if(attrID==0) throw new IllegalArgumentException("Can't modify ServiceRecordHandel.");
        //if(remoteDevice!=null) throw new RuntimeException("Trying to modify the ServiceRecord of a RemoteDevice.");
        attributes.put(new Integer(attrID), attrValue);
        return true;
    }
}

