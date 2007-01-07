package org.thenesis.midpath.ui.virtual;

import org.thenesis.midpath.ui.SDLGraphics;
import org.thenesis.midpath.ui.SDLToolkit;

import sdljava.SDLException;
import sdljava.video.SDLSurface;

import com.sun.midp.events.EventMapper;

public class SDLBackend implements UIBackend {
	
	SDLToolkit toolkit;
	
	VirtualSurface rootVirtualSurface;
	
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
		try {
			sdlSurface.updateRect(x, y, width, heigth);
		} catch (SDLException e) {
			e.printStackTrace();
		}
		
	}
	
	private class VirtualSurfaceImpl extends VirtualSurface {
		
		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w*h];
			this.width = w;
			this.height = h;
		}
		
	}

	

}
