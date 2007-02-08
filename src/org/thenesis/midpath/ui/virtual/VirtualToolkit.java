/*
 * MIDPath - Copyright (C) 2006 Guillaume Legris, Mathieu Legris
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details. 
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
package org.thenesis.midpath.ui.virtual;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.FontPeer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Toolkit;

import org.thenesis.midpath.ui.backend.awt.AWTBackend;

import com.sun.midp.events.EventMapper;
import com.sun.midp.log.Logging;
import com.sun.midp.main.Configuration;

public class VirtualToolkit extends Toolkit {

	private VirtualSurface rootSurface;
	private VirtualGraphics rootPeer;
	private UIBackend backend;

	public VirtualToolkit() {
	}

	public void initialize(int w, int h) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] VirtualToolkit.initialize(): VideoSurface: " + rootSurface);
		
		String backendName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend", "null");
		if (backendName.equalsIgnoreCase("SDL")) {
			backend = new SDLBackend(w, h);
		} else if (backendName.equalsIgnoreCase("AWT")) {
			backend = new AWTBackend(w, h);
		} else {
			backend = new NullBackend(w, h);
		}
		
		rootSurface = backend.getRootSurface();
		rootPeer = new VirtualGraphics(rootSurface);
	}

	public Graphics getRootGraphics() {
		return rootPeer;
	}

	//	public SDLGraphics createGraphics(int w, int h) {
	//		try {
	//			return new SDLGraphics(createSDLSurface(w, h));
	//		} catch (SDLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			return null;
	//		}
	//	}

	public Graphics createGraphics(Image image) {
		if (image instanceof VirtualImage) {
			return new VirtualGraphics(((VirtualImage) image).surface);
		} else {
			// FIXME ??
			return null;
		}
	}

	public void refresh(int displayId, int x, int y, long widht, long heigth) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Toolkit.refresh(): x=" + x + " y=" + y + " widht=" + widht + " heigth="
					+ heigth);

		
		backend.updateSurfacePixels(x, y, widht, heigth);
		//rootSurface.updateRect(x, y, widht, heigth);
		
	}

	public Image createImage(int w, int h) {
		return new VirtualImage(w, h);
	}

	public Image createImage(Image source) {
		if (!source.isMutable()) {
			return source;
		}
		return new VirtualImage((VirtualImage) source);
	}

	public Image createImage(byte[] imageData, int imageOffset, int imageLength) throws IOException {
		return new VirtualImage(imageData, imageOffset, imageLength);
	}

	public Image createImage(InputStream stream) throws IOException {
		return new VirtualImage(stream);
	}

	public Image createImage(String name) throws IOException {
		//System.out.println("[DEBUG] SDLToolkit.createImage(String name): " + name);
		InputStream is = getClass().getResourceAsStream(name);
		//System.out.println("[DEBUG] SDLToolkit.createImage(String name): " + is.read());
		return createImage(is);
	}

	public Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) throws IOException {
		return new VirtualImage(rgb, width, height, processAlpha);
	}

	public Image createImage(Image image, int x, int y, int width, int height, int transform) throws IOException {
		return new VirtualImage(image, x, y, width, height, transform);
	}

	public EventMapper getEventMapper() {
		return backend.getEventMapper();
	}

	/**
	 * Construct a new FontPeer object
	 *
	 * @param face The face to use to construct the Font
	 * @param style The style to use to construct the Font
	 * @param size The point size to use to construct the Font
	 */
	public FontPeer createFontPeer(int face, int style, int size) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG]VirtualToolkit.createFontPeer(): size=" + size);

		String fontRendererName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.fontRenderer", "raw");
		if (fontRendererName.equalsIgnoreCase("BDF")) {
			return new BDFFontPeer(face, style, size);
		} else {
			return new RawFontPeer(face, style, size);
		}
		
		//return new RawFontPeer(face, style, size);
		//return new BDFFontPeer(face, style, size);
	}

	public Image createImage(int[] rgb, int width, int height, boolean processAlpha) {
		return new VirtualImage(rgb, width, height, processAlpha);
	}
	
	UIBackend getBackend() {
		return backend;
	}

}
