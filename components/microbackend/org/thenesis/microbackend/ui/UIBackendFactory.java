/*
 * MIDPath - Copyright (C) 2006-2008 Guillaume Legris, Mathieu Legris
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
 */
package org.thenesis.microbackend.ui;

import org.thenesis.microbackend.ui.awt.AWTBackend;
import org.thenesis.microbackend.ui.awtgrabber.AWTGrabberBackend;
import org.thenesis.microbackend.ui.fb.FBBackend;
import org.thenesis.microbackend.ui.gtk.GTKBackend;
import org.thenesis.microbackend.ui.qt.QTBackend;
import org.thenesis.microbackend.ui.sdl.SDLBackend;
import org.thenesis.microbackend.ui.swt.SWTBackend;
import org.thenesis.microbackend.ui.x11.X11Backend;

public class UIBackendFactory {
	
	//public static final String BACKEND_PACKAGE_PREFIX = "org.thenesis.microbackend.ui.";
	
	public static final String BACKEND_SDL = "SDL";
	public static final String BACKEND_AWT = "AWT";
	public static final String BACKEND_AWTGRABBER = "AWTGRABBER";
	public static final String BACKEND_SWT = "SWT";
	public static final String BACKEND_X11 = "X11";
	public static final String BACKEND_GTK = "GTK";
	public static final String BACKEND_QT = "QT";
	public static final String BACKEND_FB = "FB";
	public static final String BACKEND_NULL = "NULL";
	
	public static UIBackend createBackend(String name) {
		
		UIBackend backend = null;
		
		if (name.equalsIgnoreCase(BACKEND_SDL)) {
			backend = new SDLBackend();
		} else if (name.equalsIgnoreCase(BACKEND_AWT)) {
			backend = new AWTBackend();
		} else if (name.equalsIgnoreCase(BACKEND_AWTGRABBER)) {
			backend = new AWTGrabberBackend();
		} else if (name.equalsIgnoreCase(BACKEND_SWT)) {
			backend = new SWTBackend();
		} else if (name.equalsIgnoreCase(BACKEND_X11)) {
			backend = new X11Backend();
		} else if (name.equalsIgnoreCase(BACKEND_GTK)) {
			backend = new GTKBackend();
		} else if (name.equalsIgnoreCase(BACKEND_QT)) {
			backend = new QTBackend();	
		} else if (name.equalsIgnoreCase(BACKEND_FB)) {
			backend = new FBBackend();
		} else {
			return null;
		}
		
		return backend;
		
	}

}
