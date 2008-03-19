package javax.microedition.khronos.egl;

import org.thenesis.midpath.opengles.jgl.JGLToolkit;

public abstract class EGLToolkit {

	private static EGLToolkit instance = new JGLToolkit();

	protected static EGLToolkit getInstance() {
		if (instance == null) {
			try {
				Class c = Class.forName("org.thenesis.midpath.opengles.jgl.JGLToolkit");
				instance = (EGLToolkit)c.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	protected abstract EGL getEGL();

	protected abstract EGLContext getNoContext();

	protected abstract EGLDisplay getNoDisplay();

	protected abstract EGLSurface getNoSurface();

}
