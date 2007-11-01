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
package org.thenesis.midpath.ui.backend.fb;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.main.Configuration;

public class FBBackend extends FBCanvas implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private FBEventMapper eventMapper = new FBEventMapper();

	private boolean dragEnabled = false;
	
	private static String keyboardDeviceName;
	private static String fbDeviceName;
	private static String mouseDeviceName;
	private static String touchscreenDeviceName;
	
	static {
		fbDeviceName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend.fb.framebufferDevice", null);
		keyboardDeviceName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend.fb.keyboardDevice", null);
		mouseDeviceName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend.fb.mouseDevice", null);
		touchscreenDeviceName = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend.fb.touchscreenDevice", null);
	}
	

	public FBBackend(int w, int h) {
		
		super(keyboardDeviceName, mouseDeviceName, touchscreenDeviceName, fbDeviceName, w, h);
		
		rootVirtualSurface = new VirtualSurfaceImpl(w, h);
		
		// Initialize the FB canvas and start the FB thread
		open();
	}

	public EventMapper getEventMapper() {
		return eventMapper;
	}

	public VirtualSurface createSurface(int w, int h) {
		return new VirtualSurfaceImpl(w, h);
	}

	public VirtualSurface getRootSurface() {
		return rootVirtualSurface;
	}

	public void updateSurfacePixels(int x, int y, long width, long height) {
		//System.out.println("[DEBUG] FBBackend.updateSurfacePixels(): " + x + " " + y + " " + width + " " + height);	
		updateARGBPixels(rootVirtualSurface.data, x, y, (int)width, (int)height);
	}
	
	public void close() {
		super.close();
	}

	private class VirtualSurfaceImpl extends VirtualSurface {

		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}

	}

	/* (non-Javadoc)
	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onKeyEvent(int, int, int)
	 */
	public void onKeyEvent(boolean pressed, int keyCode, char c) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FBBackend.keyEvent(): key code: " + keyCode + " char: " + c + " (" + ((int)c) + ")");

		NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		// Set event type (intParam1)
		if (pressed) {
			nativeEvent.intParam1 = EventConstants.PRESSED;
		} else {
			nativeEvent.intParam1 = EventConstants.RELEASED;
		} 
		// Set event key code (intParam2)
		int internalCode = FBEventMapper.mapToInternalEvent(c);
		if (internalCode != 0) {
			nativeEvent.intParam2 = internalCode;
		} else if (c != 0) {
			nativeEvent.intParam2 = c;
		} else {
			return;
		}
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);
	}
	
//	/* (non-Javadoc)
//	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onMouseButtonEvent(int, int, int)
//	 */
	public void onMouseButtonEvent(int x, int y, int button) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FBBackend.buttonEvent()");

		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		
		if (button != 0) {
			dragEnabled = true;
			nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
		} else {
			dragEnabled = false;
			nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
		}
		
		nativeEvent.intParam2 = x; // x
		nativeEvent.intParam3 = y; // y
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);
		
		//Display d = BaseMIDletSuiteLauncher.displayContainer.findDisplayById(1).getDisplay();
		//d.graphicsQ.queueRefresh(0, 0, rootVirtualSurface.width, rootVirtualSurface.height);
		//d.scheduleRepaint();
		
	}
	
	/* (non-Javadoc)
	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onMouseMoveEvent(int, int)
	 */
	public void onMouseMoveEvent(int x, int y) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] FBBackend.motionEvent(): " + dragEnabled);

		if (dragEnabled) {
			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
			nativeEvent.intParam2 = x; // x
			nativeEvent.intParam3 = y; // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);
		}
	}
//	
//	/* (non-Javadoc)
//	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onWindowDeleteEvent()
//	 */
//	public void onCloseEvent() {
//		if (Logging.TRACE_ENABLED)
//			System.out.println("Window delete event received: ");
//		
//		NativeEvent nativeEvent = new NativeEvent(EventTypes.SHUTDOWN_EVENT);
//		EventQueue.getEventQueue().post(nativeEvent);
//	}

}
