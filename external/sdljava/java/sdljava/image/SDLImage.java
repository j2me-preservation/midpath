package sdljava.image;
/**
 *  sdljava - a java binding to the SDL API
 *  Copyright (C) 2004  Ivan Z. Ganza
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA
 *
 *  Ivan Z. Ganza (ivan_ganza@yahoo.com)
 */
import sdljava.x.swig.*;

import sdljava.SDLMain;
import sdljava.SDLException;
import sdljava.video.SDLSurface;

import sdljava.util.BufferUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;
/**
 * SDL_image is an image loading library that is used with the SDL
 * library, and almost as portable. It allows a programmer to use
 * multiple image formats without having to code all the loading and
 * conversion algorithms themselves.
 *
 * @author Ivan Z. Ganza
 * @version $Id: SDLImage.java,v 1.4 2005/01/07 04:01:43 ivan_ganza Exp $
 */
public class SDLImage {
    
    /**
     * Load file for use as an image in a new surface. This actually
     * calls IMG_LoadTyped_RW, with the file extension used as the
     * type string. This can load all supported image files, including
     * TGA as long as the filename ends with ".tga". It is best to
     * call this outside of event loops, and rather keep the loaded
     * images around until you are really done with them, as disk
     * speed and image conversion to a surface is not that
     * speedy. 
     *
     * @param  file Path to image file
     * @return The loaded image as an <code>SDLSurface</code>
     * @exception SDLException if an error occurs
     */
    public static SDLSurface load(String file) throws SDLException {
	SDL_Surface swigSurface = SWIG_SDLImage.IMG_Load(file);
	if (swigSurface == null) {
	    throw new SDLException(SDLMain.getError());
	}
	return new SDLSurface(swigSurface);
    }

    /**
     * Load file for use as an image in a new surface. This actually
     * calls IMG_LoadTyped_RW, with the file extension used as the
     * type string. This can load all supported image files, including
     * TGA as long as the filename ends with ".tga". It is best to
     * call this outside of event loops, and rather keep the loaded
     * images around until you are really done with them, as disk
     * speed and image conversion to a surface is not that
     * speedy. 
     *
     * @param buf a <I>DIRECT</I> <code>Buffer</code> value with the image data
     * @return The loaded image as an <code>SDLSurface</code>
     * @exception SDLException if an error occurs
     */
    public static SDLSurface load(Buffer buf) throws SDLException {
	SDL_Surface swigSurface = SWIG_SDLImage.SWIG_IMG_Load_Buffer(buf, buf.capacity());
	if (swigSurface == null) {
	    throw new SDLException(SDLMain.getError());
	}
	return new SDLSurface(swigSurface);
    }

    /**
     * Load file for use as an image in a new surface. This actually
     * calls IMG_LoadTyped_RW, with the file extension used as the
     * type string. This can load all supported image files, including
     * TGA as long as the filename ends with ".tga". It is best to
     * call this outside of event loops, and rather keep the loaded
     * images around until you are really done with them, as disk
     * speed and image conversion to a surface is not that
     * speedy. 
     *
     * @param data a <code>byte[]</code> value
     * @return The loaded image as an <code>SDLSurface</code>
     * @exception SDLException if an error occurs
     * <P>
     * This method automatically creates the NIO Direct Buffer from the given byte array
     */
    public static SDLSurface load(byte[] data) throws SDLException {
	ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
	buf.put(data);

	return load(buf);
    }

    /**
     * Load file for use as an image in a new surface. This actually
     * calls IMG_LoadTyped_RW, with the file extension used as the
     * type string. This can load all supported image files, including
     * TGA as long as the filename ends with ".tga". It is best to
     * call this outside of event loops, and rather keep the loaded
     * images around until you are really done with them, as disk
     * speed and image conversion to a surface is not that
     * speedy. 
     *
     * @param url an <code>URL</code> value
     * @return The loaded image as an <code>SDLSurface</code>
     * @exception SDLException if an error occurs
     * <P>
     * This method automatically creates the NIO Direct Buffer
     */
    public static SDLSurface load(URL url) throws SDLException, IOException, MalformedURLException {
	return load( BufferUtil.readURL(url));
    }

    /**
     * Load file for use as an image in a new surface. This actually
     * calls IMG_LoadTyped_RW, with the file extension used as the
     * type string. This can load all supported image files, including
     * TGA as long as the filename ends with ".tga". It is best to
     * call this outside of event loops, and rather keep the loaded
     * images around until you are really done with them, as disk
     * speed and image conversion to a surface is not that
     * speedy. 
     *
     * @param in an <code>InputStream</code> value
     * @return The loaded image as an <code>SDLSurface</code>
     * @exception SDLException if an error occurs
     * <P>
     * This method automatically creates the NIO Direct Buffer
     */
    public static SDLSurface load(InputStream in ) throws SDLException, IOException {
	return load( BufferUtil.readInputStream(in));
    }
}