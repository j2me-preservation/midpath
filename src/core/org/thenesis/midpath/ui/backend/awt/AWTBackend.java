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
package org.thenesis.midpath.ui.backend.awt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;

public class AWTBackend implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private Panel panel;
	private Frame frame;
	protected BufferedImage screenImage;
	private AWTEventMapper eventMapper = new AWTEventMapper();

	public AWTBackend(int w, int h) {

		rootVirtualSurface = new VirtualSurfaceImpl(w, h);

		final Dimension dimension = new Dimension(w, h);
		panel = new Panel() {

			public void update(Graphics g) {
				paint(g);
			}

			public Dimension getMinimumSize() {
				return dimension;
			}

			public Dimension getPreferredSize() {
				return dimension;
			}

			public void paint(Graphics g) {
				if (screenImage != null) {
					g.drawImage(screenImage, 0, 0, null);
				}
			}
		};

		AWTEventConverter listener = new AWTEventConverter();

		frame = new Frame();
		frame.addWindowListener(listener);
		panel.addKeyListener(listener);
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);
		frame.add(panel);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
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

	public void updateSurfacePixels(int x, int y, long width, long heigth) {

		int w = rootVirtualSurface.width;
		int h = rootVirtualSurface.height;

		if (screenImage == null) {
			screenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}

		screenImage.setRGB(0, 0, w, h, rootVirtualSurface.data, 0, w);
		panel.repaint();

	}
	
	public void close() {
		frame.dispose();
	}

	private class VirtualSurfaceImpl extends VirtualSurface {

		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}

	}

	private class AWTEventConverter implements KeyListener, MouseListener, MouseMotionListener, WindowListener {

		public void keyPressed(KeyEvent e) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] AWTBackend.keyPressed(): key code: " + e.getKeyCode() + " char: "
						+ e.getKeyChar());

			char c = e.getKeyChar();

			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
			// Set event type (intParam1)
			nativeEvent.intParam1 = EventConstants.PRESSED;
			// Set event key code (intParam2)
			int internalCode = AWTEventMapper.mapToInternalEvent(e.getKeyCode(), c);
			if (internalCode != 0) {
				nativeEvent.intParam2 = internalCode;
			} else if ((c != KeyEvent.CHAR_UNDEFINED) && (e.getKeyCode() != KeyEvent.VK_SHIFT)) {
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
				System.out.println("[DEBUG] AWTBackend.keyReleased(): key code: " + e.getKeyCode() + " char: "
						+ e.getKeyChar());

			char c = e.getKeyChar();

			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
			// Set event type (intParam1)
			nativeEvent.intParam1 = EventConstants.RELEASED;
			// Set event key code (intParam2)
			int internalCode = AWTEventMapper.mapToInternalEvent(e.getKeyCode(), c);
			if (internalCode != 0) {
				nativeEvent.intParam2 = internalCode;
			} else if ((c != KeyEvent.CHAR_UNDEFINED) && (e.getKeyCode() != KeyEvent.VK_SHIFT)) {
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

		public void mouseClicked(MouseEvent e) {
			// Not used
		}

		public void mouseEntered(MouseEvent e) {
			// Not used
		}

		public void mouseExited(MouseEvent e) {
			// Not used
		}

		public void mousePressed(MouseEvent e) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] AWTBackend.mousePressed()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.PRESSED; // Event type
			nativeEvent.intParam2 = e.getX(); // x
			nativeEvent.intParam3 = e.getY(); // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);

		}

		public void mouseReleased(MouseEvent e) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] AWTBackend.mouseReleased()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.RELEASED; // Event type
			nativeEvent.intParam2 = e.getX(); // x
			nativeEvent.intParam3 = e.getY(); // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);

		}

		public void mouseDragged(MouseEvent e) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] AWTBackend.mouseDragged()");

			NativeEvent nativeEvent = new NativeEvent(EventTypes.PEN_EVENT);
			nativeEvent.intParam1 = EventConstants.DRAGGED; // Event type
			nativeEvent.intParam2 = e.getX(); // x
			nativeEvent.intParam3 = e.getY(); // y
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);

		}

		public void mouseMoved(MouseEvent e) { 
			/* Not used */
		}

		public void windowClosing(WindowEvent e) {
			
			// User attempts to close the window from the window's system menu.
			// Send shutdown event
			NativeEvent nativeEvent = new NativeEvent(EventTypes.SHUTDOWN_EVENT);
			EventQueue.getEventQueue().post(nativeEvent);
			
			// MIDletStateHandler.getMidletStateHandler().destroySuite();
			// NativeEvent nativeEvent = new NativeEvent(EventTypes.DESTROY_MIDLET_EVENT);
			// Set event source (intParam4). Fake display with id=1
			// nativeEvent.intParam4 = 1;
		}

		public void windowClosed(WindowEvent e) {
		}

		public void windowActivated(WindowEvent arg0) {
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowOpened(WindowEvent arg0) {
		}

	}

}
