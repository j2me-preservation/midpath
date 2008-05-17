package org.thenesis.midpath.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;

public class MIDletLauncherSE {

	public static void main(String[] args) {
		
		// Load system properties required by MIDP2 and JSR specs
		Properties properties = new Properties();
		InputStream is = MIDletLauncherSE.class.getResourceAsStream("/com/sun/midp/configuration/system_properties");
		try {
			properties.load(is);
			System.setProperties(properties);
			// System.getProperties().list(System.out);
		} catch (IOException e) {
			if (Logging.REPORT_LEVEL <= Logging.WARNING) {
				Logging.report(Logging.WARNING, LogChannels.LC_CORE, "System properties can't be loaded");
			}
			e.printStackTrace();
		}
		
		// Launch the MIDlet
		MIDletLauncher.main(args);

	}

}
