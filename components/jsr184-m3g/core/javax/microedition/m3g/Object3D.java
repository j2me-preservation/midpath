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

//import java.util.ArrayList; 

public abstract class Object3D { 

	protected int userID = 0;
	protected Object userObject = null;

	//ArrayList animationTracks = new ArrayList();

	public final Object3D duplicate() {
		
		return duplicateImpl();
		
//		Object3D copy = null;
//		try {
//			copy = (Object3D) this.clone();
//		} catch (Exception e) {
//		}
//		return copy;
	}
	
	abstract Object3D duplicateImpl(); 
//	{
//		// TODO To implement
//		throw new UnsupportedOperationException("not implemented yet");
//	}

	public Object3D find(int userID) {
		// TODO: 
		return null;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException {
		return 0;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public Object getUserObject() {
		return this.userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/*
	 public void addAnimationTrack(AnimationTrack animationTrack)
	 {
	 this.animationTracks.add(animationTrack);
	 }
	 */
}
