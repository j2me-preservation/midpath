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
package com.sun.perseus.j2d;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Image;

/**
 * This class contains utility methods which make <code>ImageLoader</code> 
 * implementations easier.
 *
 * @version $Id: ImageLoaderUtil.java,v 1.12 2006/04/21 06:34:56 st125089 Exp $
 */
public class ImageLoaderUtil extends AbstractImageLoaderUtil {

	/**
	 * Default constructor
	 */
	public ImageLoaderUtil() {
		super();
	}


	/**
	 * Utility method to turn an image href into an Image. This assumes
	 * that the href points to an <b>external</b> resource. This can 
	 * be tested on the href with the <code>isDataURI</code> method.
	 *
	 * @param href the address from which to load the image content.
	 * @return the loaded image or <code>brokenImage</code> if the image
	 *         could not be loaded.
	 */
	public RasterImage getExternalImage(final String href) {

		//System.out.println("getExternalImage(), Image href = " + href);

		Image img = null;
		StreamConnection c = null;
		InputStream s = null;

		try {
			c = (StreamConnection) Connector.open(href);
			s = c.openInputStream();
			img = Image.createImage(s);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.err.println("returning broken image");
			return brokenImage;
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			System.err.println("returning broken image");
			return brokenImage;

		} finally {

			try {
				if (s != null)
					s.close();
				if (c != null)
					c.close();
			} catch (IOException ioe) {

				//note : we have already read the image successfully. 
				//So don't fail, simply print stacktrace.

				ioe.printStackTrace();
			}

		}
		return (new RasterImage(img));

	}

	/**
	 * Creates a RasterImage from a byte array.
	 *
	 * @param b the byte array containing the encoded image
	 *        data.
	 */
	public RasterImage createImage(final byte[] imageData) {
		Image img = Image.createImage(imageData, 0, imageData.length);
		return (new RasterImage(img));
	}

	/**
	 * Creates a RasterImage from an int array containing the pixel data
	 */
	public RasterImage createImage(int[] imageData, int width, int height, boolean processAlpha) {
		Image img = Image.createRGBImage(imageData, width, height, processAlpha);
		return new RasterImage(img);
	}


	public RasterImage createImage(InputStream is) throws IOException {
		Image img = Image.createImage(is);
		return new RasterImage(img);
	}

	
}
