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
import java.io.PrintWriter;
import java.util.Vector;

import com.sun.midp.installer.ManifestProperties;
import com.sun.midp.log.Logging;
import com.sun.midp.main.BaseMIDletSuiteLauncher;
import com.sun.midp.main.Configuration;
import com.sun.midp.main.MIDletClassLoader;
import com.sun.midp.midlet.InternalMIDletSuiteImpl;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midletsuite.MIDletInfo;

public class J2SEMIDletLauncher {

	static MIDletRespository repository;
	static String repositoryPath;

	static {
		repositoryPath = Configuration.getPropertyDefault("org.thenesis.midpath.main.repositoryPath", "");
		//System.out.println("repositoryPath: " + repositoryPath);
		repository = new MIDletRespository(repositoryPath);
	}

	public J2SEMIDletLauncher() {
	}

	public void launchManager() throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		// Initialize the manager MIDlet 
		BaseMIDletSuiteLauncher.launch("org.thenesis.midpath.main.SuiteManagerMIDlet", "Suite Manager");

		// Get the launch infos from the MIDlet
		final MIDletInfo info = SuiteManagerMIDlet.launchMidletInfo;
		final MIDletSuiteJar midletSuiteJar = SuiteManagerMIDlet.launchMidletSuiteJar;

		if (info != null) {

			// Launch the MIDlet returned by the manager
			BaseMIDletSuiteLauncher.setMIDletClassLoader(new J2SEMIDletClassLoader(midletSuiteJar));
			String suiteName = midletSuiteJar.getSuiteName();
			String suiteId = InternalMIDletSuiteImpl.buildSuiteID(suiteName);
			MIDletSuite midletSuite = InternalMIDletSuiteImpl.create(midletSuiteJar.getManifestProperties(), suiteName,
					suiteId);
			BaseMIDletSuiteLauncher.launch(midletSuite, info.classname);
		}

		// Clean all and stop the VM
		BaseMIDletSuiteLauncher.close();

	}

	void launchFirstMIDlet(File file) throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		final MIDletSuiteJar midletSuiteJar = new MIDletSuiteJar(file);
		MIDletInfo[] infos = midletSuiteJar.getMIDletInfo();

		ManifestProperties manifestProperties = midletSuiteJar.getManifestProperties();
		int size = manifestProperties.size();
		for (int i = 0; i < size; i++) {
			System.out.println(manifestProperties.getKeyAt(i) + "=" + manifestProperties.getValueAt(i));
		}

		final MIDletInfo info = infos[0];
		if (infos.length == 0) {
			System.out.println("No MIDlet found");
			return;
		}

		BaseMIDletSuiteLauncher.setMIDletClassLoader(new J2SEMIDletClassLoader(midletSuiteJar));

		BaseMIDletSuiteLauncher.launch(info.classname, info.name);
		BaseMIDletSuiteLauncher.close();

	}

	void launch(File file, String className, String midletName) throws Exception {

		BaseMIDletSuiteLauncher.initialize();

		final MIDletSuiteJar midletSuiteJar = new MIDletSuiteJar(file);
		MIDletInfo[] infos = midletSuiteJar.getMIDletInfo();

		//		ManifestProperties manifestProperties = midletSuiteJar.getManifestProperties();
		//		int size = manifestProperties.size();
		//		for (int i = 0; i < size; i++) {
		//			System.out.println(manifestProperties.getKeyAt(i) + "=" + manifestProperties.getValueAt(i));
		//		}

		int index = -1;
		for (int i = 0; i < infos.length; i++) {
			if (className.equals(infos[i].classname)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			System.out.println("No MIDlet with class name " + className + " was found");
			return;
		}

		BaseMIDletSuiteLauncher.setMIDletClassLoader(new J2SEMIDletClassLoader(midletSuiteJar));
		BaseMIDletSuiteLauncher.launch(className, midletName);
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
			J2SEMIDletLauncher launcher = new J2SEMIDletLauncher();
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

class J2SEMIDletClassLoader implements MIDletClassLoader {

	private MIDletSuiteJar midletSuiteJar;
	private Class midletClass;

	public J2SEMIDletClassLoader(MIDletSuiteJar midletSuiteJar) {
		this.midletSuiteJar = midletSuiteJar;
	}

	public synchronized Class getMIDletClass(String className) throws ClassNotFoundException, InstantiationException {
		midletClass = midletSuiteJar.getURLClassLoader().loadClass(className);
		if (!Class.forName("javax.microedition.midlet.MIDlet").isAssignableFrom(midletClass)) {
			throw new InstantiationException("Class not a MIDlet");
		}
		return midletClass;
	}

	public synchronized InputStream getResourceAsStream(String name) {
		InputStream is = midletClass.getResourceAsStream(name);
		//System.out.println(name + " : " + is);
		return is;
	}
}

class MIDletRespository {

	private File repositoryDir;
	private Vector installedJars = new Vector();
	private Vector notInstalledJars = new Vector();

	public MIDletRespository(String path) {
		repositoryDir = new File(path);
		repositoryDir = repositoryDir.getAbsoluteFile();
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

		installedJars.removeAllElements();
		notInstalledJars.removeAllElements();

		File[] files = repositoryDir.listFiles();

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.toLowerCase().endsWith(".jar") && files[i].isFile()) {
				File jad = new File(repositoryDir, getJadFileName(name));
				if (jad.exists()) {
					MIDletSuiteJar jar = new MIDletSuiteJar(files[i]);
					installedJars.addElement(jar);
					//					System.out.println("[DEBUG] J2SEMidletSuiteLauncher.scanRepository(): installed "
					//							+ files[i].getName());
				} else {
					//					System.out.println("[DEBUG] J2SEMidletSuiteLauncher.scanRepository(): not installed "
					//							+ files[i].getName());
					notInstalledJars.addElement(new MIDletSuiteJar(files[i]));
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
		File jad = new File(repositoryDir, getJadFileName(jarFile.getName()));
		jad.delete();
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
				installJar(jar);
				break;
			}
		}
	}

	private void installJar(MIDletSuiteJar jar) throws IOException {
		// Create a jad with the same name of the jar file
		File jad = new File(repositoryDir, getJadFileName(jar.getFile().getName()));
		PrintWriter writer = new PrintWriter(jad);

		ManifestProperties properties = jar.getManifestProperties();
		for (int i = 0; i < properties.size(); i++) {
			String key = properties.getKeyAt(i);
			String value = properties.getValueAt(i);
			writer.println(key + ":" + value);
		}

		// Add attributes required by JAD specs
		// References:
		// - http://developers.sun.com/techtopics/mobility/midp/ttips/getAppProperty/index.html)
		// - http://www.onjava.com/pub/a/onjava/excerpt/j2menut_3/index2.html?page=3
		writer.println(ManifestProperties.JAR_URL_PROP + ":" + jar.getFile().getName());
		writer.println(ManifestProperties.JAR_SIZE_PROP + ":" + jar.getFile().length());
		writer.flush();
		writer.close();

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

	private String getJadFileName(String jarFileName) {
		return (jarFileName.substring(0, jarFileName.length() - 4) + ".jad");
	}

	public Vector getInstalledJars() {
		return installedJars;
	}

	public Vector getNotInstalledJars() {
		return notInstalledJars;
	}

}
