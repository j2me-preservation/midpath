/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
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
 * 
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.thenesis.midpath.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import com.sun.midp.installer.ManifestProperties;
import com.sun.midp.midletsuite.MIDletInfo;

public class MIDletSuiteJar  {

	private File file;
	private URL url;
	private URLClassLoader classLoader;
	private ManifestProperties manifestProperties;

	public MIDletSuiteJar(File file) throws MalformedURLException {
		this.file = file;
		this.url = file.toURL();
	}
	
	public URLClassLoader getURLClassLoader() {
		if (classLoader == null) {
			classLoader = new URLClassLoader(new URL[] { url });
		}
		return classLoader;
	}
	
//	public void unload() {
//		classLoader = null;
//		System.gc();
//	}
	
	public InputStream getManifest() throws IOException {
		
		URL u = new URL("jar", "", url + "!/META-INF/MANIFEST.MF");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		
		return uc.getInputStream();
	
	}
	
	//http://developers.sun.com/techtopics/mobility/midp/ttips/getAppProperty/index.html
	//http://www.onjava.com/pub/a/onjava/2001/04/26/midlet.html
	public ManifestProperties getManifestProperties() throws IOException {
		if (manifestProperties == null) {
			InputStream is = getManifest();
			manifestProperties = new ManifestProperties();
			manifestProperties.load(is);
			is.close();
		}
		return manifestProperties;
	}
	
	public String getSuiteName() throws IOException {
		return getManifestProperties().getProperty(ManifestProperties.SUITE_NAME_PROP);
	}
	
	public MIDletInfo[] getMIDletInfo() throws IOException {

		String midlet = null;
		MIDletInfo midletInfo = null;
		Vector infoList = new Vector();

		for (int i = 1;; i++) {
			midlet = getManifestProperties().getProperty("MIDlet-" + i);
			if (midlet == null) {
				break;
			}

			/*
			 * Verify the MIDlet class is present in the JAR
			 * An exception thrown if not.
			 * Do the proper install notify on an exception
			 */

			midletInfo = new MIDletInfo(midlet);
			infoList.addElement(midletInfo);
			//verifyMIDlet(midletInfo.classname);

		}

		MIDletInfo[] infos = new MIDletInfo[infoList.size()];
		for (int j = 0; j < infoList.size(); j++) {
			infos[j] = (MIDletInfo) infoList.elementAt(j);
		}

		return infos;

	}

	public File getFile() {
		return file;
	}

	public URL getUrl() {
		return url;
	}

	

//	/*public String getMainClassName() throws IOException {
//		URL u = new URL("jar", "", url + "!/");
//		JarURLConnection uc = (JarURLConnection) u.openConnection();
//		Attributes attr = uc.getMainAttributes();
//		return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
//	}
//	
//	public String listMidlets() throws IOException {
//		URL u = new URL("jar", "", url + "!/");
//		JarURLConnection uc = (JarURLConnection) u.openConnection();
//		Attributes attr = uc.getMainAttributes();
//		
//		Set entrySet = (Set)attr.entrySet();
//		if (entrySet != null) {
//			Iterator iterator = entrySet.iterator();
//			while(iterator.hasNext()) {
//				System.out.println(iterator.next());
//			}
//		}
////		for  (int i = 0; i < entries.size(); i++) {
////			entries.attr.size());
////		}
//		return null;
//		//return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
//	}
//
//	public void invokeClass(String name, String[] args) throws ClassNotFoundException, NoSuchMethodException,
//			InvocationTargetException {
//		Class c = loadClass(name);
//		Method m = c.getMethod("main", new Class[] { args.getClass() });
//		m.setAccessible(true);
//		int mods = m.getModifiers();
//		if (m.getReturnType() != void.class || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) {
//			throw new NoSuchMethodException("main");
//		}
//		try {
//			m.invoke(null, new Object[] { args });
//		} catch (IllegalAccessException e) {
//			// This should not happen, as we have 
//			// disabled access checks
//		}
//	}
	
	
}
