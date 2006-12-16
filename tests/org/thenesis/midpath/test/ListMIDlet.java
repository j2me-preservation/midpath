/*
 * MIDPath - Copyright (C) 2006 Guillaume Legris, Mathieu Legris
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details. 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */
package org.thenesis.midpath.test;

import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class ListMIDlet extends MIDlet {

	List softList;
	List jvmList;

	public ListMIDlet() {
	  softList = new List("Select the type of software you like",
	                         Choice.MULTIPLE);
	  softList.append("Free Software", null);
	  softList.append("Logiciel libre", null);
	  softList.insert(1, "Freier Software", null);

	  String jvms[] = {"Cacao", "Kaffe", "Jamvm"};
	  jvmList =
	    new List(
				"Select the JVM you like",
	      Choice.IMPLICIT,
	      jvms,
	      null);
	}

	public void startApp() {
		Display display = Display.getDisplay(this);
		display.setCurrent(softList);

		try{
		  Thread.currentThread().sleep(3000);
		} catch(Exception e) {}

		display.setCurrent(jvmList);
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}
}
