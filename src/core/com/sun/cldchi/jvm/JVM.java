package com.sun.cldchi.jvm;

public class JVM {

	/**
	 * Wrapper to System.loadLibrary(). It's needed to support JNI library 
	 * loading from CLDC code running on J2SE.
	 * @param name The name of the library to load
	 */
	public static void loadLibrary(String name) {
		System.loadLibrary(name);
	}

}
