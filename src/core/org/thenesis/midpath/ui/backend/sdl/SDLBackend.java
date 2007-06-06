/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
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
 */
package org.thenesis.midpath.ui.backend.sdl;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import sdljava.video.SDLSurface;

import com.sun.midp.events.EventMapper;

public class SDLBackend implements UIBackend {
	
	private SDLToolkit toolkit;
	private VirtualSurface rootVirtualSurface;
	
	public SDLBackend(int w, int h) {
		toolkit  = new SDLToolkit();
		toolkit.initialize(w, h);
		
		rootVirtualSurface = new VirtualSurfaceImpl(w,h);
	}

	public EventMapper getEventMapper() {
		return toolkit.getEventMapper();
	}

	public VirtualSurface createSurface(int w, int h) {
		return new VirtualSurfaceImpl(w,h);
	}
	
	public VirtualSurface getRootSurface() {
		return rootVirtualSurface;
	}

	public void updateSurfacePixels(int x, int y, long width, long heigth) {
		
		SDLSurface sdlSurface = ((SDLGraphics)toolkit.getRootGraphics()).getSurface();
		
		// Draw rgb field on the surface
		sdlSurface.setPixelData32(rootVirtualSurface.data);
		
		toolkit.refresh(1, x, y, width, heigth);

	}
	
	public void close() {
		toolkit.close();
	}
	
	private class VirtualSurfaceImpl extends VirtualSurface {
		
		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w*h];
			this.width = w;
			this.height = h;
		}
		
	}

	

}
