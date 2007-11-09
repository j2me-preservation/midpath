package org.thenesis.microbackend.ui;

public interface BackendEventListener {
	
	public void keyPressed(int keycode, char c, int modifiers);
	public void keyReleased(int keycode, char c, int modifiers);
	
	public void mouseMoved(int x, int y, int modifiers);
	public void mousePressed(int x, int y, int modifiers);
	public void mouseReleased(int x, int y, int modifiers);
	
	public void windowClosed();


}
