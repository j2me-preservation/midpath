package org.thenesis.midpath.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.DisplayEventHandlerImpl;

import com.sun.midp.installer.ManifestProperties;
import com.sun.midp.lcdui.DisplayEventHandlerFactory;
import com.sun.midp.main.MIDletSuiteLoader;
import com.sun.midp.midletsuite.MIDletInfo;


public class MidletSuiteLauncher {

	private MidletJarClassLoader loader;
	private URL jarURL; 

	public MidletSuiteLauncher(String url) throws Exception {
		this(new URL(url));
	}
	
	public MidletSuiteLauncher(URL url) throws Exception {
		jarURL = url;
		loader = new MidletJarClassLoader(jarURL);
	}
	
	public MidletSuiteLauncher() {
		
	}
	
	public void start() throws Exception {
		
		ManifestProperties manifestProperties = getManifestProperties();
		
		MIDletInfo[] infos = getMIDletInfo(manifestProperties);
		
		if (infos.length > 0) {
			MIDletInfo info = infos[0];
			
			DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(new DisplayEventHandlerImpl());
			MIDletSuiteLoader.init(info.classname, info.name);
		} else {
			System.out.println("No MIDlet found");
		}
		
		
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
	
	
	public MIDletInfo[] getMIDletInfo(ManifestProperties manifestProperties) throws IOException {
		
		String midlet = null;
		MIDletInfo midletInfo = null;
		Vector infoList = new Vector();
		
		for (int i = 1; ; i++) {
            midlet = manifestProperties.getProperty("MIDlet-" + i);
            if (midlet == null) {
                break;
            }

            /*
             * Verify the MIDlet class is present in the JAR
             * An exception thrown if not.
             * Do the proper install notify on an exception
             */
           
            midletInfo = new MIDletInfo(midlet);
            infoList.add(midletInfo);
                //verifyMIDlet(midletInfo.classname);
           
        }
		
		MIDletInfo[] infos = new MIDletInfo[infoList.size()];
		for (int j = 0; j < infoList.size(); j++) {
			infos[j] = (MIDletInfo)infoList.get(j);
		}
		
		return infos;
		
	}
	
//	//http://developers.sun.com/techtopics/mobility/midp/ttips/getAppProperty/index.html
//	//http://www.onjava.com/pub/a/onjava/2001/04/26/midlet.html
	public ManifestProperties getManifestProperties() throws IOException {
		
		MidletJarClassLoader loader = new MidletJarClassLoader(jarURL);
		InputStream is = loader.getManifest();
		ManifestProperties manifestProperties = new ManifestProperties();
		manifestProperties.load(is);
		return manifestProperties;
		
	}
	

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
		
		File file = new File("ext/games/MobileSudoku/MobileSudoku.jar");
		try {
			MidletSuiteLauncher launcher =  new MidletSuiteLauncher(file.toURL());
			launcher.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
	
	class MIDletSuiteInfo {
		
		MIDletInfo[] midletInfo;
		ManifestProperties manifestProperties;
		
		public MIDletSuiteInfo(ManifestProperties p, MIDletInfo[] infos) {
			manifestProperties = p;
			midletInfo = infos;
		}
		
		public String getId() {
			String id = manifestProperties.getProperty(ManifestProperties.SUITE_NAME_PROP);
			id.replace(' ', '_');
			return id;
		}
	}
}
