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

import com.sun.cldchi.jvm.JVM;

public class FBCanvas {

	private static final int BUTTON_MASK = 0x4;
	private static final int LEFT_BUTTON_MASK = 0x1;
	private static final int RIGHT_BUTTON_MASK = 0x2;

	public static final int FB_K_SHIFT = 1, FB_K_CTRL = 2, FB_K_ALT = 3, FB_K_HOME = 4, FB_K_INSERT = 5,
			FB_K_DELETE = 6, FB_K_END = 7, FB_K_PRIOR = 8, FB_K_NEXT = 9, FB_K_RETURN = 10, FB_K_F1 = 11, FB_K_F2 = 12,
			FB_K_F3 = 13, FB_K_F4 = 14, FB_K_F5 = 15, FB_K_F6 = 16, FB_K_F7 = 17, FB_K_F8 = 18, FB_K_F9 = 19,
			FB_K_F10 = 20, FB_K_F11 = 21, FB_K_F12 = 22, FB_K_DOWN = 23, FB_K_LEFT = 24, FB_K_RIGHT = 25, FB_K_UP = 26,
			FB_K_ENTER = 27;

	private String keyboardDeviceName;
	private String fbDeviceName;
	private String mouseDeviceName;
	private String touchscreenDeviceName;

	private int[] copiedARGBBuffer;

	private static final int MOUSE_WIDTH = 5;
	private static final int MOUSE_HEIGHT = 5;
	int[] mouseImage = { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFFF0000,
			0xFFFF0000, 0xFFFF0000, 0xFF000000, 0xFF000000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFF000000, 0xFF000000,
			0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 };
	private int mouseX, mouseY, mouseButton;

	static {
		JVM.loadLibrary("libmidpathfb.so");
		//System.loadLibrary("midpathfb");
	}

	private int width;
	private int height;
	private PollEventThread eventThread;

	public FBCanvas(String keyboardDeviceName, String mouseDeviceName, String touchscreenDeviceName,
			String fbDeviceName, int width, int height) {
		this.keyboardDeviceName = keyboardDeviceName;
		this.fbDeviceName = fbDeviceName;
		this.mouseDeviceName = mouseDeviceName;
		this.touchscreenDeviceName = touchscreenDeviceName;
		this.width = width;
		this.height = height;
	}

	public void open() {

		copiedARGBBuffer = new int[width * height];

		// Initialize the framebuffer.
		if (!initialize(keyboardDeviceName, mouseDeviceName, touchscreenDeviceName, fbDeviceName, width, height)) {
			System.err.println("Can't open the framebuffer");
			return;
		}

		eventThread = new PollEventThread();
		eventThread.start();

	}

	public void close() {
		eventThread.stop();
	}

	public synchronized void updateARGBPixels(final int[] argbBuffer, final int x, final int y, final int w, final int h) {
		System.arraycopy(argbBuffer, 0, copiedARGBBuffer, 0, copiedARGBBuffer.length);
		drawARGB(argbBuffer, y * width + x, width, x, y, w, h);
		drawARGB(mouseImage, 0, MOUSE_WIDTH, mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);
	}

	/* Event callback methods. Inherited classes should override them. */

	public void onKeyEvent(boolean pressed, int keyCode, char c) {
		if (pressed) {
			System.out.println("key pressed: " + keyCode + " " + c);
		} else {
			System.out.println("key released: " + keyCode + " " + c);
		}
	}

	public synchronized void onRawMouseEvent(int dx, int dy, int button) {

		//System.out.println("raw mouse event: dx=" + dx + " dy=" + dy + " button=" + button);

		// Redraw the part of screen which was hidden by the mouse
		drawARGB(copiedARGBBuffer, mouseY * width + mouseX, width, mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);

		boolean moved = false;
		if (dx != 0) {
			moved = true;
			mouseX += dx;
			if (mouseX < 0)
				mouseX = 0;
			if (mouseX > width)
				mouseX = width;
		}
		if (dy != 0) {
			moved = true;
			mouseY += dy;
			if (mouseY < 0)
				mouseY = 0;
			if (mouseY > height)
				mouseY = height;
		}

		if (button != mouseButton) {
			onMouseButtonEvent(mouseX, mouseY, button);
		}
		if (moved) {
			onMouseMoveEvent(mouseX, mouseY);
		}

		// Draw the mouse
		drawARGB(mouseImage, 0, MOUSE_WIDTH, mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);

	}

	public void onMouseMoveEvent(int x, int y) {
		System.out.println("motion event 2: " + x + " " + y);
	}

	public void onMouseButtonEvent(int x, int y, int button) {
		if (((button & BUTTON_MASK) == BUTTON_MASK) || ((button & LEFT_BUTTON_MASK) == LEFT_BUTTON_MASK)) {
			System.out.println("Left button pressed: " + x + " " + y);
		}
		if ((button & RIGHT_BUTTON_MASK) == RIGHT_BUTTON_MASK) {
			System.out.println("Right button pressed: " + x + " " + y);
		}
		if (button == 0) {
			System.out.println("button released: " + x + " " + y);
		}
	}

	public synchronized void onRawTouchscreenEvent(int rawX, int rawY, int button) {

		//System.out.println("raw touchscreen event: rawX=" + x + " rawY=" + y + " button=" + button);

		// Redraw the part of screen which was hidden by the mouse
		drawARGB(copiedARGBBuffer, mouseY * width + mouseX, width, mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);

		mouseX = rawX;
		mouseY = rawY;
		onMouseButtonEvent(mouseX, mouseY, button);

		// Draw the mouse
		drawARGB(mouseImage, 0, MOUSE_WIDTH, mouseX, mouseY, MOUSE_WIDTH, MOUSE_HEIGHT);

	}

	public void onTouchscreenEvent(int x, int y, int button) {
		if ((button & BUTTON_MASK) == BUTTON_MASK) {
			System.out.println("Touchscreen pressed: " + x + " " + y);
		} else if (button == 0) {
			System.out.println("Touchscreen released: " + x + " " + y);
		}
	}

	//	public void onCloseEvent() {
	//		System.out.println("Window delete event received: ");
	//	}

	/* Native methods */

	native private boolean initialize(String keyboardDeviceName, String mouseDeviceName, String touchscreenDeviceName,
			String fbDeviceName, int width, int height);

	/** 
	 * Renders a series of device-independent ARGB values in a specified region
	 * @param rgbData an array of ARGB values in the format
	 * <code>0xAARRGGBB</code>
	 * @param offset the array index of the first ARGB value
	 * @param scanlength the relative array offset between the
	 * corresponding pixels in consecutive rows in the
	 * <code>rgbData</code> array
	 * @param x the horizontal location of the region to be rendered
	 * @param y the vertical location of the region to be rendered
	 * @param width the width of the region to be rendered
	 * @param height the height of the region to be rendered
	 * @param processAlpha <code>true</code> if <code>rgbData</code>
	 * has an alpha channel, false if all pixels are fully opaque
	 */
	native private void drawARGB(int[] argbBuffer, int offset, int scanlength, int x, int y, int width, int height);

	native private void eventLoop();

	native private int quit();

	/**
	 * An event thread which polls events from the native layer 
	 */
	private class PollEventThread implements Runnable {

		private Thread thread;

		public void start() {
			thread = new Thread(PollEventThread.this);
			thread.start();
		}

		public void stop() {
			// Quit the event loop
			quit();

			// Wait a bit that the event thread is stopped
			try {
				while (thread.isAlive()) {
					Thread.sleep(1);
				}
			} catch (InterruptedException e) {
			}
		}

		public void run() {
			eventLoop();
		}
	}

}
