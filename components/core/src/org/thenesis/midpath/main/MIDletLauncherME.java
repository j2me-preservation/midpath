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
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 */
package org.thenesis.midpath.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.thenesis.midpath.zip.ZipEntry;
import org.thenesis.midpath.zip.ZipFile;

import com.sun.midp.io.IOToolkit;
import com.sun.midp.io.j2me.file.BaseFileHandler;
import com.sun.midp.main.BaseMIDletSuiteLauncher;
import com.sun.midp.midlet.InternalMIDletSuiteImpl;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midletsuite.MIDletInfo;

public class MIDletLauncherME {
	
	static JarInspectorME jarInspector;

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println("java org.thenesis.midpath.main.MIDletLauncherME <midlet-class-name> [midlet-name]");
			System.out.println("java org.thenesis.midpath.main.MIDletLauncherME -jar <absolute-jar-path>");
			System.exit(1);
		}

		MIDletLauncherME launcher = new MIDletLauncherME();
		try {
			launcher.launch(args);
		} catch (Exception e) {
			System.err.println("An error occured while trying to start the midlet:");
			e.printStackTrace();
		}

	}

	public void launch(String[] args) throws Exception {

		if (args.length == 2) {
			if (args[0].equals("-jar")) {
				jarInspector = new JarInspectorME(args[1]);
				MIDletInfo[] infos = jarInspector.getMIDletInfo();
				if (infos.length == 0) {
					throw new Exception("No MIDlet was found in the jar (corrupted /META-INF/MANIFEST file ?)");
				} else if (infos.length == 1) {
					launchAndClose(infos[0].classname, infos[0].name);
				} else {
					launchMIDletWithChooserAndClose();
				}
			} else {
				String className = args[0];
				String midletName = args[1];
				launchAndClose(className, midletName);
			}
		} else {
			String className = args[0];
			String midletName = className;
			launchAndClose(className, midletName);
		}
	}
	
	void launchMIDletWithChooserAndClose() throws Exception {
		// Initialize the chooser MIDlet
		BaseMIDletSuiteLauncher.initialize();
		BaseMIDletSuiteLauncher.launch("org.thenesis.midpath.main.MIDletChooserMIDlet", "MIDlet Chooser");
		
		// Get the launch infos from the MIDlet
		final MIDletInfo info = MIDletChooserMIDlet.launchMidletInfo;

		if (info != null) {
			// Launch the MIDlet returned by the manager
			String suiteName = jarInspector.getSuiteName();
			String suiteId = InternalMIDletSuiteImpl.buildSuiteID(suiteName);
			MIDletSuite midletSuite = InternalMIDletSuiteImpl.create(jarInspector.getManifestProperties(), suiteName,
					suiteId);
			BaseMIDletSuiteLauncher.launch(midletSuite, info.classname);
		}

		// Clean all and stop the VM
		BaseMIDletSuiteLauncher.close();
	}

	public void launchAndClose(String className, String midletName) throws Exception {
		BaseMIDletSuiteLauncher.initialize();
		launch(className, midletName);
		BaseMIDletSuiteLauncher.close();
	}

	void launch(String className, String midletName) throws Exception {
		BaseMIDletSuiteLauncher.launch(className, midletName);
	}

}

class JarInspectorME extends AbstractJarInspector {

	private String fileName;

	public JarInspectorME(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getManifest() throws IOException {
		
		// Load the zip from the file system 
		BaseFileHandler fHandler = IOToolkit.getToolkit().createBaseFileHandler();
		fHandler.connect("", fileName);
		if (!fHandler.exists()) {
			throw new IOException("File doesn't exist");
		}
		fHandler.openForRead();
		ZipFile file = new ZipFile(fHandler);
		
		// Get the manifest zip entry 
		// ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");
		Enumeration enumeration = file.entries();
		ZipEntry manifestEntry = null;
		while(enumeration.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)enumeration.nextElement();
			if (entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
				manifestEntry = entry;
				break;
			}
		}
		
		if (manifestEntry == null) {
			throw new IOException("No MANIFEST file in the jar");
		}
		
		InputStream is = file.getInputStream(manifestEntry);
		return is;
	}
	


}