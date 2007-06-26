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


package javax.bluetooth;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.sdp.SDPClientChannel;

/** 
 * The <code>DiscoveryAgent</code> class provides methods to perform
 * device and service discovery.  A local device must have only one
 * <code>DiscoveryAgent</code> object.  This object must be retrieved by a call to <code>getDiscoveryAgent()</code> on the
 * <code>LocalDevice</code> object. <H3>Device Discovery</H3> There are two ways to discover devices.  First, an application
 * may use <code>startInquiry()</code> to start an inquiry to find devices
 * in proximity to the local device. Discovered devices are returned
 * via the <code>deviceDiscovered()</code> method of the interface <code>DiscoveryListener</code>.  The second way to
 * discover devices is via the <code>retrieveDevices()</code> method.
 * This method will return devices that have been discovered via a
 * previous inquiry or devices that are classified as pre-known. (Pre-known devices are those devices that are defined in the
 * Bluetooth Control Center as devices this device frequently contacts.)
 * The <code>retrieveDevices()</code> method does not perform an
 * inquiry, but provides a quick way to get a list of devices that may be in the area. <H3>Service Discovery</H3>
 * The <code>DiscoveryAgent</code> class also encapsulates the
 * functionality provided by the service discovery application profile.
 * The class provides an interface for an application to search and
 * retrieve attributes for a particular service.  There are two ways to
 * search for services.  To search for a service on a single device,
 * the <code>searchServices()</code> method should be used.  On the
 * other hand, if you don't care which device a service is on, the
 * <code>selectService()</code> method does a service search on a set of remote devices.
 * @author Christian Lorenz
 */
public class DiscoveryAgent {
    /** Takes the device out of discoverable mode. <P> The value of <code>NOT_DISCOVERABLE</code> is 0x00 (0). */
    public static final int NOT_DISCOVERABLE = 0;

    /**
     * The inquiry access code for General/Unlimited Inquiry Access Code
     * (GIAC). This is used to specify the type of inquiry to complete or respond to. <P>
     * The value of <code>GIAC</code> is 0x9E8B33 (10390323). This value
     * is defined in the Bluetooth Assigned Numbers document.
     */
    public static final int GIAC = 0x9E8B33;

    /**
     * The inquiry access code for Limited Dedicated Inquiry Access Code
     * (LIAC). This is used to specify the type of inquiry to complete or respond to. <P>
     * The value of <code>LIAC</code> is 0x9E8B00 (10390272). This value
     * is defined in the Bluetooth Assigned Numbers document.
     */
    public static final int LIAC = 0x9E8B00;

    /**
     * Used with the <code>retrieveDevices()</code> method to return
     * those devices that were found via a previous inquiry.  If no
     * inquiries have been started, this will cause the method to return <code>null</code>. <P>
     * The value of <code>CACHED</code> is 0x00 (0).
     * @see #retrieveDevices
     */
    public static final int CACHED = 0x00;

    /**
     * Used with the <code>retrieveDevices()</code> method to return
     * those devices that are defined to be pre-known devices.  Pre-known
     * devices are specified in the BCC.  These are devices that are
     * specified by the user as devices with which the local device will frequently communicate. <P>
     * The value of <code>PREKNOWN</code> is 0x01 (1).
     * @see #retrieveDevices
     */
    public static final int PREKNOWN = 0x01;
    private BluetoothStack bluetoothStack;
    private Vector listeners;
    private Vector cachedRemoteDevices;
    private Vector foundRemoteDevices;
    private Hashtable remoteDevices;
    private boolean isInquiring;
    private int transactionID = 0;

    /**
     * Creates a <code>DiscoveryAgent</code> object.
     *@param bluetoothStack The Instance of the <code>BluetoothStack</code> associated with this <code>DiscoveryAgent</code>.
     * Christian Lorenz: changed constructor to public and added <code>BluetoothStack</code> as a parameter.
     */
    public DiscoveryAgent(BluetoothStack bluetoothStack) {
        this.bluetoothStack = bluetoothStack;
        this.listeners = new Vector();
        this.cachedRemoteDevices = new Vector();
        this.remoteDevices = new Hashtable();
        this.isInquiring = false;
    }

    /*  End of the constructor method   */

    /**
     * Returns an array of Bluetooth devices that have either been found
     * by the local device during previous inquiry requests or been
     * specified as a pre-known device depending on the argument. The list
     * of previously found devices is maintained by the implementation of
     * this API. (In other words, maintenance of the list of previously
     * found devices is an implementation detail.) A device can be set as a pre-known device in the Bluetooth Control Center.
     * @param option <code>CACHED</code> if previously found devices
     * should be returned; <code>PREKNOWN</code> if pre-known devices should be returned
     * @return an array containing the Bluetooth devices that were
     * previously found if <code>option</code> is <code>CACHED</code>; an array of devices that are pre-known devices if
     * <code>option</code> is <code>PREKNOWN</code>; <code>null</code> if no devices meet the criteria
     * Christian Lorenz: This implemenation does not support <code>PREKNOWN</code> devices and will always return
     * <code>null</code>. Use <code>CACHED</code> instead.
     * @exception IllegalArgumentException if <code>option</code> is not <code>CACHED</code> or <code>PREKNOWN</code>
     */
    public RemoteDevice[] retrieveDevices(int option) {
        if (option == PREKNOWN) return null;
        else if (option == CACHED) {
            RemoteDevice[] devices = null;
            synchronized(cachedRemoteDevices) {
                int deviceCount = cachedRemoteDevices.size();
                if (deviceCount > 0) {
                    devices = new RemoteDevice[deviceCount];
                    cachedRemoteDevices.copyInto(devices);
                }
            }
            return devices;
        }
        else throw new IllegalArgumentException("DiscoveryAgent.retrieveDevices: Only CACHED and PREKNOWN are vaild values. ");
    }

    /*  End of the method retrieveDevices   */

    /**
     * Places the device into inquiry mode.  The length of the inquiry is
     * implementation dependent. This method will search for devices with the
     * specified inquiry access code. Devices that responded to the inquiry are returned to the application via the method
     * <code>deviceDiscovered()</code> of the interface <code>DiscoveryListener</code>. The <code>cancelInquiry()</code>
     * method is called to stop the inquiry.
     * @see #cancelInquiry
     * @see #GIAC
     * @see #LIAC
     * @param accessCode  the type of inquiry to complete
     * @param listener the event listener that will receive device discovery events
     * @return <code>true</code> if the inquiry was started; <code>false</code> if the inquiry was not started because the
     * <code>accessCode</code> is not supported
     * @exception IllegalArgumentException if the access code provided
     * is not <code>LIAC</code>, <code>GIAC</code>, or in the range 0x9E8B00 to 0x9E8B3F
     * @exception NullPointerException if <code>listener</code> is <code>null</code>
     * @exception BluetoothStateException if the Bluetooth device does
     * not allow an inquiry to be started due to other operations that are being performed by the device
     */
    public boolean startInquiry(int accessCode, DiscoveryListener listener) throws BluetoothStateException {
        if (listener != null) { if (!listeners.contains(listener)) listeners.addElement(listener); }
        if (isInquiring) {
            Enumeration remoteDevices = foundRemoteDevices.elements();
            while (remoteDevices.hasMoreElements()) {
                RemoteDevice remoteDevice = (RemoteDevice)remoteDevices.nextElement();
                if ((remoteDevice != null) && (listener != null))
                    listener.deviceDiscovered(remoteDevice, remoteDevice.deviceClass);
            }
            return true;
        }
        else {
            isInquiring = true;
            foundRemoteDevices = new Vector();
            bluetoothStack.registerDiscoveryAgent(this);
            try {
                byte result = bluetoothStack.send_HCI_LC_Inquiry(GIAC, 10, (byte)0x00);
                if (result == 0) return true;
                else return false;
            }
            catch (HCIException e) { throw new BluetoothStateException("HCIException: DiscoveryAgent.startInquiry(): " + e); }
        }
    }

    /*  End of the method  startInquiry     */

    /**
     * Removes the device from inquiry mode. <P> An <code>inquiryCompleted()</code> event will occur with a type of
     * <code>INQUIRY_TERMINATED</code> as a result of calling this method.  After receiving this
     * event, no further <code>deviceDiscovered()</code> events will occur as a result of this inquiry. <P>
     * This method will only cancel the inquiry if the <code>listener</code> provided is the listener that started
     * the inquiry. Christian Lorenz: Acctually this implementation will not remove the device
     * from inquiry mode, or cancel any running inquiry. It will only prevent the <code>listener</code> from receiving any
     * further <code>deviceDiscovered()</code> events. This change in behavior is due to the distributed enviroment  which
     * allows multiple <code>DiscoveryAgent</code> to be running on different hosts.
     * @param listener the listener that is receiving inquiry events
     * @return <code>true</code> if the inquiry was canceled; otherwise
     * <code>false</code> if the inquiry was not canceled or if the inquiry was not started using <code>listener</code>
     * Christian Lorenz: This implementation always returns <code>true</code>.
     * @exception NullPointerException if <code>listener</code> is <code>null</code>
     */
    public boolean cancelInquiry(DiscoveryListener listener) {
        if (listener == null) throw new NullPointerException("DiscoveryAgent.cancelInquiry: DiscoveryListener is null.");
        listeners.removeElement(listener);
        listener.inquiryCompleted(DiscoveryListener.INQUIRY_TERMINATED);
        return true;
    }

    /*  End of the method cancelInquiry */

    /**
     * Searches for services on a remote Bluetooth device that have all the
     * UUIDs specified in <code>uuidSet</code>.  Once the service is found,
     * the attributes specified in <code>attrSet</code> and the default attributes are retrieved.  The default attributes are
     * ServiceRecordHandle (0x0000), ServiceClassIDList (0x0001), ServiceRecordState (0x0002), ServiceID (0x0003), and
     * ProtocolDescriptorList (0x0004).If <code>attrSet</code> is
     * <code>null</code> then only the default attributes will be retrieved.
     * <code>attrSet</code> does not have to be sorted in increasing order,
     * but must only contain values in the range [0 - (2<sup>16</sup>-1)].
     * @see DiscoveryListener
     * @param attrSet indicates the attributes whose values will be
     * retrieved on services which have the UUIDs specified in <code>uuidSet</code>
     * @param uuidSet the set of UUIDs that are being searched for;  all
     * services returned will contain all the UUIDs specified here
     * @param btDev the remote Bluetooth device to search for services on
     * @param discListener the object that will receive events when services are discovered
     * @return the transaction ID of the service search; this number must be positive
     * @exception BluetoothStateException if the number of concurrent
     * service search transactions exceeds the limit specified by the
     * <code>bluetooth.sd.trans.max</code> property obtained from the
     * class <code>LocalDevice</code> or the system is unable to start one due to current conditions
     * @exception IllegalArgumentException if <code>attrSet</code> has an illegal service attribute ID or exceeds the property
     * <code>bluetooth.sd.attr.retrievable.max</code> defined in the class <code>LocalDevice</code>; if <code>attrSet</code>
     * or <code>uuidSet</code> is of length 0; if <code>attrSet</code> or <code>uuidSet</code> contains duplicates
     * @exception NullPointerException if <code>uuidSet</code>, <code>btDev</code>, or <code>discListener</code> is
     * <code>null</code>; if an element in  <code>uuidSet</code> array is <code>null</code>
     */
    public int searchServices(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev, DiscoveryListener discListener)
        throws BluetoothStateException {
            int transID = transactionID++;
            DataElement uuidListElement = new DataElement(DataElement.DATSEQ);
            for (int i = 0; i < uuidSet.length; i++) uuidListElement.addElement(new DataElement(DataElement.UUID, uuidSet[i]));
            DataElement attributeListElement = new DataElement(DataElement.DATSEQ);
            for (int i = 0; i < attrSet.length; i++)
                attributeListElement.addElement(new DataElement(DataElement.U_INT_2, attrSet[i]));
            SDPClientChannel sdpChannel = new SDPClientChannel(btDev, discListener);
            try {
                bluetoothStack.connectL2CAPChannel(sdpChannel, btDev, (short)0x0001);
                sdpChannel.send_SDP_ServiceSearchAttributeRequest((short)transID, (short)0xffff, uuidListElement,
                    attributeListElement);
            }
            catch (HCIException e) { throw new BluetoothStateException(e.toString()); }
            catch (IOException e) { throw new BluetoothStateException(e.toString()); }
            return transID;
    }

    /*  End of the method searchServices    */

    /**
     * Cancels the service search transaction that has the specified
     * transaction ID. The ID was assigned to the transaction by the method <code>searchServices()</code>. A
     * <code>serviceSearchCompleted()</code> event with a discovery type
     * of <code>SERVICE_SEARCH_TERMINATED</code> will occur when this method is called. After receiving this event, no further
     * <code>servicesDiscovered()</code> events will occur as a result of this search.
     * @param transID the ID of the service search transaction to cancel; returned by <code>searchServices()</code>
     * @return <code>true</code> if the service search transaction is
     * terminated, else <code>false</code>  if the <code>transID</code>
     * does not represent an active service search transaction Christian Lorenz: This method has no effect. ServiceSearches
     * will continue until they are completed. This method always returns true.
     */
    public boolean cancelServiceSearch(int transID) {
        //TODO cancelSearchServices
        return true;
    }

    /*  End of the method cancelServiceSearch   */

    /**
     * Attempts to locate a service that contains <code>uuid</code> in the ServiceClassIDList of its service record.  This
     * method will return a string that may be used in <code>Connector.open()</code> to establish a connection to the
     * service.  How the service is selected if there are multiple services with <code>uuid</code> and which devices to
     * search is implementation dependent.
     * @see ServiceRecord#NOAUTHENTICATE_NOENCRYPT
     * @see ServiceRecord#AUTHENTICATE_NOENCRYPT
     * @see ServiceRecord#AUTHENTICATE_ENCRYPT
     * @param uuid the UUID to search for in the ServiceClassIDList
     * @param security specifies the security requirements for a connection to this service; must be one of
     * <code>ServiceRecord.NOAUTHENTICATE_NOENCRYPT</code>, <code>ServiceRecord.AUTHENTICATE_NOENCRYPT</code>, or
     * <code>ServiceRecord.AUTHENTICATE_ENCRYPT</code>
     * @param master determines if this client must be the master of the
     * connection; <code>true</code> if the client must be the master;
     * <code>false</code> if the client can be the master or the slave
     * @return the connection string used to connect to the service
     * with a UUID of <code>uuid</code>; or <code>null</code> if no
     * service could be found with a UUID of <code>uuid</code> in the ServiceClassIDList
     * @exception BluetoothStateException if the Bluetooth system cannot
     * start the request due to the current state of the Bluetooth system
     * @exception NullPointerException if <code>uuid</code> is <code>null</code>
     * @exception IllegalArgumentException if <code>security</code> is
     * not <code>ServiceRecord.NOAUTHENTICATE_NOENCRYPT</code>, <code>ServiceRecord.AUTHENTICATE_NOENCRYPT</code>, or
     * <code>ServiceRecord.AUTHENTICATE_ENCRYPT</code> Christian Lorenz: This method is not implemented. Use
     * searchServices instead.
     */
    public String selectService(UUID uuid, int security, boolean master) throws BluetoothStateException {
        //TODO selectService
        throw new RuntimeException("Not Implemented! Used to compile Code");
    }

    /**
     * Christian Lorenz: This method is called from <code>BluetoothStack.receive_HCI_Event_Inquiry_Result(byte[])</code>.
     * @param eventPacket
     */
    public void receive_HCI_Event_Inquiry_Result(byte[] eventPacket) {
        for (int i = 0; i < eventPacket[3]; i++) {
            long bdAddr = (((long)eventPacket[i * 14 + 4]) & 0xff) | (((long)eventPacket[i * 14 + 5]) & 0xff) << 8 |
                (((long)eventPacket[i * 14 + 6]) & 0xff) << 16 | (((long)eventPacket[i * 14 + 7]) & 0xff) << 24 |
                (((long)eventPacket[i * 14 + 8]) & 0xff) << 32 | (((long)eventPacket[i * 14 + 9]) & 0xff) << 40;
            RemoteDevice remoteDevice = (RemoteDevice)remoteDevices.get(new Long(bdAddr));
            if (remoteDevice == null) remoteDevice = new RemoteDevice(bdAddr);
            remoteDevice.pageScanRepMode = eventPacket[i * 14 + 10];
            remoteDevice.pageScanPeriodMode = eventPacket[i * 14 + 11];
            remoteDevice.pageScanMode = eventPacket[i * 14 + 12];
            int classOfDevice = (int)(eventPacket[i * 14 + 13] & 0xff) | ((int)(eventPacket[i * 14 + 14] & 0xff) << 8) |
                ((int)(eventPacket[i * 14 + 15] & 0xff) << 16);
            if (remoteDevice.deviceClass == null) remoteDevice.deviceClass = new DeviceClass(classOfDevice);
            else remoteDevice.deviceClass.record = classOfDevice;
            remoteDevice.clockOffset = (short)((short)(eventPacket[i * 14 + 16] & 0xff) |
                ((short)(eventPacket[i * 14 + 17] & 0xff) << 8));
            remoteDevices.put(new Long(bdAddr), remoteDevice);
            foundRemoteDevices.addElement(remoteDevice);
            Enumeration discoveryListeners = listeners.elements();
            while (discoveryListeners.hasMoreElements()) {
                DiscoveryListener listener = (DiscoveryListener)discoveryListeners.nextElement();
                if (listener != null) listener.deviceDiscovered(remoteDevice, remoteDevice.deviceClass);
            }
        }
    }
    
    /**
     * Christian Lorenz: This method is called from <code>BluetoothStack.receive_HCI_Event_Inquiry_Result(byte[])</code>.
     * @param eventPacket
     */
    public void receive_HCI_Event_Inquiry_Result_With_RSSI(byte[] eventPacket) {
        for (int i = 0; i < eventPacket[3]; i++) {
            long bdAddr = (((long)eventPacket[i * 14 + 4]) & 0xff) | (((long)eventPacket[i * 14 + 5]) & 0xff) << 8 |
                (((long)eventPacket[i * 14 + 6]) & 0xff) << 16 | (((long)eventPacket[i * 14 + 7]) & 0xff) << 24 |
                (((long)eventPacket[i * 14 + 8]) & 0xff) << 32 | (((long)eventPacket[i * 14 + 9]) & 0xff) << 40;
            RemoteDevice remoteDevice = (RemoteDevice)remoteDevices.get(new Long(bdAddr));
            if (remoteDevice == null) remoteDevice = new RemoteDevice(bdAddr);
            remoteDevice.pageScanRepMode = eventPacket[i * 14 + 10];
            remoteDevice.pageScanPeriodMode = eventPacket[i * 14 + 11];
            int classOfDevice = (int)(eventPacket[i * 14 + 12] & 0xff) | ((int)(eventPacket[i * 14 + 13] & 0xff) << 8) |
                ((int)(eventPacket[i * 14 + 14] & 0xff) << 16);
            if (remoteDevice.deviceClass == null) remoteDevice.deviceClass = new DeviceClass(classOfDevice);
            else remoteDevice.deviceClass.record = classOfDevice;
            remoteDevice.clockOffset = (short)((short)(eventPacket[i * 14 + 15] & 0xff) |
                ((short)(eventPacket[i * 14 + 16] & 0xff) << 8));
            remoteDevice.rssi = eventPacket[i * 14 + 17];
            remoteDevices.put(new Long(bdAddr), remoteDevice);
            foundRemoteDevices.addElement(remoteDevice);
            Enumeration discoveryListeners = listeners.elements();
            while (discoveryListeners.hasMoreElements()) {
                DiscoveryListener listener = (DiscoveryListener)discoveryListeners.nextElement();
                if (listener != null) listener.deviceDiscovered(remoteDevice, remoteDevice.deviceClass);
            }
        }
    }

    /**
     * Christian Lorenz: This method is called from <code>BluetoothStack.receive_HCI_Event_Inquiry_Complete(byte[])</code>.
     * @param eventPacket
     */
    public void receive_HCI_Event_Inquiry_Complete(byte[] eventPacket) {
        if (eventPacket[3] == 0) {
            synchronized(cachedRemoteDevices) {
                cachedRemoteDevices.removeAllElements();
                cachedRemoteDevices = foundRemoteDevices;
            }
            Enumeration discoveryListeners = listeners.elements();
            isInquiring = false;
            while (discoveryListeners.hasMoreElements()) {
                DiscoveryListener listener = (DiscoveryListener)discoveryListeners.nextElement();
                listener.inquiryCompleted(DiscoveryListener.INQUIRY_COMPLETED);
            }
            listeners.removeAllElements();
        }
    }

    /**
     * Christian Lorenz: This method is called from <code>BluetoothStack.receive_HCI_Event_Inquiry_Complete(byte[])</code>
     * @param eventPacket
     */
    public void receive_HCI_Event_Remote_Name_Request_Complete(byte[] eventPacket) {
        long bdAddr = (((long)eventPacket[4]) & 0xff) | (((long)eventPacket[5]) & 0xff) << 8 |
            (((long)eventPacket[6]) & 0xff) << 16 | (((long)eventPacket[7]) & 0xff) << 24 |
            (((long)eventPacket[8]) & 0xff) << 32 | (((long)eventPacket[9]) & 0xff) << 40;
        RemoteDevice remoteDevice = (RemoteDevice)remoteDevices.get(new Long(bdAddr));
        if (remoteDevice != null) {
            if (eventPacket[3] == 0) {
                int i = 0;
                while (i < 248 && eventPacket[i + 10] != 0x00) { i++; }
                remoteDevice.friendlyName = new String(eventPacket, 10, i);
            }
            else remoteDevice.friendlyName = "Bluetooth Device (" + remoteDevice.getBluetoothAddress() + ")";
        }
    }

    /**
     * Christian Lorenz: Resolves a Bluetooth Address to a RemoteDevice. Add this for use with Connector.
     * @param remoteAddress Bluetooth Device Address as a long.
     * @return the RemoteDevice matching the Bluetooth Address if it has been Discovered. If the Device hasn't been
     * discovered this returns null.
     */
    public RemoteDevice getRemoteDevice(long remoteAddress)
        { return (RemoteDevice)remoteDevices.get(new Long(remoteAddress)); }
}

/*  End of the definition of class DiscoveryAgent */

