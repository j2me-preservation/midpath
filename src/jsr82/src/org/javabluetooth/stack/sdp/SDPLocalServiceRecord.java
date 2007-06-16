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
* Created on Jul 31, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.sdp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

/** 
 * This class implents the javax.bluetooth.ServiceRecord interface and provides
 * a representation of a Service Record as it is kept in the local Service
 * Discover Database. These Servcie Records may be accessed by remote bluetooth devices via SDP Commands.
 * @author Christian Lorenz
 */
public class SDPLocalServiceRecord implements ServiceRecord {
    public Hashtable attributes;

    public SDPLocalServiceRecord(short serviceClassID) {
        this.attributes = new Hashtable();
        DataElement serviceClassIDList = new DataElement(DataElement.DATSEQ);
        UUID serviceClassUUID = new UUID(serviceClassID);
        serviceClassIDList.addElement(new DataElement(DataElement.UUID, new UUID(0x1111)));
        attributes.put(new Integer(1), serviceClassIDList);
        DataElement protocolDescriptorList = new DataElement(DataElement.DATSEQ);
        DataElement l2capDescriptor = new DataElement(DataElement.DATSEQ);
        l2capDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(0x0100)));
        protocolDescriptorList.addElement(l2capDescriptor);
        DataElement rfcommDescriptor = new DataElement(DataElement.DATSEQ);
        rfcommDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(0x0003)));
        rfcommDescriptor.addElement(new DataElement(DataElement.U_INT_1, 1));
        protocolDescriptorList.addElement(rfcommDescriptor);
        attributes.put(new Integer(4), protocolDescriptorList);
        DataElement browseClassIDList = new DataElement(DataElement.DATSEQ);
        UUID browseClassUUID = new UUID(0x1002);
        browseClassIDList.addElement(new DataElement(DataElement.UUID, browseClassUUID));
        attributes.put(new Integer(5), browseClassIDList);
        DataElement languageBaseAttributeIDList = new DataElement(DataElement.DATSEQ);
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 25966));
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 106));
        languageBaseAttributeIDList.addElement(new DataElement(DataElement.U_INT_2, 256));
        attributes.put(new Integer(6), languageBaseAttributeIDList);
        DataElement profileDescriptorList = new DataElement(DataElement.DATSEQ);
        DataElement profileDescriptor = new DataElement(DataElement.DATSEQ);
        profileDescriptor.addElement(new DataElement(DataElement.UUID, new UUID(0x1111)));
        profileDescriptor.addElement(new DataElement(DataElement.U_INT_2, 256));
        profileDescriptorList.addElement(profileDescriptor);
        attributes.put(new Integer(9), profileDescriptorList);
        attributes.put(new Integer(256), new DataElement(DataElement.STRING, "Tini Service"));
    }

    /** @see javax.bluetooth.ServiceRecord#getAttributeValue(int) */
    public DataElement getAttributeValue(int attrID) { return (DataElement)attributes.get(new Integer(attrID)); }

    /** @see javax.bluetooth.ServiceRecord#getHostDevice() */
    public RemoteDevice getHostDevice() { return null; }

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
    public boolean populateRecord(int[] attrIDs) throws IOException {
        throw new RuntimeException("Can't populate a Local Record. Population is only possible for Records obtained from a RemoteDevice.");
    }

    /** @see javax.bluetooth.ServiceRecord#getConnectionURL(int, boolean) */
    public String getConnectionURL(int requiredSecurity, boolean mustBeMaster) {
        LocalDevice localDev;
        try { localDev = LocalDevice.getLocalDevice(); }
        catch (BluetoothStateException e) {
            e.printStackTrace();
            return null;
        }
        String url = "btl2cap://" + localDev.getBluetoothAddress() + ":";
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
        // TODO Auto-generated method stub
    }

    /** @see javax.bluetooth.ServiceRecord#setAttributeValue(int, javax.bluetooth.DataElement) */
    public boolean setAttributeValue(int attrID, DataElement attrValue) {
        attributes.put(new Integer(attrID), attrValue);
        return true;
    }

    public byte[] toByteArray() {
        DataElement resultAttributes = new DataElement(DataElement.DATSEQ);
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            Integer key = (Integer)keys.nextElement();
            DataElement value = (DataElement)attributes.get(key);
            resultAttributes.addElement(new DataElement(DataElement.U_INT_2, key.intValue()));
            resultAttributes.addElement(value);
        }
        return resultAttributes.toByteArray();
    }
}

