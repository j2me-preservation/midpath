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

public class FBBackend extends FBCanvas implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private FBEventMapper eventMapper = new FBEventMapper();

	private boolean dragEnabled = false;
	
	private static String keyboardDeviceName;
	private static String fbDeviceName;
	
	static {
		keyboardDeviceName = "/dev/tty5";
		fbDeviceName = "/dev/fb0";
	}
	

	public FBBackend(int w, int h) {
		
		super(keyboardDeviceName, fbDeviceName, w, h);
		
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
		//System.out.println("[DEBUG] QTBackend.updateSurfacePixels(): " + x + " " + y + " " + width + " " + height);	
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

//	/* (non-Javadoc)
//	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onMouseButtonEvent(int, int, int)
//	 */
//	public void onMouseButtonEvent(int x, int y, int state) {
//		if (Logging.TRACE_ENABLED)
//			System.out.println("[DEBUG] QTBackend.buttonEvent()");
//
//		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
//		
//		if (state == PRESSED) {
//			dragEnabled = true;
//			nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
//		} else {
//			dragEnabled = false;
//			nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
//		}
//		
//		nativeEvent.intParam2 = x; // x
//		nativeEvent.intParam3 = y; // y
//		// Set event source (intParam4). Fake display with id=1
//		nativeEvent.intParam4 = 1;
//
//		EventQueue.getEventQueue().post(nativeEvent);
//	}
//
//	/* (non-Javadoc)
//	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onKeyEvent(int, int, int)
//	 */
//	public void onKeyEvent(int state, int keyCode, int unicode) {
//		if (Logging.TRACE_ENABLED)
//			System.out.println("[DEBUG] QTBackend.keyEvent(): key code: " + keyCode + " char: " + (char)unicode);
//
//		char c = (char)unicode;
//
//		NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
//		// Set event type (intParam1)
//		if (state == PRESSED) {
//			nativeEvent.intParam1 = EventConstants.PRESSED;
//		} else {
//			nativeEvent.intParam1 = EventConstants.RELEASED;
//		} 
//		// Set event key code (intParam2)
//		int internalCode = FBEventMapper.mapToInternalEvent(keyCode, c);
//		if (internalCode != 0) {
//			nativeEvent.intParam2 = internalCode;
//		} else if (unicode != 0) {
//			nativeEvent.intParam2 = c;
//		} else {
//			return;
//		}
//		// Set event source (intParam4). Fake display with id=1
//		nativeEvent.intParam4 = 1;
//
//		EventQueue.getEventQueue().post(nativeEvent);
//	}
//
//	/* (non-Javadoc)
//	 * @see org.thenesis.midpath.ui.backend.qt.QTCanvas#onMouseMoveEvent(int, int)
//	 */
//	public void onMouseMoveEvent(int x, int y) {
//		if (Logging.TRACE_ENABLED)
//			System.out.println("[DEBUG] QTBackend.motionEvent(): " + dragEnabled);
//
//		if (dragEnabled) {
//			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
//			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
//			nativeEvent.intParam2 = x; // x
//			nativeEvent.intParam3 = y; // y
//			// Set event source (intParam4). Fake display with id=1
//			nativeEvent.intParam4 = 1;
//
//			EventQueue.getEventQueue().post(nativeEvent);
//		}
//	}
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
