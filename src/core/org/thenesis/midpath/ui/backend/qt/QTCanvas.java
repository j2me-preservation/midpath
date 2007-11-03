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
package org.thenesis.midpath.ui.backend.qt;

import com.sun.cldchi.jvm.JVM;

public class QTCanvas {

	public static final int PRESSED = 1;

	static {
		JVM.loadLibrary("libmidpathqt.so");
		//System.loadLibrary("midpathqt");
	}

	private int width;
	private int height;

	public QTCanvas(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void open() {

		Runnable runnable = new Runnable() {
			public void run() {

				// Initialize QT.
				if (initialize(width, height)) {
					// Start main loop and block
					startMainLoop();
				} else {
					System.err.println("Can't initialize QTCanvas");
				}

			}
		};

		new Thread(runnable).start();

	}

	public void close() {
		quit();
	}

	public void updateARGBPixels(final int[] argbBuffer, final int x_src, final int y_src, final int width,
			final int height) {
		writeARGB(argbBuffer, x_src, y_src, width, height);
	}

	/* Event callback methods. Inherited classes should override them. */

	public void onKeyEvent(int state, int keyCode, int unicode) {
		System.out.println("key: ");
		if (state == PRESSED) {
			System.out.println("key pressed: " + keyCode + " " + ((char) unicode));
		} else {
			System.out.println("key released: " + keyCode + " " + ((char) unicode));
		}
	}

	public void onMouseMoveEvent(int x, int y) {
		System.out.println("motion event: " + x + " " + y);
	}

	public void onMouseButtonEvent(int x, int y, int state) {
		if (state == PRESSED) {
			System.out.println("button pressed: " + x + " " + y);
		} else {
			System.out.println("button released: " + x + " " + y);
		}
	}

	public void onCloseEvent() {
		System.out.println("Window delete event received: ");
	}

	/* Native methods */

	native private boolean initialize(int width, int height);

	native private void writeARGB(int[] argbBuffer, int x_src, int y_src, int width, int height);

	native private int startMainLoop();

	native private int quit();

}
