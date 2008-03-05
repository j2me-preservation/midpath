package javax.microedition.khronos.egl;

import org.thenesis.midpath.opengles.jgl.JGLToolkit;

public abstract class EGLToolkit {
	
	private static EGLToolkit instance = new JGLToolkit();
	
	protected static EGLToolkit getInstance() {
		return instance;
	}
	
	protected abstract EGL getEGL();
	protected abstract EGLContext getNoContext();
	protected abstract EGLDisplay getNoDisplay();
	protected abstract EGLSurface getNoSurface();

}
