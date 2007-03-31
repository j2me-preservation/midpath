package org.thenesis.midpath.ui.virtual;

public class VirtualSurface {
	
	public int width;
	public int height;
	
	public int[] data;
	
	public void lock() { }
	public void unlock() { }
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	

}
