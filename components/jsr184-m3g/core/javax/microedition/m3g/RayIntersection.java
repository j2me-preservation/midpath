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

public class RayIntersection {
	
	private float distance = 0.0f;
	private int submeshIndex = 0;
	private float textureS = 0.0f;
	private float textureT = 0.0f;
	private float normalX = 0.0f;
	private float normalY = 0.0f;
	private float normalZ = 1.0f;
	private float[] ray;
	
	Node intersectionNode = null;
	
	public RayIntersection() {
		ray = new float[6];
		ray[5] = 1.0f;
	}
	
	public Node getIntersected() {
		return intersectionNode;
	}
	
	public void getRay(float[] ray) {
		if (ray == null) {
			throw new NullPointerException();
		}
	    if (ray.length < 6) {
	    	throw new IllegalArgumentException("Float array size must be 6 at least");
	    }
	    
	    System.arraycopy(this.ray, 0, ray, 0, 6);
	}
	
	public float getDistance() {
		// TODO
		//float pickRayLenght = (float)Math.sqrt(ray[3] * ray[3] + ray[4] * ray[4] + ray[5] * ray[5]);
		return distance;
	}
	public float getNormalX() {
		// TODO
		return normalX;
	}
	public float getNormalY() {
		// TODO
		return normalY;
	}
	public float getNormalZ() {
		//	TODO
		return normalZ;
	}
	public int getSubmeshIndex() {
		// TODO
		return submeshIndex;
	}
	public float getTextureS() {
		// TODO
		return textureS;
	}
	public float getTextureT() {
		// TODO
		return textureT;
	}
	
}
