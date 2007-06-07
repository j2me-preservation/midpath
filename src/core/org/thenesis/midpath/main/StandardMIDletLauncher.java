package org.thenesis.midpath.main;

import javax.microedition.lcdui.UIToolkit;

import com.sun.midp.events.EventQueue;
import com.sun.midp.main.BaseMIDletSuiteLauncher;
import com.sun.midp.main.MIDletClassLoader;

public class StandardMIDletLauncher extends BaseMIDletSuiteLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage: java org.thenesis.midpath.MIDletLauncher <midlet-class-name> [midlet-name]");
			System.exit(1);
		}

		String className = args[0];
		String midletName;
		if (args.length == 2) {
			midletName = args[1];
		} else {
			midletName = className;
		}

		try {
			StandardMIDletLauncher launcher = new StandardMIDletLauncher();
			launcher.launchAndClose(className, midletName);
		} catch (Exception e) {
			System.err.println("An error occured while trying to start the midlet:");
			e.printStackTrace();
		}

	}

	public void launchAndClose(String className, String midletName) throws Exception {
		initialize();
		launch(className, midletName);
		close();
	}

	//	public void launch(String className, String midletName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	//		
	//		BaseMIDletSuiteLauncher.launch(className, midletName);
	//		
	//		// Clean and exit
	//		UIToolkit.getToolkit().close();
	//		EventQueue.getEventQueue().shutdown();
	//		System.exit(0);
	//	}

	void launch(String className, String midletName) throws Exception {

		MIDletClassLoader classLoader = new MIDletClassLoader() {
			public Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
				Class midletClass = Class.forName(className);
				if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
					throw new InstantiationException("Class not a MIDlet");
				}
				return midletClass;
			}
		};

		launch(classLoader, className, midletName);

	}

	void close() {
		// Clean and exit
		UIToolkit.getToolkit().close();
		EventQueue.getEventQueue().shutdown();
		System.exit(0);
	}

}
