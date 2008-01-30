package org.thenesis.midpath.demo.m3g;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class TestMidlet extends MIDlet {
	private static TestMidlet instance;
	TestCanvas displayable = new TestCanvas();
	Timer iTimer = new Timer();

	/* Construct the midlet. */
	public TestMidlet() {
		this.instance = this;
	}

	/** * Main method. */
	public void startApp() {
		Display.getDisplay(this).setCurrent(displayable);
		iTimer.schedule(new MyTimerTask(), 0, 20);
		System.out.println("Midlet started");
	}

	/** * Handle pausing the MIDlet. */
	public void pauseApp() {

	}

	/** * Handle destroying the MIDlet. */
	public void destroyApp(boolean unconditional) {

	}

	/** * Quit the MIDlet. */

	public static void quitApp() {
		instance.destroyApp(true);
		instance.notifyDestroyed();
		instance = null;
		System.out.println("Midlet exited");
	}

	/** * Our timer task for providing animation. */
	class MyTimerTask extends TimerTask {
		public void run() {
			if (displayable != null) {
				displayable.repaint();
			}
		}
	}
}
