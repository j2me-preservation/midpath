package org.thenesis.midpath.ui.backend.swt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import org.thenesis.midpath.ui.virtual.UIBackend;
import org.thenesis.midpath.ui.virtual.VirtualSurface;

import com.sun.midp.events.EventMapper;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;

public class SWTBackend implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private Panel panel;
	private Frame frame;
	private BufferedImage screenImage;
	private SWTEventMapper eventMapper = new SWTEventMapper();

	public SWTBackend(int w, int h) {

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
		frame.addKeyListener(listener);
		frame.add(panel);
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

	private class VirtualSurfaceImpl extends VirtualSurface {

		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}

	}

	private class AWTEventConverter implements KeyListener {

		public void keyPressed(KeyEvent e) {

			if (Logging.TRACE_ENABLED)
				System.out.println("[DEBUG] AWTBackend.keyPressed(): key code: " + e.getKeyCode() + " char: "
						+ e.getKeyChar());

			char c = e.getKeyChar();

			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
			// Set event type (intParam1)
			nativeEvent.intParam1 = EventConstants.PRESSED;
			// Set event key code (intParam2)
			int internalCode = SWTEventMapper.mapToInternalEvent(e.getKeyCode(), c);
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
			int internalCode = SWTEventMapper.mapToInternalEvent(e.getKeyCode(), c);
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

	}

}
