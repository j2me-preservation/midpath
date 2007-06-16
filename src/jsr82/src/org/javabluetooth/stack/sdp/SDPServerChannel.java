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
import java.util.Vector;

import javax.bluetooth.DataElement;

import org.javabluetooth.stack.l2cap.L2CAPChannel;
import org.javabluetooth.util.Debug;

/** 
 * The SDPServerChannel provides SDP Services to remote Bluetooth Devices.
 * It ususes the SDPServer to resolve available services and returns the attributes contained in the matching ServiceRecords.
 * @see org.javabluetooth.stack.sdp.SDPServer
 * @see org.javabluetooth.stack.sdp.SDPLocalServiceRecord
 * @author Christian Lorenz
 */
public class SDPServerChannel extends L2CAPChannel {
    private static final byte SDP_ERROR_RESPONSE                    = 0x01;
    private static final byte SDP_SERVICE_SEARCH_REQUEST            = 0x02;
    private static final byte SDP_SERVICE_SEARCH_RESPONSE           = 0x03;
    private static final byte SDP_SERVICE_ATTRIBUTE_REQUEST         = 0x04;
    private static final byte SDP_SERVICE_ATTRIBUTE_RESPONSE        = 0x05;
    private static final byte SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST  = 0x06;
    private static final byte SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE = 0x07;
    private SDPServer sdpServer;

    public SDPServerChannel(SDPServer sdpServer) { this.sdpServer = sdpServer; }

    public void wasDisconnected() { }

    public void send_SDP_ErrorResponse(short transactionID, short errorCode) {
        Debug.println(2, "SDP: Sending Error Response. ");
        //TODO send error response
    }

    public void receiveL2CAPPacket(byte[] dataPacket) {
        //Debug.println("SDP: Received Packet: "+Debug.printByteArray(dataPacket));
        switch (dataPacket[0]) //pduID
        {
            case SDP_ERROR_RESPONSE:
                receive_SDP_ErrorResponse(dataPacket);
                break;
            case SDP_SERVICE_SEARCH_REQUEST:
                receive_SDP_ServiceSearchRequest(dataPacket);
                break;
            case SDP_SERVICE_ATTRIBUTE_REQUEST:
                receive_SDP_ServiceAttributeRequest(dataPacket);
                break;
            case SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST:
                receive_SDP_ServiceSearchAttributeRequest(dataPacket);
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
    }

    /** @param dataPacket */
    private void receive_SDP_ServiceSearchRequest(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Search Request:", dataPacket);
        short transactionID              = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
        DataElement serviceSearchPattern = new DataElement(dataPacket, 5);
        Vector handels                   = sdpServer.getServiceRecordHandels(serviceSearchPattern);
        short resultHandlesCount         = 0;
        if (handels != null) resultHandlesCount = (short)handels.size();
        short sdpLenght = (short)(5 + (resultHandlesCount * 4));
        byte[] serviceSearchResponse = new byte[10 + (resultHandlesCount * 4)];
        serviceSearchResponse[0] = 0x03;
        serviceSearchResponse[1] = dataPacket[1]; //transaction ID
        serviceSearchResponse[2] = dataPacket[2]; //transaction ID
        serviceSearchResponse[3] = (byte)((sdpLenght >> 8) & 0xff);
        serviceSearchResponse[4] = (byte)((sdpLenght) & 0xff);
        serviceSearchResponse[5] = (byte)((resultHandlesCount >> 8) & 0xff); //total service record count
        serviceSearchResponse[6] = (byte)((resultHandlesCount) & 0xff); //total service record count
        serviceSearchResponse[7] = serviceSearchResponse[5]; //current service record count
        serviceSearchResponse[8] = serviceSearchResponse[6]; //current service record count
        serviceSearchResponse[9 + (resultHandlesCount * 4)] = 0x00; //no continuation
        for (int i = 0; i < resultHandlesCount; i++) {
            Long serviceHandleLong    = (Long)handels.elementAt(i);
            long serviceHandle        = serviceHandleLong.longValue();
            byte[] serviceHandleBytes = {
                (byte)((serviceHandle >> 24) & 0xff), (byte)((serviceHandle >> 16) & 0xff), (byte)((serviceHandle >> 8) & 0xff),
                    (byte)((serviceHandle >> 0) & 0xff)
            };
            System.arraycopy(serviceHandleBytes, 0, serviceSearchResponse, 9 + (i * 4), 4);
        }
        try {
            Debug.println(4, "SDP: Sending Service Search Response:", serviceSearchResponse);
            sendL2CAPPacket(serviceSearchResponse);
        }
        catch (IOException e) { this.close(); }
    }

    /** @param dataPacket */
    private void receive_SDP_ServiceAttributeRequest(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Attibute Request:", dataPacket);
        short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
        long serviceRecordHandle = (long)(((long)dataPacket[5] & 0xff) | ((long)dataPacket[6] & 0xff) << 8 |
            ((long)dataPacket[7] & 0xff) << 16 | ((long)dataPacket[8] & 0xff) << 24);
        short maximumAttributeCount = (short)(((short)dataPacket[9] & 0xff) | ((short)dataPacket[10] & 0xff) << 8);
        DataElement attributeIDList = new DataElement(dataPacket, 11);
        byte[] attributeList        = sdpServer.getAttributes(serviceRecordHandle, attributeIDList);

        /*byte[] attributeList = {0x35, 0x59,
        0x09, 0x00, 0x00, 0x0a,0x00, 0x00, 0x01, 0x00,
        0x09, 0x00, 0x01, 0x35, 0x05, 0x1a, 0x00, 0x00, 0x11, 0x01,
        0x09, 0x00, 0x04, 0x35, 0x0a, 0x35, 0x08, 0x1a, 0x00, 0x00, 0x01, 0x00, 0x19, 0x00, 0x03,
        0x09, 0x00, 0x05, 0x35, 0x05, 0x1a, 0x00, 0x00, 0x10, 0x02,
        0x09, 0x00, 0x06, 0x35, 0x09, 0x09, 0x65, 0x6e, 0x09, 0x00, 0x6a, 0x09, 0x01, 0x00,
        0x09, 0x00, 0x09, 0x35, 0x0a, 0x35, 0x08, 0x1a, 0x00, 0x00, 0x11, 0x01, 0x09, 0x01, 0x00,
        0x09, 0x01, 0x00, 0x25, 0x0c, 0x54, 0x69, 0x6e, 0x69, 0x20, 0x53, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65
        };
        */

        /*DataElement resultAttributes = new DataElement(DataElement.DATSEQ);
        resultAttributes.addElement(new DataElement(DataElement.U_INT_2,0));
        resultAttributes.addElement(new DataElement(DataElement.U_INT_4,serviceRecordHandle));
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
        */

        short attributeByteCount        = (short)attributeList.length;
        short sdpLenght                 = (short)(3 + attributeByteCount);
        byte[] serviceAttributeResponse = new byte[attributeByteCount + 10];
        serviceAttributeResponse[0] = 0x05;
        serviceAttributeResponse[1] = dataPacket[1]; //transaction ID
        serviceAttributeResponse[2] = dataPacket[2]; //transaction ID
        serviceAttributeResponse[3] = (byte)((sdpLenght >> 8) & 0xff);
        serviceAttributeResponse[4] = (byte)((sdpLenght) & 0xff);
        serviceAttributeResponse[5] = (byte)(((attributeByteCount) >> 8) & 0xff);
        serviceAttributeResponse[6] = (byte)((attributeByteCount) & 0xff);
        serviceAttributeResponse[7 + attributeByteCount] = 0x00; //no continuation
        System.arraycopy(attributeList, 0, serviceAttributeResponse, 7, attributeByteCount);
        try {
            Debug.println(4, "SDP: Sending Service Attibute Response:", serviceAttributeResponse);
            sendL2CAPPacket(serviceAttributeResponse);
        }
        catch (IOException e) { this.close(); }
    }

    private void receive_SDP_ServiceSearchAttributeRequest(byte[] dataPacket) {
        Debug.println(4, "SDP: Received Service Search Attibute Request:", dataPacket);
        //byte[] dummyPacket={0x05, 0x00, 0x01, 0x00, 0x66, 0x00, 0x63, 0x35, 0x61, 0x09, 0x00, 0x00, 0x0a, 0x00, 0x01, 0x00,
        // 0x00, 0x09, 0x00, 0x01, 0x35, 0x03, 0x19, 0x11, 0x06, 0x09, 0x00, 0x04, 0x35, 0x11, 0x35, 0x03, 0x19, 0x01, 0x00,
        // 0x35, 0x05, 0x19, 0x00, 0x03, 0x08, 0x01, 0x35, 0x03, 0x19, 0x00, 0x08, 0x09, 0x00, 0x05, 0x35, 0x03, 0x19, 0x10,
        // 0x02, 0x09, 0x00, 0x06, 0x35, 0x09, 0x09, 0x65, 0x6e, 0x09, 0x00, 0x6a, 0x09, 0x01, 0x00, 0x09, 0x00, 0x08, 0x08,
        // (byte) 0xff, 0x09, 0x00, 0x09, 0x35, 0x08, 0x35, 0x06, 0x19, 0x11, 0x06, 0x09, 0x01, 0x00, 0x09, 0x01, 0x00, 0x25,
        // 0x0e, 0x46, 0x69, 0x6c, 0x65, 0x20, 0x54, 0x72, 0x61, 0x6e, 0x73, 0x66, 0x65, 0x72, 0x00, 0x00};
        //sendData(dummyPacket);
        short transactionID = (short)(((short)dataPacket[2] & 0xff) << 8 | ((short)dataPacket[1] & 0xff));
        //short dataLenght=(short)(((short) dataPacket[4] & 0xff) << 8 | ((short) dataPacket[3]& 0xff));
        //TODO service search attribute request
    }

    private void receive_SDP_UnknownPacket(byte[] dataPacket) {
        System.err.println("SDP: Received unknown Packet: " + Debug.printByteArray(dataPacket));
        short transactionID = (short)(((short)dataPacket[1] & 0xff) << 8 | ((short)dataPacket[2] & 0xff));
        send_SDP_ErrorResponse(transactionID, (short)1);
    }
}

