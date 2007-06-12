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
import java.util.Vector;

import com.sun.midp.installer.ManifestProperties;
import com.sun.midp.log.Logging;
import com.sun.midp.main.BaseMIDletSuiteLauncher;
import com.sun.midp.main.Configuration;
import com.sun.midp.main.MIDletClassLoader;
import com.sun.midp.midletsuite.MIDletInfo;

public class J2SEMidletSuiteLauncher {
	
	static MIDletRespository repository;
	static String repositoryPath;
	
	static {
		repositoryPath = Configuration.getPropertyDefault("org.thenesis.midpath.main.repositoryPath", "");
		//System.out.println("repositoryPath: " + repositoryPath);
		repository = new MIDletRespository(repositoryPath);
	}

	public J2SEMidletSuiteLauncher() {
	}

	public void launchManager() throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		MIDletClassLoader classLoader = new MIDletClassLoader() {
			public Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
				Class midletClass = Class.forName(className);
				if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
					throw new InstantiationException("Class not a MIDlet");
				}
				return midletClass;
			}
		};

		// Initialize the manager MIDlet 
		BaseMIDletSuiteLauncher.launch(classLoader, "org.thenesis.midpath.main.SuiteManagerMIDlet", "Suite Manager");

		MIDletInfo info = SuiteManagerMIDlet.launchMidletInfo;
		final MIDletSuiteJar midletSuiteJar = SuiteManagerMIDlet.launchMidletSuiteJar;
		
		if (info != null) {

			// Launch the MIDlet returned by the manager
			//MIDletInfo info = new MIDletInfo("TextFieldDemo", null, "org.thenesis.midpath.test.ui.textfield.TextFieldDemo");
			// File file = new File("E:/Development/eclipse-3.2/workspace/mipd2-sdl-test/deployed/mipd2-sdl-test.jar");
			// final MIDletSuiteJar midletSuiteJar = new MIDletSuiteJar(file);
			
			classLoader = new MIDletClassLoader() {
				public Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
					Class midletClass = midletSuiteJar.getURLClassLoader().loadClass(className);
					if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
						throw new InstantiationException("Class not a MIDlet");
					}
					return midletClass;
				}
			};
			BaseMIDletSuiteLauncher.launch(classLoader, info.classname, info.name);
		}

		// Clean all and stop the VM
		BaseMIDletSuiteLauncher.close();

	}

	void launchFirstMIDlet(File file) throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		final MIDletSuiteJar midletSuiteJar = new MIDletSuiteJar(file);
		MIDletClassLoader classLoader = new MIDletClassLoader() {
			public Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
				Class midletClass = midletSuiteJar.getURLClassLoader().loadClass(className);
				if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
					throw new InstantiationException("Class not a MIDlet");
				}
				return midletClass;
			}
		};

		ManifestProperties manifestProperties = midletSuiteJar.getManifestProperties();
		int size = manifestProperties.size();
		for (int i = 0; i < size; i++) {
			System.out.println(manifestProperties.getKeyAt(i) + "=" + manifestProperties.getValueAt(i));
		}

		MIDletInfo[] infos = midletSuiteJar.getMIDletInfo();

		final MIDletInfo info = infos[0];
		if (infos.length == 0) {
			System.out.println("No MIDlet found");
			return;
		}

		BaseMIDletSuiteLauncher.launch(classLoader, info.classname, info.name);
		BaseMIDletSuiteLauncher.close();

		//		MIDletSuiteInfo[] suiteInfos = getMidletSuiteInfo();
		//		
		//		if (suiteInfos.length > 0) {
		//			MIDletInfo info = suiteInfos[0].midletInfo[0];
		//			
		//			DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(new DisplayEventHandlerImpl());
		//			MIDletSuiteLoader.init(info.classname, info.name);
		//		} else {
		//			System.out.println("No MIDlet found");
		//		}

	}

	void launch(File file, String className, String midletName) throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		final MIDletSuiteJar midletSuiteJar = new MIDletSuiteJar(file);
		MIDletClassLoader classLoader = new MIDletClassLoader() {
			public Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
				Class midletClass = midletSuiteJar.getURLClassLoader().loadClass(className);
				if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
					throw new InstantiationException("Class not a MIDlet");
				}
				return midletClass;
			}
		};

		BaseMIDletSuiteLauncher.launch(classLoader, className, midletName);
		BaseMIDletSuiteLauncher.close();
	}

	//	public void launchAndClose(URL url, String className, String midletName) throws Exception {
	//		BaseMIDletSuiteLauncher.initialize();
	//		launch(url, className, midletName);
	//		BaseMIDletSuiteLauncher.close();
	//	}

	//	public String getId() {
	//		String id = manifestProperties.getProperty(ManifestProperties.SUITE_NAME_PROP);
	//		id.replace(' ', '_');
	//		return id;
	//	}

	//	private void initialize(String url) throws MalformedURLException {
	//		jarURL = new URL(url);
	//		
	//	}

	//	public MIDletSuiteInfo[] getMidletSuiteInfo() throws IOException {
	//		
	//		ManifestProperties manifest = getManifestProperties();
	//		MIDletSuiteInfo[] suiteInfos = new MIDletSuiteInfo[manifests.length];
	//		
	//		for (int i = 0; i < manifests.length; i++) {
	//			ManifestProperties  p = manifests[i];
	//			MIDletInfo[] infos = getMIDletInfo(p);
	//			suiteInfos[i] = new MIDletSuiteInfo(p, infos);
	//		}
	//		
	//		return suiteInfos;
	//		
	//	}

	//	public MIDletInfo[] getMIDletInfo(ManifestProperties manifestProperties) throws IOException {
	//
	//		String midlet = null;
	//		MIDletInfo midletInfo = null;
	//		Vector infoList = new Vector();
	//
	//		for (int i = 1;; i++) {
	//			midlet = manifestProperties.getProperty("MIDlet-" + i);
	//			if (midlet == null) {
	//				break;
	//			}
	//
	//			/*
	//			 * Verify the MIDlet class is present in the JAR
	//			 * An exception thrown if not.
	//			 * Do the proper install notify on an exception
	//			 */
	//
	//			midletInfo = new MIDletInfo(midlet);
	//			infoList.addElement(midletInfo);
	//			//verifyMIDlet(midletInfo.classname);
	//
	//		}
	//
	//		MIDletInfo[] infos = new MIDletInfo[infoList.size()];
	//		for (int j = 0; j < infoList.size(); j++) {
	//			infos[j] = (MIDletInfo) infoList.elementAt(j);
	//		}
	//
	//		return infos;
	//
	//	}

	//	public ManifestProperties getManifestProperties(MidletJarClassLoader loader) throws IOException {
	//		InputStream is = loader.getManifest();
	//		ManifestProperties manifestProperties = new ManifestProperties();
	//		manifestProperties.load(is);
	//		return manifestProperties;
	//
	//	}

	//	public ManifestProperties[] getManifests() throws IOException {
	//		
	//		Vector v = new Vector();
	//		System.out.println("a");
	//		
	////		URL url =getClass().getResource("/META-INF/MANIFEST.MF");
	////		InputStream is = url.openStream();
	////		ManifestProperties manifestProperties = new ManifestProperties();
	////		manifestProperties.load(is);
	////		v.add(manifestProperties);
	////		System.out.println(url);
	//		
	//		Enumeration e = getClass().getClassLoader().getSystemResources("/META-INF/MANIFEST.MF");
	//		
	//		while(e.hasMoreElements()) {
	//			URL url = (URL)e.nextElement();
	//			InputStream is = url.openStream();
	//			ManifestProperties manifestProperties = new ManifestProperties();
	//			manifestProperties.load(is);
	//			v.add(manifestProperties);
	//			System.out.println(url);
	//		}
	//		
	//		ManifestProperties[] properties = new ManifestProperties[v.size()]; 
	//		for (int i = 0; i < v.size(); i++) {
	//			properties[i] = (ManifestProperties) v.get(i);
	//		}
	//		
	//		return properties;
	//		
	//	}

	//	public void verifyMIDlet(String classname)
	//    throws InvalidJadException
	//{
	//    if (classname == null ||
	//        classname.length() == 0) {
	//        throw new
	//            InvalidJadException(InvalidJadException.INVALID_VALUE);
	//    }
	//
	//    String file = classname.replace('.', '/').concat(".class");
	//
	//    try {
	//        /* Attempt to read the MIDlet from the JAR file. */
	//        if (JarReader.readJarEntry(state.tempFilename, file) != null) {
	//            return;                // File found, normal return
	//        }
	//        // Fall into throwing the exception
	//    } catch (IOException ioe) {
	//        // Fall into throwing the exception
	//    }
	//    // Throw the InvalidJadException
	//    throw new InvalidJadException(InvalidJadException.CORRUPT_JAR, file);
	//}

	//state.id = state.midletSuiteStorage.createSuiteID(state.vendor,
	//      state.suiteName);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			J2SEMidletSuiteLauncher launcher = new J2SEMidletSuiteLauncher();
			launcher.launchManager();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//File file = new File("ext/games/MobileSudoku/MobileSudoku.jar");
		//File file = new File("E:/Development/eclipse-3.2/workspace/mipd2-sdl-test/deployed/mipd2-sdl-test.jar");
//		try {
//			J2SEMidletSuiteLauncher launcher = new J2SEMidletSuiteLauncher();
//			launcher.launchManager();
//			//launcher.launchFirstMIDlet(file.toURL());
//			//launcher.launchManager("E:/Development/eclipse-3.2/workspace/mipd2-sdl-test/deployed/");
//
//			//			MIDletRespository rep = new MIDletRespository("E:/Development/eclipse-3.2/workspace/mipd2-sdl-test/deployed/");
//			//			System.out.println(rep.getInstallDirectory("mipd2-sdl-test.jar"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		//		File file = new File("ext/games/MobileSudoku/MobileSudoku.jar");
		//		System.out.println(file.exists());
		//		try {
		//			MidletJarClassLoader loader = new MidletJarClassLoader(file.toURL());
		////			Class c = loader.loadClass("asteroids.Game");
		////			System.out.println(c);
		////			URL url = loader.findResource("/meta-inf/manifest.mf");
		////			System.out.println(url);
		//			InputStream is = loader.getManifest();
		//			ManifestProperties manifestProperties = new ManifestProperties();
		//			manifestProperties.load(is);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

		//		MidletSuiteLauncher launcher =  new MidletSuiteLauncher();
		//		
		//		URL url =	launcher.getClass(). getResource("/META-INF/MANIFEST.MF");
		//		System.out.println(url );

		//		File file = new File("ext/games/MobileSudoku/MobileSudoku.jar");
		//		try {
		//			MidletSuiteLauncher launcher =  new MidletSuiteLauncher();
		//			launcher.start();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

	}

//	class MIDletSuiteInfo {
//
//		MIDletInfo[] midletInfo;
//		ManifestProperties manifestProperties;
//
//		public MIDletSuiteInfo(ManifestProperties p, MIDletInfo[] infos) {
//			manifestProperties = p;
//			midletInfo = infos;
//		}
//
//		public String getId() {
//			String id = manifestProperties.getProperty(ManifestProperties.SUITE_NAME_PROP);
//			id.replace(' ', '_');
//			return id;
//		}
//	}

}

class MIDletRespository {

	private File repositoryDir;
	private Vector installedJars = new Vector();
	private Vector notInstalledJars = new Vector();

	public MIDletRespository(String path) {
		repositoryDir = new File(path);
	}

	public void scanRepository() throws IOException {
		//		File[] files = repositoryDir.listFiles(new FilenameFilter() {
		//			public boolean accept(File dir, String name) {
		//				if (name.endsWith("jar")) {
		//					return true;
		//				}
		//				return false;
		//			}
		//		});

		installedJars.clear();
		notInstalledJars.removeAllElements();

		File[] files = repositoryDir.listFiles();

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.toLowerCase().endsWith(".jar") && files[i].isFile()) {
				File dir = new File(repositoryDir, getInstallDirectory(name));
				if (dir.exists()) {
					MIDletSuiteJar jar = new MIDletSuiteJar(files[i]);
					installedJars.addElement(jar);
//					System.out.println("[DEBUG] J2SEMidletSuiteLauncher.scanRepository(): installed "
//							+ files[i].getName());
				} else {
//					System.out.println("[DEBUG] J2SEMidletSuiteLauncher.scanRepository(): not installed "
//							+ files[i].getName());
					notInstalledJars.add(new MIDletSuiteJar(files[i]));
				}
			}
		}

	}

	//	public String[] getInstalledSuiteNames() {
	//		Vector installedList = getInstalledJars();
	//		for (int i = 0; i < installedList.size(); i++) {
	//			MIDletSuiteJar jar = (MIDletSuiteJar)installedList.elementAt(i);
	//			ManifestProperties manifestProperties;
	//			try {
	//				manifestProperties = jar.getManifestProperties();
	//				suiteName = manifestProperties.getProperty(ManifestProperties.SUITE_NAME_PROP);
	//				installedGroup.append("Suite 1", null);
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//			
	//		}
	//	}

	public void uninstallSuite(String suiteName) throws IOException {
		for (int i = 0; i < installedJars.size(); i++) {
			MIDletSuiteJar jar = (MIDletSuiteJar) installedJars.elementAt(i);
			if (jar.getSuiteName().equals(suiteName)) {
				uninstallSuite(jar.getFile());
				break;
			}
		}
	}

	private void uninstallSuite(File jarFile) {
		// Remove the directory matching with the given jar file
		File dir = new File(repositoryDir, getInstallDirectory(jarFile.getName()));
		dir.delete();
	}
	
	public MIDletSuiteJar getJarFromSuiteName(String suiteName) throws IOException {
		for (int i = 0; i < installedJars.size(); i++) {
			MIDletSuiteJar jar = (MIDletSuiteJar) installedJars.elementAt(i);
			if (jar.getSuiteName().equals(suiteName)) {
				return jar;
			}
		}
		return null;
	}
	

	public void installJar(String fileName) throws IOException {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] J2SEMidletSuiteLauncher.installJar(): " + notInstalledJars.size());

		for (int i = 0; i < notInstalledJars.size(); i++) {
			MIDletSuiteJar jar = (MIDletSuiteJar) notInstalledJars.elementAt(i);
			File file = jar.getFile();
			if (file.getName().equals(fileName)) {
				installJar(file);
				break;
			}
		}
	}

	private void installJar(File jarFile) {
		// Create a directory with the same name of the jar file
		File dir = new File(repositoryDir, getInstallDirectory(jarFile.getName()));
		dir.mkdir();
	}

	public void removeJar(String fileName) throws IOException {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] J2SEMidletSuiteLauncher.removeJar(): " + notInstalledJars.size());

		for (int i = 0; i < notInstalledJars.size(); i++) {
			MIDletSuiteJar jar = (MIDletSuiteJar) notInstalledJars.elementAt(i);
			File file = jar.getFile();
			if (file.getName().equals(fileName)) {
				removeJar(file);
				break;
			}
		}
	}

	private void removeJar(File jarFile) {
		jarFile.delete();
	}

	private String getInstallDirectory(String jarFileName) {
		return jarFileName.substring(0, jarFileName.length() - 4);
	}

	public Vector getInstalledJars() {
		return installedJars;
	}

	public Vector getNotInstalledJars() {
		return notInstalledJars;
	}
	
//	public J2SEMidletSuiteLauncher() {
	//
	//	}

	//	public void start() throws Exception {
	//
	//		ManifestProperties manifestProperties = getManifestProperties();
	//
	//		MIDletInfo[] infos = getMIDletInfo(manifestProperties);
	//
	//		if (infos.length > 0) {
	//			MIDletInfo info = infos[0];
	//
	//			DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(new DisplayEventHandlerImpl());
	//			launchAndClose(info.classname, info.name);
	//		} else {
	//			System.out.println("No MIDlet found");
	//		}
	//
	//		//		MIDletSuiteInfo[] suiteInfos = getMidletSuiteInfo();
	//		//		
	//		//		if (suiteInfos.length > 0) {
	//		//			MIDletInfo info = suiteInfos[0].midletInfo[0];
	//		//			
	//		//			DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(new DisplayEventHandlerImpl());
	//		//			MIDletSuiteLoader.init(info.classname, info.name);
	//		//		} else {
	//		//			System.out.println("No MIDlet found");
	//		//		}
	//
	//	}

}
