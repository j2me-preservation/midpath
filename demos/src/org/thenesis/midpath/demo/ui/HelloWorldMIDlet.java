/*
 * MIDPath - Copyright (C) 2006-2008 Guillaume Legris, Mathieu Legris
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
package org.thenesis.midpath.demo.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class HelloWorldMIDlet extends MIDlet {

	private Display display;

	//@Override
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	//@Override
	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	//@Override
	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		
		Canvas canvas = new Canvas() {
			public void paint(Graphics g) {
				g.setColor(0, 0, 0);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(0xFF, 0xFF, 0xFF);
				g.drawString("Hello World !", getWidth() / 2, getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
			}
		};
		
		display.setCurrent(canvas);
	}
	
}
