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

package com.sun.midp.security;

import com.sun.midp.log.Logging;

/**
 * A utility class that initializes internal security.  Creates the internal
 * security token and distributes it to various packages around the system
 * that need it.
 * <p>
 * The security token is created here and is passed to static methods of
 * several implementation classes around the system.  These methods, typically
 * named <code>initSecurityToken()</code>, must each ensure they are called
 * only once.  This technique works for classes other than those in public API
 * packages (that is, java.* and javax.*).  This technique cannot be used for
 * class that reside in public API packages, since the initialization method
 * must be public and adding methods to public APIs is prohibited.  Instead,
 * the following alternative approach is used.
 * <p>
 * The {@link #getSecurityToken()} method gives out the security token to
 * whomever calls it.  This method is disabled (through the use of the boolean
 * variable {@link #dispensingEnabled}) in order to prevent it from handling
 * out the token to any caller.  To get the security token into a public API
 * class, we arrange for a static initialization block in the API class to get
 * the security token by calling getSecurityToken().  We enable dispensing the
 * token and then name the API class with <code>Class.forName()</code> in
 * order to force static initialization.  This pulls security token into the
 * API class.  Finally, we disable the dispensing of the security token.
 * <p>
 * The security of this technique requires no other threads to be running in
 * the system, and it also requires the API classes not to be initialized at
 * the time internal security is initialized.  This implies that internal
 * security must be initialized very early in the execution of the system.  It
 * also implies that the public API classes must not be initialized at build
 * time by the ROMizer and that they must be initialized at runtime.
 */
public final class SecurityInitializer {

    /**
     * Ensures that security initialization is done only once.
     */
    private static boolean initialized = false;

    /**
     * The primary security token for the system.
     */
    private static SecurityToken internalSecurityToken;

    /**
     * Set to <code>true</code> to allow {@link #getSecurityToken}
     * will give out the security token.
     */
    private static boolean dispensingEnabled = true;

    /**
     * Creates the primary security token and distributes it to the
     * Display class.  This method must be called very early during the
     * initialization of the system, before anything occurs that might need
     * the security token.  It also relies on the timing of class loading and
     * initialization, so it must be called before some classes are loaded.
     * It is used in isolates that don't run MIDlets.
     *
     * @return the newly created security token
     * @throws SecurityException if it or init has already been called
     * previously
     */
    public static SecurityToken initDisplay() {
        
    	if (initialized) {
        	return internalSecurityToken;
            //throw new SecurityException();
        }

        initialized = true;

        try {
            /*
             * As the first caller to create a token we can pass in null
             * for the security token.
             */
            internalSecurityToken =
                new SecurityToken(null,
                    Permissions.forDomain(
                        Permissions.MANUFACTURER_DOMAIN_BINDING));

            // NOTE: The initialization of Display will forward on the
            // initialization to Chameleon's SkinResources if Chameleon
            // is being used
            initSecurity("javax.microedition.lcdui.Display");
        } catch (Throwable t) {
            Logging.trace(t, "Unable to initialize security");
            throw new RuntimeException(t.toString());
        }

        return internalSecurityToken;
    }

    /**
     * Creates the primary security token and distributes it around the
     * system.  This method must be called very early during the
     * initialization of the system, before anything occurs that might need
     * the security token.  It also relies on the timing of class loading and
     * initialization, so it must be called before some classes are loaded.
     *
     * @return the newly created security token
     * @throws SecurityException if it or initDisplay has already
     * been called previously
     */
    public static SecurityToken init() {

        if (initialized) {
        	return internalSecurityToken;
            //throw new SecurityException();
        }

        try {
            initialized = true;

            // As the first caller to create a token we can pass in null
            // for the security token.
            internalSecurityToken =
                new SecurityToken(null,
                    Permissions.forDomain(
                        Permissions.MANUFACTURER_DOMAIN_BINDING));
            // FIXME
            
//            SecurityHandler.initSecurityToken(internalSecurityToken);
//
//            com.sun.midp.midletsuite.MIDletSuiteStorage.initSecurityToken(
//                internalSecurityToken);
//            com.sun.midp.io.j2me.http.Protocol.initSecurityTokenStatically(
//                internalSecurityToken);
//            com.sun.midp.io.j2me.datagram.Protocol.initSecurityToken(
//                internalSecurityToken);
//
//            DisplayEventHandlerFactory.
//                initSecurityToken(internalSecurityToken);
//            com.sun.midp.midlet.MIDletPeer.initSecurityToken(
//                internalSecurityToken);
//            com.sun.midp.midlet.MIDletStateHandler.initSecurityToken(
//                internalSecurityToken);
//            com.sun.mmedia.MMEventHandler.initSecurityToken(
//                internalSecurityToken);
//            com.sun.midp.io.j2me.push.PushRegistryImpl.initSecurityToken(
//                    internalSecurityToken);
//
//            /*
//             * Conditional initializations for classes that may or may
//             * not be present.
//             */
//
//// #ifdef ENABLE_PUBLICKEYSTORE
//            initSecurityToken("com.sun.midp.publickeystore.WebPublicKeyStore");
//// #endif ENABLE_PUBLICKEYSTORE
//
//// #ifdef ENABLE_SSL
//            initSecurityToken("com.sun.midp.io.j2me.https.Protocol");
//            initSecurityToken("com.sun.midp.io.j2me.ssl.Protocol");
//// #endif ENABLE_SSL
//
//// #ifdef ENABLE_JSR_205
//            initSecurityToken("com.sun.midp.io.j2me.sms.Protocol");
//            initSecurityToken("com.sun.midp.io.j2me.cbs.Protocol");
//            initSecurityToken("com.sun.midp.io.j2me.mms.Protocol");
//// #endif ENABLE_JSR_205
//
//// #ifdef ENABLE_JSR_135
//            initSecurityToken("com.sun.mmedia.WavRecordCtrl");
//            initSecurityToken("com.sun.mmedia.protocol.FileConnectionSubstitute");
//// #endif ENABLE_JSR_135
//
//// #ifdef ENABLE_JSR_234
//            initSecurityToken("com.sun.mmedia.WavRecordCtrl");
//            initSecurityToken("com.sun.mmedia.protocol.FileConnectionSubstitute");
//// #endif ENABLE_JSR_234
//
//// #ifdef ENABLE_JSR_177
//            initSecurityToken("com.sun.midp.io.j2me.apdu.Protocol");
//            initSecurityToken("com.sun.satsa.acl.ACSlot");
//            initSecurityToken("com.sun.midp.io.j2me.jcrmi.Protocol");
//            initSecurityToken("com.sun.satsa.pki.PKIManager");
//// #endif ENABLE_JSR_177
//
//// #ifdef ENABLE_JSR_179
//            initSecurity("com.sun.j2me.location.LocationPersistentStorage");
//// #endif ENABLE_JSR_179
//
//// #ifdef ENABLE_JSR_180
//            initSecurityToken("com.sun.midp.io.j2me.sip.Protocol");
//            initSecurityToken("com.sun.midp.io.j2me.sips.Protocol");
//// #endif ENABLE_JSR_180
//
//// #ifdef ENABLE_JSR_75
//            initSecurityToken("com.sun.kvem.midp.pim.PIMDatabase");
//// #endif ENABLE_JSR_75
//
//// #ifdef ENABLE_JSR_82
//            initSecurityToken("com.sun.midp.jsr082.SecurityInitializer");
//// #endif ENABLE_JSR_82
//
//// #ifdef ENABLE_JSR_238
//            initSecurityToken("com.sun.midp.jsr238.SecurityInitializer");
//// #endif ENABLE_JSR_238
//            initSecurity("com.sun.j2me.global.NormalizationTableImpl");
//            initSecurity("com.sun.j2me.global.CollationElementTableImpl");
//
//            initSecurity("javax.microedition.rms.RecordStore");
//
//            initSecurity("javax.microedition.midlet.MIDlet");
//
//            // NOTE: The initialization of Display will forward on the
//            // initialization to Chameleon's SkinResources if Chameleon
//            // is being used
//
//            initSecurity("javax.microedition.lcdui.Display");
//
//// #ifdef ENABLE_JSR211
//            initSecurity("javax.microedition.content.Registry");
//            initSecurity("com.sun.midp.content.RegistryImpl");
//            initSecurity("com.sun.midp.content.InvocationImpl");
//// #endif ENABLE_JSR211
//
//// IMPL NOTE: PauseTest is temporarily disabled.
////            // Pass the security token to the standalone pause test.
////            initSecurity("com.sun.midp.internal.test.PauseTest");
//
//            // Pass the security token to the i3 test framework.
//
//// #ifdef ENABLE_I3_TEST
//            initSecurity("com.sun.midp.i3test.TestCase");
//// #endif ENABLE_I3_TEST
//
//// #ifdef ENABLE_JSR_75
//            initSecurity("javax.microedition.io.file.FileSystemEventHandler");
//// #endif ENABLE_JSR_75
//
//// #ifdef ENABLE_JSR_229
//            initSecurity("com.sun.j2me.payment.CldcPaymentModule");
//            initSecurity("com.sun.j2me.payment.CldcTransactionStoreImpl");
//// #endif ENABLE_JSR_229
//
//// #ifdef ENABLE_JSR_226
//            initSecurity("com.sun.perseus.platform.ResourceHandler");
//            initSecurity("com.sun.perseus.builder.DefaultFontFace");
//// #endif ENABLE_JSR_226

        } catch (Throwable t) {
            Logging.trace(t, "Unable to initialize security");
            throw new RuntimeException(t.toString());
        }

        return internalSecurityToken;
    }

    /**
     * Method to initialize an ImplicitlyTrusted class that may
     * or may not be present. If the class can be found and
     * and if an instance of ImplicitlyTrusted can be created it is
     * called with the internal security token.
     * ClassNotFoundExceptions to the class initialization are logged.
     * Other exceptions related to creating an instance are thrown.
     *
     * @param classname the name of the class to initialize.
     * @exception InstantiationException may be thrown by newInstance
     * @exception IllegalAccessException may be thrown by newInstance
     * @exception ClassCastException may be thrown if the class does
     * not implement ImplicitlyTrustedClass
     */
    private static void initSecurityToken(String classname)
            throws InstantiationException, IllegalAccessException
    {
        try {
            ImplicitlyTrustedClass trusted = (ImplicitlyTrustedClass)
                    Class.forName(classname).newInstance();
            trusted.initSecurityToken(internalSecurityToken);
        } catch (ClassNotFoundException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Unable to initialize security for " +
                        classname);
            }
        }
    }

    /**
     * Method to trigger the static initializer for a class.
     * The {@link #dispensingEnabled} is enabled only for the
     * duration of the class initialization to allow that static
     * initializer to call {@link #getSecurityToken}.
     * <p>
     * ClassNotFoundExceptions are logged but other exceptions
     * caused by class initialization are thrown.
     * @param classname the name of the class to initialize.
     */
    private static void initSecurity(String classname) {
        try {
            /*
            * Enable giving out the security token, force static
            * initialization of named classes, then disable giving
            * out the security token.  This allows the static initializers
            * of the named classes to fetch the security token.
            * ClassNotFoundException is ignored; these may occur
            * if the class has been configured out of the system.
            */
            dispensingEnabled = true;
            Class.forName(classname);
        } catch (ClassNotFoundException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Class not found");
            }
        } finally {
            dispensingEnabled = false;
        }
    }

    /**
     * Gets the internal security token.  This method is enabled only within a
     * narrow window of time during initialization.
     *
     * @return the internal security token
     * @throws SecurityException if handing out the token is disabled
     */
    public static SecurityToken getSecurityToken() {
        if (dispensingEnabled) {
            return internalSecurityToken;
        } else {
            throw new SecurityException();
        }
    }

    /**
     * Prevents creation of any instances.
     */
    private SecurityInitializer() {
    }
}
