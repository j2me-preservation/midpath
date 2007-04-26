package org.thenesis.midpath;

import javax.microedition.lcdui.DisplayEventHandlerImpl;

import com.sun.midp.lcdui.DisplayEventHandlerFactory;
import com.sun.midp.main.MIDletSuiteLoader;

public class MIDletLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length  < 1) {
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
			MIDletLauncher.launch(className, midletName);
		} catch (Exception e) {
			System.err.println("An error occured while trying to start the midlet:");
			e.printStackTrace();
		} 
		
	}
	
	public static void launch(String className, String midletName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(new DisplayEventHandlerImpl());
		MIDletSuiteLoader.init(className, midletName);
	}

}
