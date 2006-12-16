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
package org.thenesis.midpath.ui;

import sdljava.event.SDLEvent;
import sdljava.event.SDLExposeEvent;
import sdljava.event.SDLKeyboardEvent;
import sdljava.event.SDLMouseButtonEvent;
import sdljava.event.SDLMouseMotionEvent;
import sdljava.event.SDLQuitEvent;
import sdljava.x.swig.SDLPressedState;

import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;

/**
 * Fetches events from SDL, translates them to MIDP events and pumps them up
 * into the MIDP UI event queue.
 *
 * @author Guillaume Legris
 * @author Mathieu Legris
 */
public class SDLEventPump implements Runnable {

	/**
	 * Indicates if we are currently inside a drag operation. This is
	 * set to the button ID when a button is pressed and to -1 (indicating
	 * that no drag is active) when the mouse is released.
	 */
	private int drag;

	private boolean running = true;

	/**
	 * Creates a new SDLEventPump for the specified X Display.
	 */
	SDLEventPump() {
		drag = -1;
		Thread t = new Thread(this);
		t.start();
	}

	/**
	 * The main event pump loop. Events are pumped into the system event queue.
	 */
	public void run() {

		try {
			
			SDLEvent.enableUNICODE(1);

			while (running) {
				processEvent(SDLEvent.waitEvent(true));
			}

		} catch (Throwable x) {
			System.err.println("Exception during event dispatch:");
			x.printStackTrace(System.err);
		}

	}

	public void processEvent(SDLEvent event) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SDLEventPump.processEvent()");

		if (event instanceof SDLMouseButtonEvent)
			processEvent((SDLMouseButtonEvent) event);
		else if (event instanceof SDLMouseMotionEvent)
			processEvent((SDLMouseMotionEvent) event);
		else if (event instanceof SDLQuitEvent)
			processEvent((SDLQuitEvent) event);
		else if (event instanceof SDLExposeEvent)
			processEvent((SDLExposeEvent) event);
		else if (event instanceof SDLKeyboardEvent)
			processEvent((SDLKeyboardEvent) event);
	}

	public void processEvent(SDLMouseButtonEvent event) {
		
		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);

		int sdlButton = event.getButton();
		drag = sdlButton;

		if (event.getState() == SDLPressedState.PRESSED) {
			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SDLEventPump.processEvent(): MOUSE_PRESSED");
			nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
		} else {
			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SDLEventPump.processEvent(): MOUSE_RELEASED");
			drag = -1;
			nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
		}
		
		nativeEvent.intParam2 = event.getX(); // x
		nativeEvent.intParam3 = event.getY(); // y
		
		EventQueue.getEventQueue().post(nativeEvent);
		
	}

	public void processEvent(SDLMouseMotionEvent event) {
		
		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		
		if (drag != -1) {
			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
			nativeEvent.intParam2 = event.getX(); // x
			nativeEvent.intParam3 = event.getY(); // y
		}
		
		EventQueue.getEventQueue().post(nativeEvent);

	}

	public void processEvent(SDLKeyboardEvent event) {

		int unicode = event.getUnicode();
		int keyCode = event.getSym();
		
		// If the key code is visible, check if we need upper case
		if (unicode != 0) {
			keyCode = unicode;
			if (event.getMod().shift()) {
				if (Character.isLetter(unicode))
					keyCode = Character.toUpperCase(unicode);
			}
			
		} else {
			// Skip the key code if it's not a known code
			// If it's not an internal event key, test if it's a system key
			int code = SDLEventMapper.mapToInternalEvent(keyCode);
			if (code == keyCode) {
				if (SDLToolkit.getToolkit().getEventMapper().getSystemKey(keyCode) == 0) 
					return;
			}
		}
		
		NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		
		// Set event type (intParam1)
		if (event.getState() == SDLPressedState.PRESSED) {
			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] SDLEventPump.processEvent(): key sym: " + event.getSym() + " unicode: " + unicode + " char: " + new String(Character.toChars(unicode)));
			nativeEvent.intParam1 = EventConstants.PRESSED;
		} else if (event.getState() == SDLPressedState.RELEASED) {
			nativeEvent.intParam1 = EventConstants.RELEASED;
		}
		
		// Set event key code (intParam2)
		nativeEvent.intParam2 = SDLEventMapper.mapToInternalEvent(keyCode);
		
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;
		
		EventQueue.getEventQueue().post(nativeEvent);
		
//		// TODO Fake only one available window yet
//		Integer windowKey = new Integer(0);
//		Window awtWindow = (Window) windows.get(windowKey);
//
//		long when = System.currentTimeMillis();
//		int keyCode = EventMapping.mapToKeyCode(event);
//		int modifiers = EventMapping.mapModifiers(event);
//		char keyChar = EventMapping.mapToKeyChar(event);
//		KeyEvent ke;
//		
//		//if (SDLToolkit.DEBUG)
//		//	System.out.println("[DEBUG] SDLEventPump.processEvent(SDLKeyboardEvent event): key : " + keyChar);
//
//		if (event.getState() == SDLPressedState.PRESSED) {
//			System.out.println("[DEBUG] SDLEventPump.processEvent(SDLKeyboardEvent event): key pressed : code : " + keyCode);
//			//ke = new KeyEvent(awtWindow, KeyEvent.KEY_PRESSED, when, modifiers, keyCode, keyChar);
//			ke = new KeyEvent(awtWindow, KeyEvent.KEY_PRESSED, when, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
//			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
//			if (keyChar != KeyEvent.CHAR_UNDEFINED) {
//				System.out.println("[DEBUG] SDLEventPump.processEvent(SDLKeyboardEvent event): key typed: " + keyChar);
//				ke = new KeyEvent(awtWindow, KeyEvent.KEY_TYPED, when, modifiers, KeyEvent.VK_UNDEFINED, keyChar);
//				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
//			}
//		} else {
//			System.out.println("[DEBUG] SDLEventPump.processEvent(SDLKeyboardEvent event): key released: code :" + keyCode);
//			ke = new KeyEvent(awtWindow, KeyEvent.KEY_RELEASED, when, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
//			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
//		}

	}

	public void processEvent(SDLQuitEvent event) {
		//System.exit(0);
	}

	public void processEvent(SDLExposeEvent event) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SDLEventPump.processEvent(SDLExposeEvent event): NOT IMPLEMENTED");
	}


	
}
