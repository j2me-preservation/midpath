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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

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

		RasterImage img = null;
		URLConnection c = null;
		InputStream s = null;

		try {
			URL url = new URI(href).toURL();
			c = (URLConnection) url.openConnection();
			s = c.getInputStream();
			img = createImage(s);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.err.println("returning broken image");
			return brokenImage;
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			System.err.println("returning broken image");
			return brokenImage;
		} catch (URISyntaxException use) {
			use.printStackTrace();
			System.err.println("returning broken image");
			return brokenImage;
		} finally {

			try {
				if (s != null)
					s.close();
				if (c != null)
					c = null;
			} catch (IOException ioe) {

				//note : we have already read the image successfully. 
				//So don't fail, simply print stacktrace.

				ioe.printStackTrace();
			}

		}
		return img;

	}

	/**
	 * Creates a RasterImage from a byte array.
	 *
	 * @param b the byte array containing the encoded image
	 *        data.
	 */
	public RasterImage createImage(final byte[] imageData) {
		BufferedImage img;
		try {
			img = ImageIO.read(new ByteArrayInputStream(imageData));
			return (new RasterImage(img));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Creates a RasterImage from an int array containing the pixel data
	 */
	public RasterImage createImage(int[] imageData, int width, int height, boolean processAlpha) {
		int type = processAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage img = new BufferedImage(width, height, type);
		img.setRGB(0, 0, width, height, imageData, 0, width);
		return new RasterImage(img);
	}

	public RasterImage createImage(InputStream is) throws IOException {
		BufferedImage img = ImageIO.read(is);
		return (new RasterImage(img));

	}

}
