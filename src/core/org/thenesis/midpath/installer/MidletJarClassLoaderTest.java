package org.thenesis.midpath.installer;

import java.net.MalformedURLException;
import java.net.URL;

public class MidletJarClassLoaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		URL url;
		try {
			url = new URL("file:///E:/Development/eclipse-3.2/workspace/midp2-sdl/ext/games/MobileSudoku/MobileSudoku.jar");
			MidletJarClassLoader loader = new MidletJarClassLoader(url);
			//loader.listMidlets();
			//System.out.println(loader.getMainClassName());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
