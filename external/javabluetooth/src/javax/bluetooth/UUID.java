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

/** 
 * The <code>UUID</code> class defines universally unique identifiers. These 128-bit unsigned integers are guaranteed
 * to be unique across all time and space. Accordingly, an instance of this class is immutable.
 * The Bluetooth specification provides an algorithm describing how a
 * 16-bit or 32-bit UUID could be promoted to a 128-bit UUID. Accordingly, this class provides an interface that assists
 * applications in creating 16-bit, 32-bit, and 128-bit long UUIDs. The
 * methods supported by this class allow equality testing of two UUID objects. <p> The Bluetooth Assigned Numbers document (<A
 * HREF="http://www.bluetooth.org/assigned-numbers/sdp.htm"> http://www.bluetooth.org/assigned-numbers/sdp.htm</A>)
 * defines a large number of UUIDs for protocols and service classes.
 * The table below provides a short list of the most common UUIDs defined in the Bluetooth Assigned Numbers document. <TABLE>
 * <TR><TH>Name</TH><TH>Value</TH><TH>Size</TH></TR> <TR><TD>Base UUID Value (Used in promoting 16-bit and 32-bit UUIDs to
 * 128-bit UUIDs)</TD><TD>0x0000000000001000800000805F9B34FB</TD> <TD>128-bit</TD></TR>
 * <TR><TD>SDP</TD><TD>0x0001</TD><TD>16-bit</TD></TR> <TR><TD>RFCOMM</TD><TD>0x0003</TD><TD>16-bit</TD></TR>
 * <TR><TD>OBEX</TD><TD>0x0008</TD><TD>16-bit</TD></TR> <TR><TD>HTTP</TD><TD>0x000C</TD><TD>16-bit</TD></TR>
 * <TR><TD>L2CAP</TD><TD>0x0100</TD><TD>16-bit</TD></TR> <TR><TD>BNEP</TD><TD>0x000F</TD><TD>16-bit</TD></TR>
 * <TR><TD>Serial Port</TD><TD>0x1101</TD><TD>16-bit</TD></TR> <TR><TD>ServiceDiscoveryServerServiceClassID</TD><TD>0x1000</TD>
 * <TD>16-bit</TD></TR> <TR><TD>BrowseGroupDescriptorServiceClassID</TD><TD>0x1001</TD> <TD>16-bit</TD></TR>
 * <TR><TD>PublicBrowseGroup</TD><TD>0x1002</TD><TD>16-bit</TD></TR> <TR><TD>OBEX Object Push
 * Profile</TD><TD>0x1105</TD><TD>16-bit</TD></TR> <TR><TD>OBEX File Transfer Profile</TD><TD>0x1106</TD><TD>16-bit</TD></TR>
 * <TR><TD>Personal Area Networking User</TD><TD>0x1115</TD> <TD>16-bit</TD></TR>
 * <TR><TD>Network Access Point</TD><TD>0x1116</TD><TD>16-bit</TD></TR>
 * <TR><TD>Group Network</TD><TD>0x1117</TD><TD>16-bit</TD></TR> </TABLE>
 * @author Christian Lorenz
 */
public class UUID {
    private String uuidString;
    private byte[] uuidBytes;
    private long uuidLong;

    /**
     * Christian Lorenz: Added this for use with DataElement implementation.
     * @param uuidBytes
     */
    public UUID(byte[] uuidBytes) {
        this.uuidBytes = uuidBytes;
        if (uuidBytes.length == 4) {
            this.uuidLong = (long)(((long)uuidBytes[0] & 0xff) << 24 | ((long)uuidBytes[1] & 0xff) << 16 |
                ((long)uuidBytes[2] & 0xff) << 8 | ((long)uuidBytes[3] & 0xff));
        }
        else if (uuidBytes.length == 2) {
            this.uuidLong = (long)(((long)uuidBytes[0] & 0xff) << 8 | ((long)uuidBytes[1] & 0xff));
        }
        else throw new IllegalArgumentException("uuidValue is not in range.");
    }

    /**
     * Creates a <code>UUID</code> object from <code>long</code> value <code>uuidValue</code>. A UUID
     * is defined as an unsigned integer whose value can range from
     * [0 to 2<sup>128</sup>-1]. However, this constructor allows only
     * those values that are in the range of [0 to 2<sup>32</sup> -1].
     * Negative values and values in the range of [2<sup>32</sup>, 2<sup>63</sup> -1] are not
     * allowed and will cause an <code>IllegalArgumentException</code> to be thrown.
     * @param uuidValue the 16-bit or 32-bit value of the UUID
     * @exception IllegalArgumentException if <code>uuidValue</code> is not in the range [0, 2<sup>32</sup> -1]
     */
    public UUID(long uuidValue) {
        uuidLong = uuidValue;
        uuidBytes = new byte[4];
        uuidBytes[0] = (byte)((uuidValue >> 24) & 0xff);
        uuidBytes[1] = (byte)((uuidValue >> 16) & 0xff);
        uuidBytes[2] = (byte)((uuidValue >> 8) & 0xff);
        uuidBytes[3] = (byte)(uuidValue & 0xff);
        //TODO throw new IllegalArgumentException("uuidValue is not in range.");
    }

    /*  End of the constructor method   */

    /**
     * Creates a <code>UUID</code> object from the string provided.  The
     * characters in the string must be from the hexadecimal set [0-9,
     * a-f, A-F].  It is important to note that the prefix "0x" generally
     * used for hex representation of numbers is not allowed. If the
     * string does not have characters from the hexadecimal set, an
     * exception will be thrown. The string length has to be positive
     * and less than or equal to 32. A string length that exceeds 32 is
     * illegal and will cause an exception. Finally, a <code>null</code> input
     * is also considered illegal and causes an exception. <P>
     * If <code>shortUUID</code> is <code>true</code>, <code>uuidValue</code>
     * represents a 16-bit or 32-bit UUID.  If <code>uuidValue</code> is in
     * the range 0x0000 to 0xFFFF then this constructor will create a 16-bit UUID.  If <code>uuidValue</code> is in the range
     * 0x000010000 to 0xFFFFFFFF, then this constructor will create
     * a 32-bit UUID.  Therefore, <code>uuidValue</code> may only be 8 characters long. <P>
     * On the other hand, if <code>shortUUID</code> is <code>false</code>, then
     * <code>uuidValue</code> represents a 128-bit UUID.  Therefore, <code>uuidValue</code> may only be 32 character long
     * @param uuidValue the string representation of a 16-bit, 32-bit or 128-bit UUID
     * @param shortUUID indicates the size of the UUID to be constructed; <code>true</code> is used to indicate short UUIDs,
     * i.e. either 16-bit or 32-bit; <code>false</code> indicates an 128-bit UUID
     * @exception NumberFormatException if <code>uuidValue</code>
     * has characters that are not defined in the hexadecimal set [0-9, a-f, A-F]
     * @exception IllegalArgumentException if <code>uuidValue</code>
     * length is zero; if <code>shortUUID</code> is <code>true</code>
     * and <code>uuidValue</code>'s length is  greater than 8; if <code>shortUUID</code> is <code>false</code> and
     * <code>uuidValue</code>'s length is greater than 32
     * @exception NullPointerException if <code>uuidValue</code> is <code>null</code>
     */
    public UUID(String uuidValue, boolean shortUUID) {
        if (uuidValue == null) throw new NullPointerException("uuidValue is null.");
        if (shortUUID) {
            uuidLong = Long.decode("0x" + uuidValue).longValue(); //also throws a number format exception
            uuidBytes = new byte[4];
            uuidBytes[0] = (byte)((uuidLong >> 24) & 0xff);
            uuidBytes[1] = (byte)((uuidLong >> 16) & 0xff);
            uuidBytes[2] = (byte)((uuidLong >> 8) & 0xff);
            uuidBytes[3] = (byte)(uuidLong & 0xff);
        }
        else { //TODO properly convert shortUUIDs to long UUIDs
            throw new IllegalArgumentException("This Implemention does not yet support 128-bit UUID's.");
        }
    }

    /*  End of the constructor method   */

    /**
     * Returns the string representation of the 128-bit UUID object. The string being returned represents a UUID
     * that contains characters from the hexadecimal set, [0-9, A-F]. It does not include the prefix "0x" that is generally
     * used for hex representation of numbers. The return value will never be <code>null</code>.
     * @return the string representation of the UUID
     */
    public String toString() {
        if (uuidString == null) {
            uuidString = "";
            for (int i = 0; i < uuidBytes.length; i++)
                uuidString += Integer.toString((uuidBytes[i] & 0xff) + 0x100, 16).substring(1);
            uuidString = uuidString.toUpperCase();
        }
        return uuidString;
    }

    /*  End of the method toString  */

    /** Christian Lorenz: Added this for use with DataElement implementation. */
    public byte[] toByteArray() { //TODO handle uuids constructed from strings
        return uuidBytes;
    }

    /** Christian Lorenz: Added this for use with equals(). */
    public long toLong() { //TODO handle uuids constructed from strings
        return uuidLong;
    }

    /**
     * Determines if two <code>UUID</code>s are equal.  They are equal
     * if their 128 bit values are the same. This method will return <code>false</code> if <code>value</code> is
     * <code>null</code> or is not a <code>UUID</code> object.
     * @param value the object to compare to
     * @return <code>true</code> if the 128 bit values of the two objects are equal, otherwise <code>false</code>
     */
    public boolean equals(Object value) { return (uuidLong == ((UUID)value).toLong()); }

    /* End of the method equals */

    /**
     * Computes the hash code for this object. This method retains the same semantic contract as defined in
     * the class <code>java.lang.Object</code> while overriding the implementation.
     * @return the hash code for this object
     */
    public int hashCode() { return (int)uuidLong; }

    /*  End of the method hashCode  */
}

/*  End of the class UUID definition. */

