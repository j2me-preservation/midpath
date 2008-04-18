/*
 *
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.io.j2me.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;

import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.j2me.ProtocolBase;
import com.sun.midp.security.Permissions;

/**
 * SMS message connection implementation.
 *
 * <code>Protocol</code> itself is not instantiated. Instead, the application
 * calls <code>Connector.open</code> with an SMS URL string and obtains a
 * {@link javax.wireless.messaging.MessageConnection MessageConnection}
 *  object. It is an instance of <code>MessageConnection</code>
 * that is instantiated. The Generic Connection Framework mechanism
 * in CLDC will return a <code>Protocol</code> object, which is the
 * implementation of <code>MessageConnection</code>. The
 * <code>Protocol</code> object represents a connection to a low-level transport
 * mechanism.
 * <p>
 * Optional packages, such as <code>Protocol</code>, cannot reside in
 * small devices.
 * The Generic Connection Framework allows an application to reach the
 * optional packages and classes indirectly. For example, an application
 * can be written with a string that is used to open a connection. Inside
 * the implementation of <code>Connector</code>, the string is mapped to a
 * particular implementation: <code>Protocol</code>, in this case. This allows
 * the implementation to be optional even though
 * the interface, <code>MessageConnection</code>, is required.
 * <p>
 * Closing the connection frees an instance of <code>MessageConnection</code>.
 * <p>
 * The <code>Protocol</code> class contains methods
 * to open and close the connection to the low-level transport mechanism. The
 * messages passed on the transport mechanism are defined by the
 * {@link MessageObject MessageObject}
 * class.
 * Connections can be made in either client mode or server mode.
 * <ul>
 * <li>Client mode connections are for sending messages only. They are
 * created by passing a string identifying a destination address to the
 * <code>Connector.open()</code> method.</li>
 * <li>Server mode connections are for receiving and sending messages. They
 * are created by passing a string that identifies a port, or equivalent,
 * on the local host to the <code>Connector.open()</code> method.</li>
 * </ul>
 * The class also contains methods to send, receive, and construct
 * <code>Message</code> objects.
 * <p>
 * <p>
 * This class declares that it implements <code>StreamConnection</code>
 * so it can intercept calls to <code>Connector.open*Stream()</code>
 * to throw an <code>IllegalArgumentException</code>.
 * </p>
 *
 */
public class Protocol extends ProtocolBase {

	/** Connection mode. */
	private final int connectionMode = 0;

	/** Currently opened connections. */
	static protected Vector openconnections = new Vector();

	/** Name of current connection. */
	protected String url;

	/** Local handle for port number. */
	private int m_iport = 0;

	/** Count of simultaneous opened conenctions. */
	protected static int open_count = 0;

	/** Ports barred from use int the specification. */
	int restrictedPorts[] = { 2805, // WAP WTA secure connection-less session service
			2923, // WAP WTA secure session service
			2948, // WAP Push connectionless session service (client side)
			2949, // WAP Push secure connectionless session service (client side)
			5502, // Service Card reader
			5503, // Internet access configuration reader
			5508, // Dynamic Menu Control Protocol
			5511, // Message Access Protocol
			5512, // Simple Email Notification
			9200, // WAP connectionless session service
			9201, // WAP session service
			9202, // WAP secure connectionless session service
			9203, // WAP secure session service
			9207, // WAP vCal Secure
			49996, // SyncML OTA configuration
			49999 // WAP OTA configuration
	};

	/**	 DCS: GSM Alphabet  */
	protected static final int GSM_TEXT = 0;

	/**	 DCS: Binary */
	protected static final int GSM_BINARY = 1;

	/**	 DCS: Unicode UCS-2 */
	protected static final int GSM_UCS2 = 2;
	
	private SMSBackend backend = new NullSMSBackend();

	/** Creates a message connection protocol handler. */
	public Protocol() {
		super();
		ADDRESS_PREFIX = "sms://";
	}

	/**
	 * Gets the connection parameter in string mode.
	 * @return string that contains a parameter 
	 */
	protected String getAppID() {
		if (m_iport > 0) {
			return new String(Integer.toString(m_iport));
		} else {
			return null;
		}
	}

	/**
	 * Sets the connection parameter in string mode.
	 * @param newValue new value of connection parameter 
	 */
	protected void setAppID(String newValue) {
		try {
			m_iport = Integer.parseInt(newValue);
		} catch (NumberFormatException exc) {
			m_iport = 0;
		}
	}

	/**
	 * Private class to encapsulate SMSPacket data structure
	 *
	 */
class SMSPacket {
		/** Message buffer */
		public byte[] message;
		/** Buffer with sender's address */
		public byte[] address;
		/** port number */
		public int port;
		/** sent at */
		public long sentAt;
		/** Type of message */
		public int messageType;
	};

	/*
	 * MessageConnection Interface
	 */

	/**
	 * Constructs a new message object of a text or binary type.
	 * If the <code>TEXT_MESSAGE</code> constant is passed in, the
	 * <code>TextMessage</code> interface is implemented by the created object.
	 * If the <code>BINARY_MESSAGE</code> constant is passed in, the
	 * <code>BinaryMessage</code> interface is implemented by the created
	 * object.
	 * <p>
	 * If this method is called in a sending mode, a new <code>Message</code>
	 * object is requested from the connection. For example:
	 * <p>
	 * <code>Message msg = conn.newMessage(TEXT_MESSAGE);</code>
	 * <p>
	 * The <code>Message</code> object that was created doesn't have the
	 * destination address set. It's the application's responsibility to set it
	 * before the message is sent.
	 * <p>
	 * If this method is called in receiving mode, the
	 * <code>Message</code> object does have
	 * its address set. The application can act on the object to extract
	 * the address and message data.
	 * <p>
	 * <!-- The <code>type</code> parameter indicates the number of bytes
	 * that should be
	 * allocated for the message. No restrictions are placed on the application
	 * for the value of <code>size</code>.
	 * A value of <code>null</code> is permitted and creates a
	 * <code>Message</code> object
	 * with a 0-length message. -->
	 *
	 * @param type <code>TEXT_MESSAGE</code> or
	 *     <code>BINARY_MESSAGE</code>.
	 * @return a new message.
	 */
	public Message newMessage(String type) {
		String address = null;

		/*
		 * Provide the default address from the original open.
		 */

		if (((m_mode & Connector.WRITE) > 0) && (host != null)) {
			address = ADDRESS_PREFIX + host;
			if (m_iport != 0) {
				address = address + ":" + String.valueOf(m_iport);
			}
		}

		return newMessage(type, address);
	}

	/**
	 * Constructs a new message object of a text or binary type and specifies
	 * a destination address.
	 * If the <code>TEXT_MESSAGE</code> constant is passed in, the
	 * <code>TextMessage</code> interface is implemented by the created object.
	 * If the <code>BINARY_MESSAGE</code> constant is passed in, the
	 * <code>BinaryMessage</code> interface is implemented by the created
	 * object.
	 * <p>
	 * The destination address <code>addr</code> has the following format:
	 * </p>
	 * <p>
	 * <code>sms://</code><em>phone_number</em>:<em>port</em>
	 * </p>
	 *
	 * @param type <code>TEXT_MESSAGE</code> or
	 *     <code>BINARY_MESSAGE</code>.
	 * @param addr the destination address of the message.
	 * @return a new <code>Message</code> object.
	 */
	public Message newMessage(String type, String addr) {
		Message message = null;

		if (type.equals(MessageConnection.TEXT_MESSAGE)) {

			message = new TextObject(addr);
		} else {
			if (type.equals(MessageConnection.BINARY_MESSAGE)) {

				message = new BinaryObject(addr);
			} else {
				throw new IllegalArgumentException("Message type not supported");
			}
		}

		return message;
	}

	/**
	 * Receives the bytes that have been sent over the connection, constructs a
	 * <code>Message</code> object, and returns it.
	 * <p>
	 * If there are no <code>Message</code>s waiting on the connection, this
	 * method will block until a message is received, or the
	 * <code>MessageConnection</code> is closed.
	 *
	 * @return a <code>Message</code> object.
	 * @exception java.io.IOException if an error occurs while receiving a
	 *     message.
	 * @exception java.io.InterruptedIOException if this
	 *     <code>MessageConnection</code> object is closed during this receive
	 *     method call.
	 * @exception java.lang.SecurityException if the application does not have
	 *      permission to receive messages using the given port number.
	 */
	public synchronized Message receive() throws IOException {

		/* Check if we have permission to recieve. */
		checkReceivePermission();

		/* Make sure the connection is still open. */
		ensureOpen();

		if (((m_mode & Connector.READ) == 0) || (host != null)) {

			throw new IOException("Invalid connection mode");
		}

		Message message = null;
		int length = 0;
		try {

			SMSPacket smsPacket = new SMSPacket();

			/*
			 * Packet has been received and deleted from inbox.
			 * Time to wake up receive thread.
			 */
			// Pick up the SMS message from the message pool.
			length = backend.receive(m_iport, midletSuite.getUniqueID(), connHandle, smsPacket);

			if (length >= 0) {
				String type;
				boolean isTextMessage = true;
				if (smsPacket.messageType == GSM_BINARY) {
					type = MessageConnection.BINARY_MESSAGE;
					isTextMessage = false;
				} else {
					type = MessageConnection.TEXT_MESSAGE;
					isTextMessage = true;
				}
				message = newMessage(type, new String(ADDRESS_PREFIX + new String(smsPacket.address) + ":"
						+ smsPacket.port));
				String messg = null;
				if (isTextMessage) {
					if (length > 0) {
						if (smsPacket.messageType == GSM_TEXT) {
							messg = new String(TextEncoder.toString(TextEncoder.decode(smsPacket.message)));
						} else {
							messg = new String(TextEncoder.toString(smsPacket.message));

						}
					} else {
						messg = new String("");
					}
					((TextObject) message).setPayloadText(messg);
				} else {
					if (length > 0) {
						((BinaryObject) message).setPayloadData(smsPacket.message);
					} else {
						((BinaryObject) message).setPayloadData(new byte[0]);
					}
				}
				((MessageObject) message).setTimeStamp(smsPacket.sentAt);
			}
		} catch (InterruptedIOException ex) {
			length = 0;
			throw new InterruptedIOException("MessageConnection is closed");
		} catch (IOException ex) {
			io2InterruptedIOExc(ex, "receiving");
		} finally {
			if (length < 0) {
				throw new InterruptedIOException("Connection closed error");
			}
		}

		return message;
	}

	/**
	 * Sends a message over the connection. This method extracts the data
	 * payload from the <code>Message</code> object so that it can be sent as a
	 * datagram.
	 *
	 * @param     dmsg a <code>Message</code> object
	 * @exception java.io.IOException if the message could not be sent or
	 *     because of network failure
	 * @exception java.lang.IllegalArgumentException if the message is
	 *     incomplete or contains invalid information. This exception is also
	 *     thrown if the payload of the message exceeds the maximum length for
	 *     the given messaging protocol.
	 * @exception java.io.InterruptedIOException if a timeout occurs while
	 *     either trying to send the message or if this <code>Connection</code>
	 *     object is closed during this <code>send</code> operation.
	 * @exception java.lang.NullPointerException if the parameter is
	 *     <code>null</code>.
	 * @exception java.lang.SecurityException if the application does not have
	 *      permission to send the message.
	 */
	public void send(Message dmsg) throws IOException {
		String phoneNumber = null;
		String address = null;

		if (dmsg == null) {
			throw new NullPointerException();
		}

		if (dmsg.getAddress() == null) {
			throw new IllegalArgumentException();
		}

		/*
		 * parse name into host and port
		 */
		String addr = dmsg.getAddress();
		HttpUrl url = new HttpUrl(addr);
		if (url.port == -1) {
			/* no port supplied */
			url.port = 0;
		}

		/* Can not send to cbs address. */
		if (addr.startsWith("cbs:")) {
			// Can't send a CBS message.
			throw new IllegalArgumentException("Can't send CBS msg.");
		}

		int numSeg = numberOfSegments(dmsg);
		if ((numSeg <= 0) || (numSeg > 3)) {
			throw new IOException("Error: message is too large");
		}

		try {
			midletSuite.checkForPermission(Permissions.SMS_SEND, url.host, Integer.toString(numSeg));
		} catch (InterruptedException ie) {
			throw new InterruptedIOException("Interrupted while trying " + "to ask the user permission");
		}

		ensureOpen();

		if ((m_mode & Connector.WRITE) == 0) {

			throw new IOException("Invalid mode");
		}

		int sourcePort = 0;
		if ((m_mode & Connector.READ) != 0 && host == null) {
			sourcePort = m_iport;
		}

		for (int restrictedPortIndex = 0; restrictedPortIndex < restrictedPorts.length; restrictedPortIndex++) {
			if (url.port == restrictedPorts[restrictedPortIndex]) {
				throw new SecurityException("not allowed to send SMS messages to the restricted ports");
			}
		}

		int messageType = GSM_BINARY;
		byte[] msgBuffer = null;

		if (dmsg instanceof TextObject) {
			byte[] gsm7bytes;
			msgBuffer = ((TextObject) dmsg).getBytes();
			if (msgBuffer != null) {
				/*
				 * Attempt to encode the UCS2 bytes as GSM 7-bit.
				 */
				gsm7bytes = TextEncoder.encode(msgBuffer);
				if (gsm7bytes != null) {
					msgBuffer = gsm7bytes;
					messageType = GSM_TEXT;
				} else {
					/*
					 * Encoding attempt failed. Send UCS2 bytes.
					 */
					messageType = GSM_UCS2;
				}
			}

		} else if (dmsg instanceof BinaryObject) {
			msgBuffer = ((BinaryObject) dmsg).getPayloadData();
		} else {
			throw new IllegalArgumentException("Message type not supported");
		}

		try {
			backend.send(connHandle, messageType, url.host, url.port, sourcePort, msgBuffer);
		} catch (IOException ex) {
			io2InterruptedIOExc(ex, "sending");
		}
	}

	/**
	 * Returns how many segments in the underlying protocol would
	 * be needed for sending the <code>Message</code> given as the parameter.
	 *
	 * <p>Note that this method does not actually send the message;
	 * it will only calculate the number of protocol segments
	 * needed for sending it.
	 * </p>
	 * <p>This method calculates the number of segments required
	 * when this message is split into the protocol segments
	 * utilizing the underlying protocol's features.
	 * Possible implementation's limitations that may limit the number of
	 * segments that can be sent using it are not taken into account. These
	 * limitations are protocol specific. They are documented
	 * with that protocol's adapter definition.
	 * </p>
	 * @param msg the message to be used for the calculation
	 * @return number of protocol segments required to send the message.
	 *     If the <code>Message</code> object can't be sent using
	 *     the underlying protocol, <code>0</code> is returned.
	 */
	public int numberOfSegments(Message msg) {

		/** The number of segments required to send the message. */
		int segments = 0;

		/* Generate the proper buffer contents and message type. */
		byte[] msgBuffer = null;
		int messageType = GSM_TEXT;
		if (msg instanceof TextObject) {
			msgBuffer = ((TextObject) msg).getBytes();
			if (msgBuffer != null) {
				/*
				 * Attempt to encode the UCS2 bytes as GSM 7-bit.
				 */
				byte[] gsm7bytes = TextEncoder.encode(msgBuffer);
				if (gsm7bytes != null) {
					msgBuffer = gsm7bytes;
				} else {
					/*
					 * Encoding attempt failed. Use UCS2 bytes.
					 */
					messageType = GSM_UCS2;
				}
			}
		} else if (msg instanceof BinaryObject) {
			msgBuffer = ((BinaryObject) msg).getPayloadData();
			messageType = GSM_BINARY;
		} else {
			throw new IllegalArgumentException("Message type not supported.");
		}

		// Pick up the message length.
		if (msgBuffer != null) {

			// Parse address to see if there's a port value.
			boolean hasPort = false;
			String addr = msg.getAddress();
			if (addr != null) {
				// workaround. HttpUrl can throw IAE on zero port.
				try {
					HttpUrl url = new HttpUrl(addr);
					if (url.port != -1) {
						/* No port supplied. */
						hasPort = true;
					}
				} catch (IllegalArgumentException iae) {
					hasPort = false;
				}

				if (addr.startsWith("cbs:")) {
					// Can't send a CBS message.
					return 0;
				}
			}
			// Other protocols can receive the message.
			segments = numberOfSegments(msgBuffer, msgBuffer.length, messageType, hasPort);
		}

		return segments;
	}

	/**
	 * Closes the connection. Resets the connection <code>open</code> flag
	 * to <code>false</code>. Subsequent operations on a
	 * closed connection should throw an appropriate exception.
	 *
	 *
	 * @exception IOException  if an I/O error occurs
	 */
	public void close() throws IOException {
		/*
		 * Set m_iport to 0, in order to quit out of the while loop
		 * in the receiver thread.
		 */
		int save_iport = m_iport;

		m_iport = 0;

		synchronized (closeLock) {
			if (open) {
				/*
				 * Reset open flag early to prevent receive0 executed by
				 * concurrent thread to operate on partially closed
				 * connection
				 */
				open = false;

				backend.close(save_iport, connHandle, 1);

				setMessageListener(null);

				/*
				 * Reset handle and other params to default
				 * values. Multiple calls to close() are allowed
				 * by the spec and the resetting would prevent any
				 * strange behaviour.
				 */
				connHandle = 0;
				host = null;
				m_mode = 0;

				/*
				 * Remove this connection from the list of open
				 * connections.
				 */
				int len = openconnections.size();
				for (int i = 0; i < len; i++) {
					if (openconnections.elementAt(i) == this) {
						openconnections.removeElementAt(i);
						break;
					}
				}

				open_count--;
			}
		}
	}

	/*
	 * ConnectionBaseInterface Interface
	 */

	/**
	 * Opens a connection. This method is called from the
	 * <code>Connector.open()</code> method to obtain the destination
	 * address given in the <code>name</code> parameter.
	 * <p>
	 * The format for the <code>name</code> string for this method is:
	 * </p>
	 * <p>
	 * <code>sms://<em>[phone_number</em>:<em>][port_number]</em></code>
	 * </p>
	 * <p>
	 * where the <em>phone_number:</em> is optional.
	 * If the <em>phone_number</em>
	 * parameter is present, the connection is being opened in
	 * client mode. This means that messages can be sent.
	 * If the parameter is absent, the connection is being opened in
	 * server mode. This means that messages can be sent and received.
	 * <p>
	 * The connection that is opened is to a low-level transport mechanism
	 * which can be any of the following:
	 * <ul>
	 * <li>a datagram Short Message Peer-to-Peer (SMPP)
	 * to a service center </li>
	 * <li>a <code>comm</code> connection to a phone device with
	 *   AT-commands</li>
	 * <li>a native SMS stack</li>
	 *  </ul>
	 * Currently, the <code>mode</code> and <code>timeouts</code> parameters are
	 * ignored.
	 *
	 * @param name the target of the connection
	 * @param mode indicates whether the caller
	 *             intends to write to the connection. Currently,
	 *             this parameter is ignored.
	 * @param timeouts indicates whether the caller
	 *                 wants timeout exceptions. Currently,
	 *             this parameter is ignored.
	 * @return this connection
	 * @exception IOException if the connection is closed or unavailable
	 */
	public Connection openPrim(String name, int mode, boolean timeouts) throws IOException {

		return openPrimInternal(name, mode, timeouts);
	}

	/*
	 * StreamConnection Interface
	 */

	/**
	 * Open and return an input stream for a connection.
	 * This method always throw
	 * <code>IllegalArgumentException</code>.
	 *
	 * @return                 An input stream
	 * @exception IOException  If an I/O error occurs
	 * @exception IllegalArgumentException  is thrown for all requests
	 */
	public InputStream openInputStream() throws IOException {

		throw new IllegalArgumentException("Not supported");
	}

	/**
	 * Open and return a data input stream for a connection.
	 * This method always throw
	 * <code>IllegalArgumentException</code>.
	 *
	 * @return                 An input stream
	 * @exception IOException  If an I/O error occurs
	 * @exception IllegalArgumentException  is thrown for all requests
	 */
	public DataInputStream openDataInputStream() throws IOException {

		throw new IllegalArgumentException("Not supported");
	}

	/**
	 * Open and return an output stream for a connection.
	 * This method always throw
	 * <code>IllegalArgumentException</code>.
	 *
	 * @return                 An output stream
	 * @exception IOException  If an I/O error occurs
	 * @exception IllegalArgumentException  is thrown for all requests
	 */
	public OutputStream openOutputStream() throws IOException {

		throw new IllegalArgumentException("Not supported");
	}

	/**
	 * Open and return a data output stream for a connection.
	 * This method always throw
	 * <code>IllegalArgumentException</code>.
	 *
	 * @return                 an output stream
	 * @exception IOException  if an I/O error occurs
	 * @exception IllegalArgumentException  is thrown for all requests
	 */
	public DataOutputStream openDataOutputStream() throws IOException {

		throw new IllegalArgumentException("Not supported");
	}

	/*
	 * Protocol members
	 */

	/**
	 * Opens a connection. This is the internal entry point that
	 * allows the CBS protocol handler to use the reserved port for
	 * CBS emulated messages.
	 *
	 * @param name the target of the connection
	 * @param mode indicates whether the caller
	 *             intends to write to the connection. Currently,
	 *             this parameter is ignored.
	 * @param timeouts indicates whether the caller
	 *                 wants timeout exceptions. Currently,
	 *             this parameter is ignored.
	 * @return this connection
	 * @exception IOException if the connection is closed or unavailable
	 */
	public synchronized Connection openPrimInternal(String name, int mode, boolean timeouts) throws IOException {

		/*
		 * If <code>host == null</code>, then we are a server endpoint at
		 * the supplied <code>port</code>.
		 *
		 * If <code>host != null</code> we are a client endpoint at a port
		 * decided by the system and the default address for
		 * SMS messages to be sent is <code>sms://host:port</code>.
		 */

		String portName = null;

		if ((name == null) || (name.length() <= 2) || (name.charAt(0) != '/') || (name.charAt(1) != '/')) {
			throw new IllegalArgumentException("Missing protocol separator");
		}

		int colon = name.indexOf(':');
		if (colon > 0) {
			if (colon != 2) {
				host = name.substring(2, colon);
			}
			portName = name.substring(colon + 1);
		} else {
			if (name.length() > 2) {
				host = name.substring(2);
			}
		}

		if (host != null) {
			int offset = 0;
			int len = host.length();
			char c = '\0';
			/* Only '+' followed by 0-9 are allowed in the host field. */
			if (len > 0) {
				c = host.charAt(0);
				if (c == '+') {
					offset = 1;
				}
				for (int i = offset; i < host.length(); i++) {
					c = host.charAt(i);
					if ('0' <= c && c <= '9') {

						continue;
					} else {
						throw new IllegalArgumentException("Host format");
					}
				}
			}
		}

		int portNumber = 0;
		m_iport = 0;
		if (portName != null) {
			int len = portName.length();
			if (len == 0) {
				throw new IllegalArgumentException("Port length");
			}
			/*
			 * Add a numeric check hat the port is less than the
			 * GSM maximum port number.
			 */
			try {
				portNumber = Integer.parseInt(portName);
				m_iport = portNumber;
				if ((portNumber > 65535) || (portNumber < 0)) {
					throw new IllegalArgumentException("Port range");
				}
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Port Number" + " formatted badly.");
			}

		}

		if (mode == Connector.READ && host != null && host.length() > 0) {
			throw new IllegalArgumentException("Cannot read on " + "client connection");
		}

		if ((mode == Connector.WRITE) && (host == null)) {
			/*
			 * avoid throwing the following exception for compliance
			 * throw new IllegalArgumentException("Missing host name");
			 */
		}

		if ((mode != Connector.READ) && (mode != Connector.WRITE) && (mode != Connector.READ_WRITE)) {

			throw new IllegalArgumentException("Invalid mode");
		}

		/*
		 * Check to see if the application has the permision to
		 * use this connection type.
		 */
		if (!openPermission) {
			try {
				midletSuite.checkForPermission(Permissions.SMS_SERVER, "sms:open");
				openPermission = true;
			} catch (InterruptedException ie) {
				throw new InterruptedIOException("Interrupted while trying " + "to ask the user permission");
			}
		}
		/*
		 * Check to see if this connection is already opened.
		 */
		int len = openconnections.size();
		for (int i = 0; i < len; i++) {
			if (!(openconnections.elementAt(i) instanceof com.sun.midp.io.j2me.sms.Protocol)
					|| ((Protocol) openconnections.elementAt(i)).url.equals(name)) {
				throw new IOException("Already opened");
			}
		}

		openconnections.addElement(this);
		url = name;

		try {
			connHandle = backend.open(host, midletSuite.getUniqueID(), m_iport);
		} catch (IOException ioexcep) {
			m_mode = 0;
			throw new IOException("SMS connection cannot be opened");
		} catch (OutOfMemoryError oomexcep) {
			m_mode = 0;
			throw new IOException("SMS connection cannot be opened");
		}

		open_count++;
		m_mode = mode;
		open = true;

		return this;
	}

	/**
	 * Checks internal setting of receive permission.
	 * Called from receive and setMessageListener methods.
	 * @exception InterruptedIOException if permission dialog
	 * was preempted
	 */
	protected void checkReceivePermission() throws InterruptedIOException {
		/* Check if we have permission to recieve. */
		if (!readPermission) {
			try {
				midletSuite.checkForPermission(Permissions.SMS_RECEIVE, "sms:receive");
				readPermission = true;
			} catch (InterruptedException ie) {
				throw new InterruptedIOException("Interrupted while trying " + "to ask the user permission");
			}
		}
	}

	/**
	 * Unblock the receive thread.
	 *
	 * @param msid The MIDlet suite ID.
	 *
	 * @return  returns handle to the connection.
	 */
	protected int unblockReceiveThread(int msid) throws IOException {
		return backend.unblockReceiveThread();
	}

	/**
	 * Close connection.
	 *
	 * @param connHandle handle returned by open0
	 * @param deRegister Deregistration appID when parameter is 1.
	 * @return    0 on success, -1 on failure
	 */
	protected int closeConnection(int connHandle, int deRegister) {
		return backend.close(m_iport, connHandle, deRegister);
	}

	/**
	 * Waits until message available
	 *
	 * @param handle handle to connection
	 * @return 0 on success, -1 on failure
	 * @exception IOException  if an I/O error occurs
	 */
	protected int waitUntilMessageAvailable(int handle) throws IOException {
		return backend.waitUntilMessageAvailable(m_iport, handle);
	}

	/**
	 * Computes the number of transport-layer segments that would be required to
	 * send the given message.
	 *
	 * @param msgBuffer The message to be sent.
	 * @param msgLen The length of the message.
	 * @param msgType The message type: binary or text.
	 * @param hasPort Indicates if the message includes a source or destination
	 *	   port number.
	 *
	 * @return The number of transport-layer segments required to send the
	 *	   message.
	 */
	private int numberOfSegments(byte msgBuffer[], int msgLen, int msgType, boolean hasPort) {
		/** The number of bytes in one message fragment. */
	    int fragmentSize = 0;

	    /** Extra header size for concatenated messages. */
	    int headerSize = 0;

	    /**
	     * Extra header space used for the port number in the destination address.
	     */
	    int portHeaderSize = 0;

	    /* The number of segments required to send the message. */
	    int numSegments = 0;

	    /* When a port number exists, it occupies part of the header. */
	    if (hasPort) {
	        portHeaderSize = 7;
	    }

	    /*
	     * Determine the sizes for the various encoding types.
	     */
	    switch (msgType) {
	        case GSM_TEXT:
	            /* 160/152/145 */
	            fragmentSize = 160;  /* The number of 7-bit values. */
	            headerSize = 8;
	            break;
	        case GSM_UCS2:
	            /* 140/132/126 */
	            fragmentSize = 140;
	            headerSize = 8;
	            if (portHeaderSize != 0) {
	                portHeaderSize = 6;
	            }
	            break;
	        case GSM_BINARY:
	        default:
	            /* 140/133/126 */
	            fragmentSize = 140;
	            headerSize = 7;
	            if (portHeaderSize != 0) {
	                portHeaderSize = 6;
	            }
	            break;
	    }

	    fragmentSize = fragmentSize - portHeaderSize;

	    if (msgLen < fragmentSize) {
	        numSegments = 1;
	    } else {
	        fragmentSize = fragmentSize - headerSize;
	        numSegments = (msgLen + fragmentSize - 1) / fragmentSize;
	    }
		
		return numSegments;
	}

}
