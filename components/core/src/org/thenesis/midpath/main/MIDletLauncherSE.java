package org.thenesis.midpath.main;

import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;

public class MIDletLauncherSE {

	public static void main(String[] args) {
		
		// Load system properties required by MIDP2 and JSR specs
		MIDletLauncherSE.callSystemPropertiesLoader();
		
		// Launch the MIDlet
		MIDletLauncher.main(args);

	}
	
	static void callSystemPropertiesLoader()  {
		
		try {
			Class clazz = Class.forName("org.thenesis.midpath.main.SystemPropertiesLoader");
			clazz.newInstance();
		} catch (Exception e) {
			if (Logging.REPORT_LEVEL <= Logging.WARNING) {
				Logging.report(Logging.WARNING, LogChannels.LC_CORE, "MIDletLauncherSE: System properties can't be loaded");
			}
			e.printStackTrace();
		} 
		
	}

}
