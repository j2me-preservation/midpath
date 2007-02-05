package org.thenesis.midpath.ui.backend.awt;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
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

public class AWTBackend implements UIBackend {

	private VirtualSurface rootVirtualSurface;
	private Frame frame;
	private BufferedImage screenImage;

	public AWTBackend(int w, int h) {

		rootVirtualSurface = new VirtualSurfaceImpl(w, h);

		frame = new Frame() {

			public void update(Graphics g) {
				paint(g);
			}

			public void paint(Graphics g) {
				g.drawImage(screenImage, getInsets().left, getInsets().top, null);
			}

		};

		AWTEventConverter listener = new AWTEventConverter();
		frame.addKeyListener(listener);

		Insets insets = frame.getInsets();
		frame.setSize(w + insets.left + insets.right, h + insets.top + insets.bottom);
		frame.setVisible(true);
	}

	public EventMapper getEventMapper() {
		//return toolkit.getEventMapper();
		return null;
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
		frame.repaint();

		//SDLSurface sdlSurface = ((SDLGraphics)toolkit.getRootGraphics()).getSurface();

		// Draw rgb field on the surface
		//		sdlSurface.setPixelData32(rootVirtualSurface.data);
		//		try {
		//			sdlSurface.updateRect(x, y, width, heigth);
		//		} catch (SDLException e) {
		//			e.printStackTrace();
		//		}

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
				
			//System.out.println(KeyEvent.VK_DOWN);

			char c = e.getKeyChar();
			
			NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
			// Set event type (intParam1)
			nativeEvent.intParam1 = EventConstants.PRESSED;
			// Set event key code (intParam2)
			if (c != KeyEvent.VK_UNDEFINED) {
				nativeEvent.intParam2 = c;
			} else {
				nativeEvent.intParam2 = AWTEventMapper.mapToInternalEvent(e.getKeyCode());
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
			if (c != KeyEvent.VK_UNDEFINED) {
				nativeEvent.intParam2 = c;
			} else {
				nativeEvent.intParam2 = AWTEventMapper.mapToInternalEvent(e.getKeyCode());
			}
			// Set event source (intParam4). Fake display with id=1
			nativeEvent.intParam4 = 1;

			EventQueue.getEventQueue().post(nativeEvent);
		}

		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

	}

}
