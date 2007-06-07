package org.thenesis.midpath.main;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;

public class MidletJarClassLoader extends java.net.URLClassLoader {

	private URL url;

	public MidletJarClassLoader(URL url) {
		super(new URL[] { url });
		this.url = url;
	}
	
	public InputStream getManifest() throws IOException {
		
		URL u = new URL("jar", "", url + "!/META-INF/MANIFEST.MF");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		return uc.getInputStream();
	
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
