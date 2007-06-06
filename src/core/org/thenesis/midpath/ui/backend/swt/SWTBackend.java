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
package org.thenesis.midpath.ui.backend.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;

public class SWTBackend implements UIBackend, Runnable, KeyListener, MouseListener, MouseMoveListener, DisposeListener {

	private VirtualSurface rootVirtualSurface;
	private SWTEventMapper eventMapper = new SWTEventMapper();

	private ImageData imageData;
	private Image image;
	private GC gc;
	private Runnable painterRunnable;
	private Display display;
	private Shell shell;
	private Thread swtThread;
	private boolean dragEnabled = false;

	private volatile boolean initialized = false;

	public SWTBackend(int w, int h) {
		rootVirtualSurface = new VirtualSurfaceImpl(w, h);
		start();
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

		//System.out.println("[DEBUG] SWTBackend.updateSurfacePixels(): " + x + " " + y + " " + width + " " + height);	

		int w = rootVirtualSurface.width;
		int h = rootVirtualSurface.height;

		for (int j = 0; j < h; j++) {
			imageData.setPixels(0, j, w, rootVirtualSurface.data, w * j);
		}

		//		for (int j = 0; j < height; j++) {
		//			imageData.setPixels(x, j, (int) width, rootVirtualSurface.data, rootVirtualSurface.width * j + x);
		//		}

		if (display != null) {
			display.syncExec(painterRunnable);
		}

	}
	
	public void close() {
		stop();
	}
	

	private class VirtualSurfaceImpl extends VirtualSurface {

		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}

	}
	
	/*
	 * SWT thread
	 */

	private void start() {

		swtThread = new Thread(this);
		swtThread.start();

		// FIXME Ugly
		try {
			while (!initialized) {
				Thread.sleep(1);
			}
		} catch (InterruptedException e) {
			// Do nothing
		}

	}
	
	private void stop() {
		shell.dispose();
	}

	public void run() {

		int w = rootVirtualSurface.width;
		int h = rootVirtualSurface.height;

		display = new Display();
		shell = new Shell(display);
		shell.setText("");

		Canvas canvas = new Canvas(shell, SWT.NONE);
		canvas.setSize(w, h);
		PaletteData palette = new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF);
		imageData = new ImageData(w, h, 32, palette);
		gc = new GC(canvas);

		painterRunnable = new Runnable() {

			public void run() {
				image = new Image(display, imageData);
				gc.drawImage(image, 0, 0);
				image.dispose();
			}
		};

		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addDisposeListener(this);
		canvas.forceFocus();
		shell.pack();
		shell.open();

		initialized = true;

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

	public void keyPressed(KeyEvent e) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SWTBackend.keyPressed(): key code: " + e.keyCode + " char: " + e.character);

		char c = e.character;

		NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		// Set event type (intParam1)
		nativeEvent.intParam1 = EventConstants.PRESSED;
		// Set event key code (intParam2)
		int internalCode = SWTEventMapper.mapToInternalEvent(e.keyCode, c);
		if (internalCode != 0) {
			nativeEvent.intParam2 = internalCode;
		} else if ((e.keyCode != SWT.SHIFT) && (e.keyCode != SWT.CONTROL)) {
			nativeEvent.intParam2 = c;
		} else {
			return;
		}
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);

	}

	public void keyReleased(KeyEvent e) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SWTBackend.keyReleased(): key code: " + e.keyCode + " char: " + e.character);

		char c = e.character;

		NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
		// Set event type (intParam1)
		nativeEvent.intParam1 = EventConstants.RELEASED;
		// Set event key code (intParam2)
		int internalCode = SWTEventMapper.mapToInternalEvent(e.keyCode, c);
		if (internalCode != 0) {
			nativeEvent.intParam2 = internalCode;
		} else if ((e.keyCode != SWT.SHIFT) && (e.keyCode != SWT.CONTROL)) {
			nativeEvent.intParam2 = c;
		} else {
			return;
		}
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);
	}

	public void keyTyped(KeyEvent e) {
		// Not used
	}

	public void mouseDoubleClick(MouseEvent arg0) {
		// Not used
	}

	public void mouseDown(MouseEvent e) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SWTBackend.mouseDown()");

		dragEnabled = true;

		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
		nativeEvent.intParam2 = e.x; // x
		nativeEvent.intParam3 = e.y; // y
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);

	}

	public void mouseUp(MouseEvent e) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SWTBackend.mouseUp()");

		dragEnabled = false;

		NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
		nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
		nativeEvent.intParam2 = e.x; // x
		nativeEvent.intParam3 = e.y; // y
		// Set event source (intParam4). Fake display with id=1
		nativeEvent.intParam4 = 1;

		EventQueue.getEventQueue().post(nativeEvent);

	}

	public void mouseMove(MouseEvent e) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SWTBackend.mouseDragged(): " + dragEnabled);

		if (dragEnabled) {
			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
			nativeEvent.intParam2 = e.x; // x
			nativeEvent.intParam3 = e.y; // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);
		}

	}

	public void widgetDisposed(DisposeEvent e) {
		NativeEvent nativeEvent = new NativeEvent(EventTypes.SHUTDOWN_EVENT);
		EventQueue.getEventQueue().post(nativeEvent);
	}

}
