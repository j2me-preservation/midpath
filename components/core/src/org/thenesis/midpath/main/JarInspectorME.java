package org.thenesis.midpath.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.thenesis.midpath.zip.ZipEntry;
import org.thenesis.midpath.zip.ZipFile;

import com.sun.midp.io.IOToolkit;
import com.sun.midp.io.j2me.file.BaseFileHandler;

public class JarInspectorME extends AbstractJarInspector {

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
