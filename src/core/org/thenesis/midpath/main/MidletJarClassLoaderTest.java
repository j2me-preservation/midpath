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
			MIDletSuiteJar loader = new MIDletSuiteJar(url);
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
		MIDletSuiteJar loader = new MIDletSuiteJar(url); 
		Class clazz = loader.getURLClassLoader().loadClass("SimpleClass");
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
