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

package com.sun.perseus.platform;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import java.io.InputStream;

import java.io.IOException;

/**
 * This class is used to provide GZIP support to the Perseus engine by using 
 * a custom implementation of GZIPInputStream
 * <code>com.sun.perseus.platform.GZIPInputStream</code>
 * 
 *
 */
public class GZIPSupport extends AbstractGZIPSupport {


    /**
     * If GZIP encoding is supported, this method should setup the
     * HTTP Request Header to declare that GZIP encoding is supported.
     *
     * @param svgURI the url of the requested SVG resource.
     * @return a stream that does not handles GZIP uncompression.
     * @throws IOException if an I/O error happens while opening the 
     *         requested URI.
     */
    public static InputStream openHandleGZIP(String svgURI) throws IOException {

        InputConnection svgURLConnection = 
            (InputConnection)Connector.open(svgURI, Connector.READ);

        try {
            if (svgURLConnection instanceof HttpConnection) {
                setupHttpEncoding((HttpConnection)svgURLConnection);
            }

            return(svgURLConnection.openInputStream());

        } finally {
            svgURLConnection.close();
        }
    }

        /**
         *
         */
        static void setupHttpEncoding(final HttpConnection httpC) throws IOException {
            String encodings = 
                httpC.getRequestProperty(HTTP_ACCEPT_ENCODING);
        
            if (encodings == null) {
                encodings = "";
            }
        
            if (encodings.trim().length() > 0) {
                encodings += ",";
            }
            encodings += HTTP_GZIP_ENCODING;
            httpC.setRequestProperty(HTTP_ACCEPT_ENCODING, encodings);
        }


    }
