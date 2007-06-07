package org.thenesis.midpath.main;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class MidletJarClassLoaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MidletJarClassLoaderTest testSuite = new MidletJarClassLoaderTest();
		try {
			testSuite.testResourceLoading();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MidletJarClassLoaderTest() {
		System.out.println("MidletJarClassLoaderTest.<init>()");
	}

	public void test1() throws Exception {

		URL url;
		try {
			url = new URL(
					"file:///E:/Development/eclipse-3.2/workspace/midp2-sdl/ext/games/MobileSudoku/MobileSudoku.jar");
			MidletJarClassLoader loader = new MidletJarClassLoader(url);
			//loader.listMidlets();
			//System.out.println(loader.getMainClassName());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testResourceLoading() throws Exception {
		
		System.out.println("testResourceLoading()");
		
		URL url;

		url = new URL("file:///E:/Development/eclipse-3.2/workspace/mipd2-sdl-test/deployed/mipd2-sdl-test.jar");
		MidletJarClassLoader loader = new MidletJarClassLoader(url);
		Class clazz = loader.loadClass("SimpleClass");
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			System.out.println("method:" + methods[i].getName());
		}
		System.out.println("testResourceLoading()2");
		Object o = clazz.newInstance();
		System.out.println("testResourceLoading()3");
		
		//loader.listMidlets();
		//System.out.println(loader.getMainClassName());

	}

}
