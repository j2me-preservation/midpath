package org.thenesis.midpath.ui.backend.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
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

public class SWTBackend implements UIBackend {

	private VirtualSurface rootVirtualSurface;

	private SWTEventMapper eventMapper = new SWTEventMapper();
	private ImageData imageData;
	private Image image;
	private GC gc;
	private Runnable painterRunnable;
	private Display display;
	private Shell shell;

	public SWTBackend(int w, int h) {

		rootVirtualSurface = new VirtualSurfaceImpl(w, h);

		SWTThread swtThread = new SWTThread();
		swtThread.start();

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

		//System.out.println(x + " " + y + " " + width + " " + height);

		try {
			while (imageData == null) {
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			// Do nothing
		}
		
		int w = rootVirtualSurface.width;
		int h = rootVirtualSurface.height;
		for (int j = 0; j < height; j++) {
			imageData.setPixels(x, j, w, rootVirtualSurface.data, w * j + x);
		}

//		for (int j = 0; j < height; j++) {
//			imageData.setPixels(x, j, (int) width, rootVirtualSurface.data, rootVirtualSurface.width * j + x);
//		}

		if (display != null) {
			display.syncExec(painterRunnable);
		}

	}

	private class VirtualSurfaceImpl extends VirtualSurface {

		public VirtualSurfaceImpl(int w, int h) {
			data = new int[w * h];
			this.width = w;
			this.height = h;
		}

	}

	private class SWTThread extends Thread implements KeyListener {

		public void keyPressed(KeyEvent e) {

						//if (Logging.TRACE_ENABLED)
							System.out.println("[DEBUG] SWTBackend.keyPressed(): key code: " + e.keyCode + " char: "
									+ e.character);
			
						char c = e.character;
			
						NativeEvent nativeEvent = new NativeEvent(EventTypes.KEY_EVENT);
						// Set event type (intParam1)
						nativeEvent.intParam1 = EventConstants.PRESSED;
						// Set event key code (intParam2)
						int internalCode = SWTEventMapper.mapToInternalEvent(e.keyCode, c);
						if (internalCode != 0) {
							nativeEvent.intParam2 = internalCode;
						} //else if ((c != KeyEvent.CHAR_UNDEFINED) && (e.keyCode != KeyEvent.VK_SHIFT)) {
						//	nativeEvent.intParam2 = c;
						 else {
							return;
						}
						// Set event source (intParam4). Fake display with id=1
						nativeEvent.intParam4 = 1;
			
						EventQueue.getEventQueue().post(nativeEvent);

		}

		public void keyReleased(KeyEvent e) {

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
			//			int internalCode = SWTEventMapper.mapToInternalEvent(e.getKeyCode(), c);
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
		}

		public void keyTyped(KeyEvent e) {
			// Not used
		}

		public void run() {

			int w = rootVirtualSurface.width;
			int h = rootVirtualSurface.height;

			display = new Display();
			shell = new Shell(display);
			shell.setText("");
			shell.forceFocus();

			Canvas canvas = new Canvas(shell, SWT.NONE);
			PaletteData palette = new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF);
			imageData = new ImageData(w, h, 32, palette);
			gc = new GC(shell);

			painterRunnable = new Runnable() {
				public void run() {
					image = new Image(display, imageData);
					gc.drawImage(image, 0, 0);
					image.dispose();
				}
			};

			shell.addKeyListener(this);

			shell.open();
			shell.setSize(w, h);

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		}

	}

}
