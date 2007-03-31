package org.thenesis.midpath.ui.virtual;

import com.sun.midp.events.EventMapper;

public interface UIBackend {
	
	public EventMapper getEventMapper();
	public VirtualSurface createSurface(int w, int h);
	public VirtualSurface getRootSurface();
	
	public void updateSurfacePixels(int x, int y, long widht, long heigth);

}
