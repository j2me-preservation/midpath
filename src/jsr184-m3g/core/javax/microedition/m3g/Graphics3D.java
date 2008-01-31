package javax.microedition.m3g;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.lcdui.Graphics;

public final class Graphics3D {

	private static Graphics3D instance = null;

	private int maxTextureUnits = 1;
	private int maxLights = 1;

	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;

	private EGL10 egl;
	private EGLConfig eglConfig;
	private EGLDisplay eglDisplay;
	private EGLSurface eglWindowSurface;
	private EGLContext eglContext;
	private GL10 gl = null;

	private Object renderTarget;

	private Camera camera;
	private Transform cameraTransform;

	private Vector lights = new Vector();

	private CompositingMode defaultCompositioningMode = new CompositingMode();
	private PolygonMode defaultPolygonMode = new PolygonMode();
	

	private Graphics3D() {
		initGLES();
	}

	public static Graphics3D getInstance() {
		if (instance == null) {
			instance = new Graphics3D();
		}
		return instance;
	}
	
	private void initGLES() {
		
		// Create EGL context
		this.egl = (EGL10) EGLContext.getEGL();
		this.eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] major_minor = new int[2];
		egl.eglInitialize(eglDisplay, major_minor);

		int[] num_config = new int[1];
		egl.eglGetConfigs(eglDisplay, null, 0, num_config);

		int redSize = 8;
		int greenSize = 8;
		int blueSize = 8;
		int alphaSize = 0;
		int depthSize = 32;
		int stencilSize = EGL10.EGL_DONT_CARE;
		int[] s_configAttribs = { EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
				blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE,
				stencilSize, EGL10.EGL_NONE };

		EGLConfig[] eglConfigs = new EGLConfig[num_config[0]];
		egl.eglChooseConfig(eglDisplay, s_configAttribs, eglConfigs, eglConfigs.length, num_config);
		this.eglConfig = eglConfigs[0];

		this.eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null);
		this.gl = (GL10) eglContext.getGL();

		// Get the number of texture units and lights available
		int[] params = new int[1];
		gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_UNITS, params, 0);
		maxTextureUnits = params[0]; 
		gl.glGetIntegerv(GL10.GL_MAX_LIGHTS, params, 0);
		maxLights = params[0]; 
	}

	public void bindTarget(Object target) {
		if (target instanceof Graphics) {
			this.renderTarget = target;
			
			Graphics g = (Graphics)target;
			this.eglWindowSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, g, null);
			// Make the context current on this thread
			egl.eglMakeCurrent(eglDisplay, eglWindowSurface, eglWindowSurface, eglContext);

			// Perform setup and clear background using GL
			egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, g);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void bindTarget(Object target, boolean depthBuffer, int hints) {
		bindTarget(target);
	}

	public Object getTarget() {
		return this.renderTarget;
	}

	public void releaseTarget() {
		egl.eglWaitGL();

		// Release the context
		egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
	}

	public void clear(Background background) {
		if (background != null)
			background.setupGL(gl);
		else {
			// clear to black
			gl.glClearColor(0, 0, 0, 0);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		}
	}

	public int addLight(Light light, Transform transform) {
		if (light == null)
			throw new NullPointerException("light must not be null");

		lights.addElement(light);
		int index = lights.size() - 1;

		// limit the number of lights
		if (index < maxLights) {
			gl.glPushMatrix();
			transform.multGL(gl); // TODO: should this really be setGL? I was thinking multGL...

			light.setupGL(gl);
			gl.glPopMatrix();
		}

		return index;
	}

	public void setLight(int index, Light light, Transform transform) {
		lights.setElementAt(light, index);
		// TODO: set transform and update light
	}

	public void resetLights() {
		lights.removeAllElements();

		for (int i = 0; i < maxLights; ++i)
			gl.glDisable(GL10.GL_LIGHT0 + i);
	}

	public int getLightCount() {
		// TODO
		return 0;
	}

	public Light getLight(int index, Transform transform) {
		// TODO
		return null;
	}

	public int getHints() {
		// TODO
		return 0;
	}

	public boolean isDepthBufferEnabled() {
		// TODO
		return true;
	}

	public void setViewport(int x, int y, int width, int height) {
		this.viewportX = x;
		this.viewportY = y;
		this.viewportWidth = width;
		this.viewportHeight = height;

		gl.glViewport(x, y, width, height);
	}

	public int getViewportX() {
		return this.viewportX;
	}

	public int getViewportY() {
		return this.viewportY;
	}

	public int getViewportWidth() {
		return this.viewportWidth;
	}

	public int getViewportHeight() {
		return this.viewportHeight;
	}

	public void setDepthRange(float near, float far) {
		// TODO
	}

	public void setCamera(Camera camera, Transform transform) {
		this.camera = camera;
		this.cameraTransform = transform;

		Transform t = new Transform();

		gl.glMatrixMode(GL10.GL_PROJECTION);
		camera.getProjection(t);
		t.setGL(gl);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		t.set(transform);
		t.invert();
		t.setGL(gl);
	}

	public Camera getCamera(Transform transform) {
		if (transform != null)
			transform.set(this.cameraTransform);
		return camera;
	}

	public void render(Node node, Transform transform) {
		if (node instanceof Mesh) {
			Mesh mesh = (Mesh) node;
			int subMeshes = mesh.getSubMeshCount();
			VertexBuffer vertices = mesh.getVertexBuffer();
			for (int i = 0; i < subMeshes; ++i)
				render(vertices, mesh.getIndexBuffer(i), mesh.getAppearance(i), transform);
		} else if (node instanceof Sprite3D) {
			Sprite3D sprite = (Sprite3D) node;
			sprite.render(gl, transform);
		}
	}

	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform) {
		if (vertices == null)
			throw new NullPointerException("vertices == null");
		if (triangles == null)
			throw new NullPointerException("triangles == null");
		if (appearance == null)
			throw new NullPointerException("appearance == null");

		// Vertices
		float[] scaleBias = new float[4];
		VertexArray positions = vertices.getPositions(scaleBias);
		FloatBuffer pos = positions.getFloatBuffer();
		pos.position(0);
		gl.glVertexPointer(positions.getComponentCount(), GL10.GL_FLOAT, 0, pos);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// Normals
		VertexArray normals = vertices.getNormals();
		if (normals != null) {
			FloatBuffer norm = normals.getFloatBuffer();
			norm.position(0);

			gl.glEnable(GL10.GL_NORMALIZE);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, norm);
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		} else {
			gl.glDisable(GL10.GL_NORMALIZE);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}

		// Colors
		VertexArray colors = vertices.getColors();
		if (colors != null) {
			Buffer buffer = colors.getBuffer();
			buffer.position(0);
			gl.glColorPointer(colors.getComponentCount(), colors.getComponentTypeGL(), 0, buffer);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		} else
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		// Textures
		for (int i = 0; i < maxTextureUnits; ++i) {
			float[] texScaleBias = new float[4];
			VertexArray texcoords = vertices.getTexCoords(i, texScaleBias);
			gl.glClientActiveTexture(GL10.GL_TEXTURE0 + i);
			if ((texcoords != null) && (appearance.getTexture(i) != null)) {
				// Enable the texture coordinate array 
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				FloatBuffer tex = texcoords.getFloatBuffer();
				tex.position(0);

				// Activate the texture unit
				gl.glActiveTexture(GL10.GL_TEXTURE0 + i);
				appearance.getTexture(i).setupGL(gl, texScaleBias);
				
				// Set the texture coordinates
				gl.glTexCoordPointer(texcoords.getComponentCount(), GL10.GL_FLOAT, 0, tex);
			} else {
				gl.glDisable(GL10.GL_TEXTURE_2D);
				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			}
		}

		// Appearance
		setAppearance(appearance);

		// Scene
		gl.glPushMatrix();
		transform.multGL(gl);

		gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
		gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);

		// Draw
		if (triangles instanceof TriangleStripArray) {
			ShortBuffer indices = triangles.getBuffer();
			indices.position(0);
			gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, triangles.getIndexCount(), GL10.GL_UNSIGNED_SHORT, indices);
		} else
			gl.glDrawElements(GL10.GL_TRIANGLES, triangles.getIndexCount(), GL10.GL_UNSIGNED_SHORT, triangles.getBuffer());

		gl.glPopMatrix();
	}

	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform,
			int scope) {
		// TODO: check scope
		render(vertices, triangles, appearance, transform);
	}

	public void render(World world) {
		clear(world.getBackground());

		Transform t = new Transform();

		// Setup camera
		Camera c = world.getActiveCamera();
		if (c == null)
			throw new IllegalStateException("World has no active camera.");
		if (!c.getTransformTo(world, t))
			throw new IllegalStateException("Camera is not in world.");
		setCamera(c, t);

		// Setup lights
		resetLights();
		populateLights(world, world);

		// Begin traversal of scene graph
		renderDescendants(world, world);
	}

	private void populateLights(World world, Object3D obj) {
		int numReferences = obj.getReferences(null);
		if (numReferences > 0) {
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for (int i = 0; i < numReferences; ++i) {
				if (objArray[i] instanceof Light) {
					Transform t = new Transform();
					Light light = (Light) objArray[i];
					if (light.isRenderingEnabled() && light.getTransformTo(world, t))
						addLight(light, t);
				}
				populateLights(world, objArray[i]);
			}
		}
	}

	private void renderDescendants(World world, Object3D obj) {
		int numReferences = obj.getReferences(null);
		if (numReferences > 0) {
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for (int i = 0; i < numReferences; ++i) {
				if (objArray[i] instanceof Node) {
					Transform t = new Transform();
					Node node = (Node) objArray[i];
					node.getTransformTo(world, t);
					render(node, t);
				}
				renderDescendants(world, objArray[i]);
			}
		}
	}

	void setAppearance(Appearance appearance) {
		if (appearance == null)
			throw new NullPointerException("appearance must not be null");

		// Polygon mode
		PolygonMode polyMode = appearance.getPolygonMode();
		if (polyMode == null)
			polyMode = defaultPolygonMode;
		polyMode.setupGL(gl);

		// Material
		if (appearance.getMaterial() != null)
			appearance.getMaterial().setupGL(gl, polyMode.getLightTarget());
		else
			gl.glDisable(GL10.GL_LIGHTING);

		// Fog
		if (appearance.getFog() != null)
			appearance.getFog().setupGL(gl);
		else
			gl.glDisable(GL10.GL_FOG);

		// Comp mode
		if (appearance.getCompositingMode() != null)
			appearance.getCompositingMode().setupGL(gl);
		else
			defaultCompositioningMode.setupGL(gl);
	}

	void setGL(GL10 gl) {
		this.gl = gl;
	}

	int getTextureUnitCount() {
		return maxTextureUnits;
	}

	void disableTextureUnits() {
		for (int i = 0; i < maxTextureUnits; i++) {
			gl.glActiveTexture(GL10.GL_TEXTURE0 + i);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
	}
}
