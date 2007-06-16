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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.util.Debug;

/** 
 * This class implements a L2CAPChannel for the Service Discovery Protocol.
 * It provides the capabilites to send service search and attribute requests, and parses the received results.
 * @author Christian Lorenz
 */
public class SDPClientChannel extends L2CAPChannel {
    private static final byte SDP_ERROR_RESPONSE                    = 0x01;
    private static final byte SDP_SERVICE_SEARCH_REQUEST            = 0x02;
    private static final byte SDP_SERVICE_SEARCH_RESPONSE           = 0x03;
    private static final byte SDP_SERVICE_ATTRIBUTE_REQUEST         = 0x04;
    private static final byte SDP_SERVICE_ATTRIBUTE_RESPONSE        = 0x05;
    private static final byte SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST  = 0x06;
    private static final byte SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE = 0x07;
    private RemoteDevice remoteDevice;
    private DiscoveryListener discoveryListener;

    /**
     * @param remoteDevice
     * @param discoveryAgent
     */
    public SDPClientChannel(RemoteDevice remoteDevice, DiscoveryListener discoveryListener) {
        this.remoteDevice = remoteDevice;
        this.discoveryListener = discoveryListener;
    }

    public void wasDisconnected() { }

    public void send_SDP_ErrorResponse(short transactionID, short errorCode) {
        Debug.println(2, "SDP: Sending Error Response. ");
        //TODO send error response
    }

    public void send_SDP_ServiceSearchRequest(short transactionID, short maxRecordsReturned,
        DataElement uuidListElement) throws IOException {
            byte[] uuidList             = uuidListElement.toByteArray();
            short length                = (short)(uuidList.length + 3);
            byte[] serviceSearchRequest = new byte[length + 5];
            serviceSearchRequest[0] = 0x02;
            serviceSearchRequest[1] = (byte)((transactionID >> 8) & 0xff);
            serviceSearchRequest[2] = (byte)((transactionID) & 0xff);
            serviceSearchRequest[3] = (byte)((length >> 8) & 0xff);
            serviceSearchRequest[4] = (byte)((length) & 0xff);
            serviceSearchRequest[5 + uuidList.length] = (byte)((maxRecordsReturned >> 8) & 0xff);
            serviceSearchRequest[6 + uuidList.length] = (byte)((maxRecordsReturned) & 0xff);
            serviceSearchRequest[7 + uuidList.length] = 0x00; //no continuation
            System.arraycopy(uuidList, 0, serviceSearchRequest, 5, uuidList.length);
            Debug.println(4, "SDP: Sending Service Search Request:", serviceSearchRequest);
            this.sendL2CAPPacket(serviceSearchRequest);
    }

    public void send_SDP_ServiceAttributeRequest(short transactionID, short maxAttributesReturned, int serviceRecordHandle,
        DataElement attributeListElement) throws IOException {
            byte[] attributeList           = attributeListElement.toByteArray();
            short length                   = (short)(attributeList.length + 7);
            byte[] serviceAttributeRequest = new byte[length + 5];
            serviceAttributeRequest[0] = 0x04;
            serviceAttributeRequest[1] = (byte)((transactionID >> 8) & 0xff);
            serviceAttributeRequest[2] = (byte)((transactionID) & 0xff);
            serviceAttributeRequest[3] = (byte)((length >> 8) & 0xff);
            serviceAttributeRequest[4] = (byte)((length) & 0xff);
            serviceAttributeRequest[5] = (byte)((serviceRecordHandle >> 24) & 0xff);
            serviceAttributeRequest[6] = (byte)((serviceRecordHandle >> 16) & 0xff);
            serviceAttributeRequest[7] = (byte)((serviceRecordHandle >> 8) & 0xff);
            serviceAttributeRequest[8] = (byte)((serviceRecordHandle) & 0xff);
            serviceAttributeRequest[9] = (byte)((maxAttributesReturned >> 8) & 0xff);
            serviceAttributeRequest[10] = (byte)((maxAttributesReturned) & 0xff);
            serviceAttributeRequest[11 + attributeList.length] = 0x00; //no continuation state
            System.arraycopy(attributeList, 0, serviceAttributeRequest, 11, attributeList.length);
            Debug.println(4, "SDP: Sending Service Attribute Request:", serviceAttributeRequest);
            this.sendL2CAPPacket(serviceAttributeRequest);
    }

    public void send_SDP_ServiceSearchAttributeRequest(short transactionID, short maxAttributeByteCount,
        DataElement uuidListElement, DataElement attributeListElement) throws IOException {
            byte[] uuidList                      = uuidListElement.toByteArray();
            byte[] attributeList                 = attributeListElement.toByteArray();
            short length                         = (short)(uuidList.length + attributeList.length + 3);
            byte[] serviceSearchAttributeRequest = new byte[length + 5];
            serviceSearchAttributeRequest[0] = 0x06;
            serviceSearchAttributeRequest[1] = (byte)((transactionID >> 8) & 0xff);
            serviceSearchAttributeRequest[2] = (byte)((transactionID) & 0xff);
            serviceSearchAttributeRequest[3] = (byte)((length >> 8) & 0xff);
            serviceSearchAttributeRequest[4] = (byte)((length) & 0xff);
            serviceSearchAttributeRequest[5 + uuidList.length] = (byte)((maxAttributeByteCount >> 8) & 0xff);
            serviceSearchAttributeRequest[6 + uuidList.length] = (byte)((maxAttributeByteCount) & 0xff);
            serviceSearchAttributeRequest[7 + uuidList.length + attributeList.length] = 0x00; //no continuation
            System.arraycopy(uuidList, 0, serviceSearchAttributeRequest, 5, uuidList.length);
            System.arraycopy(attributeList, 0, serviceSearchAttributeRequest, 7 + uuidList.length, attributeList.length);
            Debug.println(4, "SDP: Sending Search Service Attribute Request. ", serviceSearchAttributeRequest);
            this.sendL2CAPPacket(serviceSearchAttributeRequest);
    }

    public void receiveL2CAPPacket(byte[] dataPacket) {
        //Debug.println("SDP: Received Packet: "+Debug.printByteArray(dataPacket));
        switch (dataPacket[0]) //pduID
        {
            case SDP_ERROR_RESPONSE:
                receive_SDP_ErrorResponse(dataPacket);
                break;
            case SDP_SERVICE_SEARCH_RESPONSE:
                receive_SDP_ServiceSearchResponse(dataPacket);
                break;
            case SDP_SERVICE_ATTRIBUTE_RESPONSE:
                receive_SDP_ServiceAttributeResponse(dataPacket);
                break;
            case SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE:
                receive_SDP_ServiceSearchAttributeResponse(dataPacket);
                break;
            default:
                receive_SDP_UnknownPacket(dataPacket);
        }
    }

    private void receive_SDP_ErrorResponse(byte[] dataPacket) {
        short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
        short errorCode = (short)(((short)dataPacket[5] & 0xff) << 8 | ((short)dataPacket[6] & 0xff));
        switch (errorCode) {
            case 1:
                System.err.println("SDP: Received Error Response: Invalid/unsupported SDP version. (transactionID=" +
                    transactionID + ")");
                break;
            case 2:
                System.err.println("SDP: Received Error Response: Invalid Service Record Handle. (transactionID=" +
                    transactionID + ")");
                break;
            case 3:
                System.err.println("SDP: Received Error Response: Invalid request syntax. (transactionID=" +
                    transactionID + ")");
                break;
            case 4:
                System.err.println("SDP: Received Error Response: Invalid PDU Size. (transactionID=" + transactionID + ")");
                break;
            case 5:
                System.err.println("SDP: Received Error Response: Invalid Continuation State. (transactionID=" +
                    transactionID + ")");
                break;
            case 6:
                System.err.println("SDP: Received Error Response: Insufficient Resources to satisfy Request. (transactionID=" +
                    transactionID + ")");
                break;
            default:
                System.err.println("SDP: Received Error Response: Error Code " + errorCode + " (transactionID=" +
                    transactionID + ")");
        }
        if (discoveryListener != null) {
            discoveryListener.serviceSearchCompleted(transactionID, DiscoveryListener.SERVICE_SEARCH_ERROR);
        }
    }

    /** @param dataPacket */
    private void receive_SDP_ServiceSearchResponse(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Search Response:", dataPacket);
        if (remoteDevice != null) {
            short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
            short serviceRecordCount = (short)(((short)dataPacket[5] & 0xff) << 8 | ((short)dataPacket[6] & 0xff));
            //byte[] attributeList={0x35, 0x05, 0x0a, 0x00, 0x00, (byte) 0xff, (byte) 0xff};
            //DataElement attributeListElement = new DataElement(attributeList,0);
            Vector servicesDiscovered = new Vector();
            Hashtable serviceMap = new Hashtable();
            for (int i = 0; i < serviceRecordCount; i++) {
                int serviceRecordHandle = (int)(((int)dataPacket[9 + (i * 4)] & 0xff) << 24 |
                    ((int)dataPacket[10 + (i * 4)] & 0xff) << 16 | ((int)dataPacket[11 + (i * 4)] & 0xff) << 8 |
                    ((int)dataPacket[12 + (i * 4)] & 0xff));
                Integer serviceHandle = new Integer(serviceRecordHandle);
                ServiceRecord serviceRecord = (ServiceRecord)remoteDevice.serviceRecords.get(serviceHandle);
                if (serviceRecord == null) serviceRecord = new SDPRemoteServiceRecord(remoteDevice, serviceRecordHandle);
                servicesDiscovered.addElement(serviceRecord);
                serviceMap.put(serviceHandle, serviceRecord);
            }
            remoteDevice.serviceRecords = serviceMap;
            if (discoveryListener != null) {
                if (serviceRecordCount == 0)
                    discoveryListener.serviceSearchCompleted(transactionID, DiscoveryListener.SERVICE_SEARCH_NO_RECORDS);
                else {
                    ServiceRecord[] serviceArray = new ServiceRecord[servicesDiscovered.size()];
                    for (int i = 0; i < serviceArray.length; i++) {
                        serviceArray[i] = (ServiceRecord)servicesDiscovered.elementAt(i);
                    }
                    discoveryListener.servicesDiscovered(transactionID, serviceArray);
                    discoveryListener.serviceSearchCompleted(transactionID, DiscoveryListener.SERVICE_SEARCH_COMPLETED);
                }
            }
        }
    }

    private void receive_SDP_ServiceAttributeResponse(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Attibute Response:", dataPacket);
        if (remoteDevice != null) {
            short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
            //short attributeListByteCount = (short)(((short) dataPacket[5] & 0xff) << 8 | ((short) dataPacket[6]& 0xff));
            SDPRemoteServiceRecord serviceRecord = null;
            DataElement attributeList = new DataElement(dataPacket, 7);
            //System.out.println("AttributeList : "+attributeList);
            Enumeration attributeElements = (Enumeration)attributeList.getValue();
            while (attributeElements.hasMoreElements()) {
                DataElement attrIDElement    = (DataElement)attributeElements.nextElement();
                DataElement attrValueElement = (DataElement)attributeElements.nextElement();
                int attrID                   = (int)attrIDElement.getLong();
                if (attrID == 0) {
                    serviceRecord = (SDPRemoteServiceRecord)
                        remoteDevice.serviceRecords.get(new Integer((int)attrValueElement.getLong()));
                }
                if (serviceRecord != null) serviceRecord.setAttributeValue(attrID, attrValueElement);
            }
            if (serviceRecord != null) serviceRecord.isPopulated = true;
        }
    }

    private void receive_SDP_ServiceSearchAttributeResponse(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Attibute Response:", dataPacket);
        if ((remoteDevice != null)) {
            short transactionID                   = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
            DataElement serviceAttributeListElement = new DataElement(dataPacket, 7);
            Enumeration serviceAttributeList      = (Enumeration)serviceAttributeListElement.getValue();
            while (serviceAttributeList.hasMoreElements()) {
                DataElement attributeList            = (DataElement)serviceAttributeList.nextElement();
                SDPRemoteServiceRecord serviceRecord = null;
                Enumeration attributeElements        = (Enumeration)attributeList.getValue();
                while (attributeElements.hasMoreElements()) {
                    DataElement attrIDElement    = (DataElement)attributeElements.nextElement();
                    DataElement attrValueElement = (DataElement)attributeElements.nextElement();
                    int attrID                   = (int)attrIDElement.getLong();
                    if (attrID == 0) {
                        serviceRecord = (SDPRemoteServiceRecord)
                            remoteDevice.serviceRecords.get(new Integer((int)attrValueElement.getLong()));
                        if (serviceRecord == null)
                            serviceRecord = new SDPRemoteServiceRecord(remoteDevice, (int)attrValueElement.getLong());
                    }
                    if (serviceRecord != null) { serviceRecord.setAttributeValue(attrID, attrValueElement); }
                }
                if (serviceRecord != null) {
                    serviceRecord.isPopulated = true;
                    if (discoveryListener != null) {
                        ServiceRecord[] records = { serviceRecord };
                        discoveryListener.servicesDiscovered(transactionID, records);
                    }
                }
            }
            if (discoveryListener != null) {
                discoveryListener.serviceSearchCompleted(transactionID, DiscoveryListener.SERVICE_SEARCH_COMPLETED);
            }
        }
    }

    private void receive_SDP_UnknownPacket(byte[] dataPacket) {
        System.err.println("SDP: Received unknown Packet: " + Debug.printByteArray(dataPacket));
        short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
        send_SDP_ErrorResponse(transactionID, (short)1);
    }
}

