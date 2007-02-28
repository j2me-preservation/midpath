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

import gnu.x11.Connection;
import gnu.x11.Display;
import gnu.x11.Option;
import gnu.x11.Window;
import gnu.x11.event.ClientMessage;
import gnu.x11.event.Event;
import gnu.x11.event.Expose;
import gnu.x11.event.KeyPress;
import gnu.x11.image.ZPixmap;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;

public class X11Backend implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private X11Application x11App;
	private X11EventMapper eventMapper = new X11EventMapper();

	public X11Backend(int w, int h) {

		rootVirtualSurface = new VirtualSurfaceImpl(w, h);

		x11App = new X11Application(new String[] { "" }, w, h);
		x11App.start();

		//		frame = new Frame();
		//		panel.addKeyListener(listener);
		//		panel.addMouseListener(listener);
		//		panel.addMouseMotionListener(listener);
		//		frame.add(panel);
		//		frame.setResizable(false);
		//		frame.pack();
		//		frame.setVisible(true);
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

		//screenImage.setRGB(0, 0, w, h, rootVirtualSurface.data, 0, w);
		//panel.repaint();

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

		public void start() {
			
			option = new Option(args);
			String env = gnu.util.Environment.value("DISPLAY");
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
			win_attr.set_event_mask(Event.BUTTON_PRESS_MASK | Event.EXPOSURE_MASK | Event.KEY_PRESS_MASK);
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

		public void dispatch_event() {
			System.out.println("blocking-read event");
			event = display.next_event();
			System.out.println("got event " + event);

			switch (event.code()) {
			case gnu.x11.event.ButtonPress.CODE:

				break;

			case ClientMessage.CODE:
				//if (((ClientMessage) event).delete_window ())
				break;

			case Expose.CODE:
				exposed = true;
				break;

			case KeyPress.CODE: {
				KeyPress e = (KeyPress) event;

				int keycode = e.detail();
				int keystate = e.state();
				int keysym = display.input.keycode_to_keysym(keycode, keystate);

				//		      if (keysym == 'q' || keysym == 'Q' 
				//		        || keysym == gnu.x11.keysym.Misc.ESCAPE) exit ();
				break;

			}
			}
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

		//		public void keyPressed(KeyEvent e) {
		//
		//			if (Logging.TRACE_ENABLED)
		//				System.out.println("[DEBUG] AWTBackend.keyPressed(): key code: " + e.getKeyCode() + " char: "
		//						+ e.getKeyChar());
		//
		//			char c = e.getKeyChar();
		//
		//			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		//			// Set event type (intParam1)
		//			nativeEvent.intParam1 = EventConstants.PRESSED;
		//			// Set event key code (intParam2)
		//			int internalCode = X11EventMapper.mapToInternalEvent(e.getKeyCode(), c);
		//			if (internalCode != 0) {
		//				nativeEvent.intParam2 = internalCode;
		//			} else if ((c != KeyEvent.CHAR_UNDEFINED) && (e.getKeyCode() != KeyEvent.VK_SHIFT)) {
		//				nativeEvent.intParam2 = c;
		//			} else {
		//				return;
		//			}
		//			// Set event source (intParam4). Fake display with id=1
		//			nativeEvent.intParam4 = 1;
		//
		//			EventQueue.getEventQueue().post(nativeEvent);
		//
		//		}
		//
		//		public void keyReleased(KeyEvent e) {
		//
		//			if (Logging.TRACE_ENABLED)
		//				System.out.println("[DEBUG] AWTBackend.keyReleased(): key code: " + e.getKeyCode() + " char: "
		//						+ e.getKeyChar());
		//
		//			char c = e.getKeyChar();
		//
		//			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		//			// Set event type (intParam1)
		//			nativeEvent.intParam1 = EventConstants.RELEASED;
		//			// Set event key code (intParam2)
		//			int internalCode = X11EventMapper.mapToInternalEvent(e.getKeyCode(), c);
		//			if (internalCode != 0) {
		//				nativeEvent.intParam2 = internalCode;
		//			} else if ((c != KeyEvent.CHAR_UNDEFINED) && (e.getKeyCode() != KeyEvent.VK_SHIFT)) {
		//				nativeEvent.intParam2 = c;
		//			} else {
		//				return;
		//			}
		//			// Set event source (intParam4). Fake display with id=1
		//			nativeEvent.intParam4 = 1;
		//
		//			EventQueue.getEventQueue().post(nativeEvent);
		//		}
		//
		//		public void keyTyped(KeyEvent e) {
		//			// Not used
		//		}
		//
		//		public void mouseClicked(MouseEvent e) {
		//			// Not used
		//		}
		//
		//		public void mouseEntered(MouseEvent e) {
		//			// Not used
		//		}
		//
		//		public void mouseExited(MouseEvent e) {
		//			// Not used
		//
		//		}
		//
		//		public void mousePressed(MouseEvent e) {
		//
		//			if (Logging.TRACE_ENABLED)
		//				System.out.println("[DEBUG] AWTBackend.mousePressed()");
		//
		//			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		//			nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
		//			nativeEvent.intParam2 = e.getX(); // x
		//			nativeEvent.intParam3 = e.getY(); // y
		//			// Set event source (intParam4). Fake display with id=1
		//			nativeEvent.intParam4 = 1;
		//
		//			EventQueue.getEventQueue().post(nativeEvent);
		//
		//		}
		//
		//		public void mouseReleased(MouseEvent e) {
		//
		//			if (Logging.TRACE_ENABLED)
		//				System.out.println("[DEBUG] AWTBackend.mouseReleased()");
		//
		//			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		//			nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
		//			nativeEvent.intParam2 = e.getX(); // x
		//			nativeEvent.intParam3 = e.getY(); // y
		//			// Set event source (intParam4). Fake display with id=1
		//			nativeEvent.intParam4 = 1;
		//
		//			EventQueue.getEventQueue().post(nativeEvent);
		//
		//		}
		//
		//		public void mouseDragged(MouseEvent e) {
		//
		//			if (Logging.TRACE_ENABLED)
		//				System.out.println("[DEBUG] AWTBackend.mouseDragged()");
		//
		//			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		//			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
		//			nativeEvent.intParam2 = e.getX(); // x
		//			nativeEvent.intParam3 = e.getY(); // y
		//			// Set event source (intParam4). Fake display with id=1
		//			nativeEvent.intParam4 = 1;
		//
		//			EventQueue.getEventQueue().post(nativeEvent);
		//
		//		}
		//
		//		public void mouseMoved(MouseEvent e) {
		//			// Not used
		//		}

	}

}
