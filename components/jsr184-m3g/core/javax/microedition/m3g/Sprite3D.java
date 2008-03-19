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
package javax.microedition.m3g;

import java.util.Hashtable;

import javax.microedition.khronos.opengles.GL10;

public class Sprite3D extends Node {
	private static Hashtable textures = new Hashtable();

	private boolean scaled = false;
	private Appearance appearance;
	private Image2D image;
	private Texture2D texture;

	public Sprite3D(boolean scaled, Image2D image, Appearance appearance) {
		this.setImage(image);
		this.setAppearance(appearance);
	}
	
	Object3D duplicateImpl() {
		Sprite3D copy = new Sprite3D(scaled, image, appearance);
		duplicate((Node)copy);
		copy.texture = texture;
		return copy;
	}

	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public void setImage(Image2D image) {
		this.image = image;
		texture = (Texture2D) textures.get(image);

		if (texture == null) {
			texture = new Texture2D(image);
			texture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
			texture.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
			texture.setBlending(Texture2D.FUNC_REPLACE);

			// cache texture
			textures.put(image, texture);
		}
	}

	public Image2D getImage() {
		return image;
	}

	public boolean isScaled() {
		return scaled;
	}

	void render(GL10 gl, Transform t) {
		
		
		System.out.println("[DEBUG] Sprite3D.render(): not implemented yet");
		
//		gl.glMatrixMode(GL10.GL_MODELVIEW);
//		gl.glPushMatrix();
//		t.multGL(gl);
//
//		// get current modelview matrix
//		float[] m = new float[16];
//		gl.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, m, 0);
//
//		// get up and right vector, used to create a camera-facing quad
//		Vector3 up = new Vector3(m[1], m[5], m[9]);
//		up.normalize();
//		Vector3 right = new Vector3(m[0], m[4], m[8]);
//		right.normalize();
//
//		float size = 1;
//		Vector3 rightPlusUp = new Vector3(right);
//		rightPlusUp.add(up);
//		rightPlusUp.multiply(size);
//		Vector3 rightMinusUp = new Vector3(right);
//		rightMinusUp.subtract(up);
//		rightMinusUp.multiply(size);
//
//		Vector3 topLeft = new Vector3(rightMinusUp);
//		topLeft.multiply(-1);
//
//		Vector3 topRight = new Vector3(rightPlusUp);
//
//		Vector3 bottomLeft = new Vector3(rightPlusUp);
//		bottomLeft.multiply(-1);
//
//		Vector3 bottomRight = new Vector3(rightMinusUp);
//
//		Graphics3D.getInstance().setAppearance(getAppearance());
//		Graphics3D.getInstance().disableTextureUnits();
//		gl.glActiveTexture(GL10.GL_TEXTURE0);
//		texture.setupGL(gl, new float[] { 1, 0, 0, 0 });
//
//		// Draw sprite
//		gl.glBegin(GL10.GL_QUADS);
//
//		gl.glTexCoord2f(0, 0);
//		gl.glVertex3f(topLeft.x, topLeft.y, topLeft.z); // Top Left
//
//		gl.glTexCoord2f(0, 1);
//		gl.glVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z); // Bottom Left
//
//		gl.glTexCoord2f(1, 1);
//		gl.glVertex3f(bottomRight.x, bottomRight.y, bottomRight.z); // Bottom Right
//
//		gl.glTexCoord2f(1, 0);
//		gl.glVertex3f(topRight.x, topRight.y, topRight.z); // Top Right
//
//		gl.glEnd();
//
//		gl.glPopMatrix();
//
//		gl.glDisable(GL10.GL_TEXTURE_2D);
//
//		// HACK: for some reason, the depth write flag of other object destroyed 
//		// after rendering a sprite.
//		// this ensures that it's defaulted back to true
//		// TODO: find error and fix it!
//		gl.glDepthMask(true);
	}
}
