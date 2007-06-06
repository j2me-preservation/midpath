package org.thenesis.midpath.ui.virtual;

import com.sun.midp.events.EventMapper;

public class NullBackend implements UIBackend {
	
	VirtualSurface rootVirtualSurface;
	
	public NullBackend(int w, int h) {
		rootVirtualSurface = new VirtualSurfaceImpl(w,h);
	}

	public EventMapper getEventMapper() {
		return null;
	}

	public VirtualSurface createSurface(int w, int h) {
		return new VirtualSurfaceImpl(w,h);
	}
	
	public VirtualSurface getRootSurface() {
		return rootVirtualSurface;
	}

	public void updateSurfacePixels(int x, int y, long width, long heigth) {
		// Do nothing
	}
	
	private class VirtualSurfaceImpl extends VirtualSurface {
		
		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w*h];
			this.width = w;
			this.height = h;
		}
		
	}

	public void close() {
		// Do nothing
	}

	

}
