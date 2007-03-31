/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.midp.midlet;

import java.util.Hashtable;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * Implements a the required MIDletSuite functionality needed by the
 * system. The class is only needed for internal romized midlets.
 */
public class InternalMIDletSuiteImpl implements MIDletSuite {
	/** Display name for permission dialogs. */
	private String displayName = null;

	/** The ID of this suite. */
	private String id;

	/** Permissions for this suite. */
	private byte[] permissions;

	/** Flag for trusted suites. If true the system trust icon is displayed. */
	private boolean trusted;

	/** Suite properties for this suite. */
	//private Properties properties;
	private Hashtable properties = new Hashtable();

	/**
	 * number of midlets in this suite. For a rommized suite assume 1.
	 */
	private int numberOfMidlets = 1;

	/**
	 * Creates MIDletSuite for rommized MIDlet.
	 *
	 * @param theDisplayName display name to use in permission dialogs,
	 *        and in the MIDlet proxy list
	 * @param theId ID to separate this suite's resources from others
	 *
	 * @return new MIDletSuite object
	 */
	public static MIDletSuite create(String theDisplayName, String theId) {
		return new InternalMIDletSuiteImpl(theDisplayName, theId);
	}

	/**
	 * Creates MIDletSuite for rommized MIDlet.
	 *
	 * @param theDisplayName display name to use in permission dialogs,
	 *        and in the MIDlet proxy list
	 * @param theId unique identifier for this suite
	 */
	private InternalMIDletSuiteImpl(String theDisplayName, String theId) {
		if (theDisplayName != null) {
			displayName = theDisplayName;
		} else {
			displayName = Resource.getString(ResourceConstants.AMS_SYSTEM_SUITE_NAME);
		}

		id = theId;

		trusted = true;

		permissions = (Permissions.forDomain(Permissions.MANUFACTURER_DOMAIN_BINDING))[Permissions.CUR_LEVELS];

		//properties = new Properties();
	}

	/**
	 * Provides the number of MIDlets in this suite.
	 *
	 * @return number of MIDlet in the suite
	 */
	public int getNumberOfMIDlets() {
		return numberOfMidlets;
	}

	/**
	 * Get the name of a MIDlet to display to the user.
	 *
	 * @param className class name of the MIDlet to be checked
	 *
	 * @return name to display to the user
	 */
	public String getMIDletName(String className) {
		/*
		 * Each internal MIDlet runs in it own suite,
		 * just return the suite name
		 */
		return displayName;
	}

	/**
	 * Gets the unique ID of the suite.
	 *
	 * @return suite ID
	 */
	public String getID() {
		return id;
	}

	/**
	 * Gets a property of the suite. A property is an attribute from
	 * either the application descriptor or JAR Manifest.
	 *
	 * @param key the name of the property
	 * @return A string with the value of the property.
	 *    <code>null</code> is returned if no value is available for
	 *          the key.
	 */
	public String getProperty(String key) {
		//return properties.getProperty(key);
		// TODO
		return (String) properties.get(key);
	}

	/**
	 * Replace or add a property to the suite for this run only.
	 *
	 * @param token token with the AMS permission set to allowed,
	 *        can be null to use the suite's permission
	 * @param key the name of the property
	 * @param value the value of the property
	 *
	 * @exception SecurityException if the caller's token does not have
	 *            internal AMS permission
	 */
	public void setTempProperty(SecurityToken token, String key, String value) {
		if (token != null) {
			token.checkIfPermissionAllowed(Permissions.AMS);
		} else {
			MIDletSuite current = MIDletStateHandler.getMidletStateHandler().getMIDletSuite();

			current.checkIfPermissionAllowed(Permissions.AMS);
		}

		//properties.setProperty(key, value);
		properties.put(key, value);
	}

	/**
	 * Checks to see the suite has the ALLOW level for specific permission.
	 * This is used for by internal APIs that only provide access to
	 * trusted system applications.
	 *
	 * @param permission permission ID from com.sun.midp.security.Permissions
	 *
	 * @exception SecurityException if the suite is not allowed the permission
	 */
	public void checkIfPermissionAllowed(int permission) {
		if (checkPermission(permission) != 1) {
			throw new SecurityException(SecurityToken.STD_EX_MSG);
		}
	}

	/**
	 * Check for permission and throw an exception if not allowed.
	 * May block to ask the user a question.
	 *
	 * @param permission ID of the permission to check for,
	 *      the ID must be from
	 *      {@link com.sun.midp.security.Permissions}
	 * @param resource string to insert into the question, can be null if
	 *        no %2 in the question
	 *
	 * @exception SecurityException if the permission is not
	 *            allowed by this token
	 * @exception InterruptedException if another thread interrupts the
	 *   calling thread while this method is waiting to preempt the
	 *   display.
	 */
	public void checkForPermission(int permission, String resource) throws InterruptedException {
		checkForPermission(permission, resource, null);
	}

	/**
	 * Checks for permission and throw an exception if not allowed.
	 * May block to ask the user a question.
	 *
	 * @param permission ID of the permission to check for,
	 *      the ID must be from
	 *      {@link com.sun.midp.security.Permissions}
	 * @param resource string to insert into the question, can be null if
	 *        no %2 in the question
	 * @param extraValue string to insert into the question,
	 *        can be null if no %3 in the question
	 *
	 * @exception SecurityException if the permission is not
	 *            allowed by this token
	 * @exception InterruptedException if another thread interrupts the
	 *   calling thread while this method is waiting to preempt the
	 *   display.
	 */
	public void checkForPermission(int permission, String resource, String extraValue) throws InterruptedException {
		checkIfPermissionAllowed(permission);
	}

	/**
	 * Gets the status of the specified permission.
	 * If no API on the device defines the specific permission
	 * requested then it must be reported as denied.
	 * If the status of the permission is not known because it might
	 * require a user interaction then it should be reported as unknown.
	 *
	 * @param permission to check if denied, allowed, or unknown
	 *
	 * @return 0 if the permission is denied; 1 if the permission is allowed;
	 *  -1 if the status is unknown
	 */
	public int checkPermission(String permission) {
		for (int i = 0; i < Permissions.NUMBER_OF_PERMISSIONS; i++) {
			if (Permissions.getName(i).equals(permission)) {
				return checkPermission(i);

			}
		}

		return 0;
	}

	/**
	 * Check to see the suite has the ALLOW level for specific permission.
	 * This is used for by internal APIs that only provide access to
	 * trusted system applications.
	 *
	 * @param permission permission ID from com.sun.midp.security.Permissions
	 *
	 * @param permission to check if denied, allowed, or unknown
	 *
	 * @return 0 if the permission is denied; 1 if the permission is allowed;
	 *  -1 if the status is unknown
	 */
	private int checkPermission(int permission) {
		if (permission < 0 || permission >= permissions.length) {
			// report denied
			return 0;
		}

		switch (permissions[permission]) {
		case Permissions.ALLOW:
		case Permissions.BLANKET_GRANTED:
			// report allowed
			return 1;

		case Permissions.SESSION:
		case Permissions.BLANKET:
		case Permissions.ONE_SHOT:
		case Permissions.DENY:
			// report unknown
			return -1;

		default:
			break;
		}

		// report denied
		return 0;
	}

	/**
	 * Indicates if the named MIDlet is registered in the suite
	 * with MIDlet-&lt;n&gt; record in the manifest or
	 * application descriptor.
	 *
	 * @param midletClassName class name of the MIDlet to be checked
	 *
	 * @return true if the MIDlet is registered
	 */
	public boolean isRegistered(String midletClassName) {
		// Rommized MIDlets don't have JAD MIDlet-n entries.
		return false;
	}

	/**
	 * Indicates if this suite is trusted.
	 * (not to be confused with a domain named "trusted",
	 * this is used for extra checks beyond permission checking)
	 *
	 * @return true if the suite is trusted false if not
	 */
	public boolean isTrusted() {
		return trusted;
	}

	/**
	 * Get state of classes preverification within the suite.
	 *
	 * @return true because internal suite should be always preverified.
	 */
	public boolean isVerified() {
		return true;
	}

	/**
	 * Determine if the a MIDlet from this suite can be run. Note that
	 * disable suites can still have their settings changed and their
	 * install info displayed.
	 *
	 * @return true if suite is enabled, false otherwise
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Asks the user want to interrupt the current MIDlet with
	 * a new MIDlet that has received network data.
	 *
	 * @param connection connection to place in the permission question or
	 *        null for alarm
	 *
	 * @return true if the use wants interrupt the current MIDlet, else false
	 */
	public boolean permissionToInterrupt(String connection) {
		// Rommized internal MIDlet can interrupt other MIDlets without asking.
		return true;
	}

	/**
	 * Gets push options for this suite.
	 *
	 * @return push options are defined in {@link PushRegistryImpl}
	 */
	public int getPushOptions() {
		// There are not push options for rommized suites.
		return 0;
	}

	/**
	 * Close the opened MIDletSuite
	 */
	public void close() {
	}
}
