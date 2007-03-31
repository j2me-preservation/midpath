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
package com.sun.midp.util;

import com.sun.midp.security.SecurityToken;


/**
 * The ResourceHandler class is a system level utility class.
 * Its purpose is to return system resources as an array of bytes
 * based on a unique String identifier. All methods in this utility
 * class should be protected through use of the SecurityToken.
 */
public class ResourceHandler {

    /**
     * Load a resource from the system and return it as a byte array.
     * This method is used to load system level resources, such as
     * images, sounds, properties, etc.
     *
     * @param token the SecurityToken to use to grant permission to
     *              execute this method.
     * @param resource a String identifier which can uniquely describe
     *                 the location of the resource to be loaded.
     * @return a byte[] containing the resource retrieved from the
     *         system. null if the resource could not be found.
     */
    public static byte[] getSystemResource(SecurityToken token,
                                           String resource)
    {
    	
    	
    	
    	return null;
    	
//        token.checkIfPermissionAllowed(Permissions.MIDP);
//
//        byte[] resourceBuffer = null;
//        String resourceFilename = File.getConfigRoot() + resource;
//        RandomAccessStream stream = new RandomAccessStream(token);
//
//        try {
//            stream.connect(resourceFilename, Connector.READ);
//            resourceBuffer = new byte[stream.getSizeOf()];
//            stream.readBytes(resourceBuffer, 0, resourceBuffer.length);
//        } catch (java.io.IOException e) {
//            resourceBuffer = null;
//        } finally {
//            try {
//                stream.disconnect();
//            } catch (java.io.IOException ignored) {
//	    }
//        }
//
//        return resourceBuffer;
    	
    	
    }
}

