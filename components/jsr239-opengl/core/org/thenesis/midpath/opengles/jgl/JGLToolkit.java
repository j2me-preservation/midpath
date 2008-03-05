package org.thenesis.midpath.opengles.jgl;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.egl.EGLToolkit;

public class JGLToolkit extends EGLToolkit {
	
	private EGLContext noContext = new JavaEGLContext();
	private EGLDisplay noDisplay = new JavaEGLDisplay();
	private EGLSurface noSurface = new JavaEGLSurface(null, 0, 0);

	protected EGL getEGL() {
		return new JavaEGL10();
	}

	protected EGLContext getNoContext() {
		return noContext;
	}

	protected EGLDisplay getNoDisplay() {
		return noDisplay;
	}

	protected EGLSurface getNoSurface() {
		return noSurface;
	}

}


