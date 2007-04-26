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
package org.thenesis.midpath.ui.backend.x11;

import gnu.util.Environment;
import gnu.x11.Connection;
import gnu.x11.Display;
import gnu.x11.Option;
import gnu.x11.Window;
import gnu.x11.event.ButtonPress;
import gnu.x11.event.ButtonRelease;
import gnu.x11.event.ClientMessage;
import gnu.x11.event.Event;
import gnu.x11.event.Expose;
import gnu.x11.event.KeyPress;
import gnu.x11.event.KeyRelease;
import gnu.x11.event.MotionNotify;
import gnu.x11.image.ZPixmap;
import gnu.x11.keysym.Misc;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.main.Configuration;

public class X11Backend implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private X11Application x11App;
	private X11EventMapper eventMapper = new X11EventMapper();

	public X11Backend(int w, int h) {
		
		rootVirtualSurface = new VirtualSurfaceImpl(w, h);
		
		String display = Configuration.getPropertyDefault("org.thenesis.midpath.ui.backend.x11.Display", ":0.0");
		Environment.setValue("DISPLAY", display);
		
		x11App = new X11Application(new String[] { "" }, w, h);
		x11App.start();
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
		x11App.setPixels(x, y, (int) width, (int) height, rootVirtualSurface.data, 0, rootVirtualSurface.width);
		x11App.paint();
	}

	private class VirtualSurfaceImpl extends VirtualSurface {
		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}
	}

	private class X11Application implements Runnable {

		private Thread thread;
		private volatile boolean running = true;

		public Event event;
		public boolean leave_display_open;
		public Window window;
		public Display display;
		public ZPixmap zpixmap;
		private boolean exposed = false;
		protected Option option;
		private String[] args;
		private int width;
		private int height;
		private boolean dragEnabled;

		public void start() {

			option = new Option(args);
			String env = gnu.util.Environment.getValue("DISPLAY");
			Display.Name display_name = option.display_name("display", "X server to connect to", new Display.Name(env));

			int send_mode = option.enum("send-mode", "request sending mode", Connection.SEND_MODE_STRINGS,
					Connection.ASYNCHRONOUS);

			display = new Display(display_name);
			display.connection.send_mode = send_mode;
			leave_display_open = true;
			zpixmap = new ZPixmap(display, width, height, display.default_pixmap_format);

			Window.Attributes win_attr = new Window.Attributes();
			win_attr.set_background(display.default_white);
			win_attr.set_border(display.default_black);
			win_attr.set_event_mask(Event.BUTTON_PRESS_MASK | Event.BUTTON_RELEASE_MASK | Event.EXPOSURE_MASK
					| Event.KEY_PRESS_MASK | Event.KEY_RELEASE_MASK | Event.POINTER_MOTION_MASK);
			window = new Window(display.default_root, 10, 10, width, height, 5, win_attr);

			window.set_wm(this, "main");
			window.set_wm_delete_window();
			window.map();

			// Wait while the window is not shown
			while (!exposed) {
				dispatch_event();
			}

			// Start event thread
			thread = new Thread(this);
			thread.start();
		}

		public void stop() {
			running = false;
		}

		public X11Application(String[] args, int width, int height) {
			this.args = args;
			this.width = width;
			this.height = height;
		}
		
		public void setPixels(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
			for (int y = startX; y < startY + h; y++) {
				for (int x = startX; x < startX + w; x++) {
					zpixmap.set(x, y, rgbArray[offset + y * scansize + x]);
				}
			}
		}

		public void paint() {
			window.put_image(display.default_gc, zpixmap, 0, 0);
		}

		public void run() {

			while (running)
				dispatch_event();

			if (!leave_display_open)
				display.close();
		}

		public void dispatch_event() {
			event = display.next_event();

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] X11Backend.dispatch_event(): " + event);

			switch (event.code()) {
			case ClientMessage.CODE:
				//if (((ClientMessage) event).delete_window ())
				break;
			case Expose.CODE:
				exposed = true;
				paint();
				break;
			case KeyPress.CODE: {
				KeyPress e = (KeyPress) event;
				int keycode = e.detail();
				int keystate = e.state();
				int keysym = display.input.keycode_to_keysym(keycode, keystate);
				fireKeyEvent(keycode, keystate, (char) keysym, true);
			}
				break;
			case KeyRelease.CODE: {
				KeyRelease e = (KeyRelease) event;
				int keycode = e.detail();
				int keystate = e.state();
				int keysym = display.input.keycode_to_keysym(keycode, keystate);
				fireKeyEvent(keycode, keystate, (char) keysym, false);
			}
				break;
			case ButtonPress.CODE: {
				dragEnabled = true;
				ButtonPress e = (ButtonPress) event;
				firePointerPressureEvent(e.event_x(), e.event_y(), true);
			}
				break;
			case ButtonRelease.CODE: {
				dragEnabled = false;
				ButtonRelease e = (ButtonRelease) event;
				firePointerPressureEvent(e.event_x(), e.event_y(), false);
			}
				break;
			case MotionNotify.CODE: {
				if (dragEnabled) {
					MotionNotify e = (MotionNotify) event;
					firePointerDraggedEvent(e.event_x(), e.event_y());
				}
			}
				break;
			}

		}

		public void fireKeyEvent(int keycode, int keystate, char c, boolean pressed) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SWTBackend.processKeyEvent()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
			// Set event type (intParam1)
			nativeEvent.intParam1 = pressed ? EventConstants.PRESSED : EventConstants.RELEASED;
			// Set event key code (intParam2)
			int internalCode = X11EventMapper.mapToInternalEvent(keycode, c);
			if (internalCode != 0) {
				nativeEvent.intParam2 = internalCode;
			} else if ((c != Misc.SHIFT_L) && (c != Misc.SHIFT_R) && (c != Misc.ALT_L) && (c != Misc.ALT_R)) {
				nativeEvent.intParam2 = c;
			} else {
				return;
			}
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);

		}

		private void firePointerPressureEvent(int x, int y, boolean pressed) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SWTBackend.processPointerPressedEvent()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = pressed ? EventConstants.PRESSED : EventConstants.RELEASED; // Event type
			nativeEvent.intParam2 = x; // x
			nativeEvent.intParam3 = y; // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);

		}

		private void firePointerDraggedEvent(int x, int y) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SWTBackend.processPointerDraggedEvent()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
			nativeEvent.intParam2 = x; // x
			nativeEvent.intParam3 = y; // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);
		}

	}

}
