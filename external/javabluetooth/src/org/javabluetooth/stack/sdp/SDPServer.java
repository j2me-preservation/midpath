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
* Created on Jun 28, 2003
* by Christian Lorenz
*/

package org.javabluetooth.stack.sdp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.UUID;

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.stack.l2cap.L2CAPChannelFactory;
import org.javabluetooth.stack.l2cap.L2CAPLink;

/** 
 * The SDPServer managed the local Service Discover Database. Services
 * may be registered and Service Lookups may be performed through this class.
 * @author Christian Lorenz
 */
public class SDPServer {
    private static SDPServer sdpServer;
    private Hashtable services;
    private Hashtable serviceRecords;
    private Hashtable uuidMap;
    private long serviceHandleCounter = 0;


    private SDPServer() {
        services = new Hashtable();
        serviceRecords = new Hashtable();
        uuidMap = new Hashtable();
        services.put(new Long(0x0001), new SDPServerChannelCreator(this));
    }

    public static SDPServer getSDPServer() {
        if (sdpServer == null) sdpServer = new SDPServer();
        return sdpServer;
    }

    public L2CAPChannel resolveAndCreateL2CAPChannel(long psm, L2CAPLink link, short localChannelID, short remoteChannelID) {
        L2CAPChannelFactory channelCreator = (L2CAPChannelFactory)services.get(new Long(psm));
        return channelCreator.newL2CAPChannel();
    }

    public Vector getServiceRecordHandels(DataElement serviceSearchPattern) {
        Vector serviceRecordHandles = new Vector();
        Enumeration elements = (Enumeration)serviceSearchPattern.getValue();
        while (elements.hasMoreElements()) {
            DataElement element = (DataElement)elements.nextElement();
            UUID uuid = (UUID)element.getValue();
            System.out.println("getSRH for " + uuid);
            serviceRecordHandles = (Vector)uuidMap.get(uuid);
            break;
            //TODO this only takes the first UUID into consideration and not all like it's supposed to...
        }
        return serviceRecordHandles;
    }

    public byte[] getAttributes(long serviceRecordHandle, DataElement attributeIDList) {
        // TODO parse attributes
        Enumeration attributes = (Enumeration)attributeIDList.getValue();
        return (byte[]) serviceRecords.get(new Long(serviceRecordHandle));
    }

    /**
     * @param channel
     * @param serviceUUID
     * @param browseUUID
     * @param serviceRecord
     */
    public boolean registerService(L2CAPChannelFactory channelFactory, short serviceUUIDshort, short browseUUIDshort,
        byte[] serviceRecord) {
            Long psmKey = new Long(serviceUUIDshort);
            if (services.contains(psmKey)) { //return false;	
            }
            services.put(psmKey, channelFactory);
            Long serviceHandle = new Long(serviceHandleCounter++);
            DataElement serviceRecordElement = new DataElement(DataElement.DATSEQ);
            serviceRecordElement.addElement(new DataElement(DataElement.U_INT_2, 0));
            serviceRecordElement.addElement(new DataElement(DataElement.U_INT_4, serviceHandle.longValue()));
            DataElement recordElements = new DataElement(serviceRecord);
            Enumeration elements = (Enumeration)recordElements.getValue();
            while (elements.hasMoreElements()) {
                DataElement element = (DataElement)elements.nextElement();
                serviceRecordElement.addElement(element);
            }
            serviceRecords.put(serviceHandle, serviceRecordElement.toByteArray());
            UUID serviceUUID = new UUID(serviceUUIDshort);
            Vector serviceHandleVector = (Vector)uuidMap.get(serviceUUID);
            if (serviceHandleVector == null) {
                serviceHandleVector = new Vector();
                uuidMap.put(serviceUUID, serviceHandleVector);
            }
            serviceHandleVector.addElement(serviceHandle);
            UUID browseUUID = new UUID(browseUUIDshort);
            Vector browseHandleVector = (Vector)uuidMap.get(browseUUID);
            if (browseHandleVector == null) {
                browseHandleVector = new Vector();
                uuidMap.put(browseUUID, browseHandleVector);
            }
            browseHandleVector.addElement(serviceHandle);
            return true;
    }

    public void unregisterService(UUID uuid, Long handle) {
        Vector serviceHandleVector = (Vector)uuidMap.get(uuid);
        if (serviceHandleVector != null) {
            serviceHandleVector.removeElement(handle);
            if (serviceHandleVector.size() == 0) uuidMap.remove(uuid);
        }
        services.remove(uuid);
        serviceRecords.remove(handle);
    }
}

