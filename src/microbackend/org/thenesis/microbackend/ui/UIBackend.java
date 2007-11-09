package org.thenesis.microbackend.ui;

import java.io.IOException;

public interface UIBackend {
	
	public void initialize(Configuration conf, int width, int height);
	public void setBackendEventListener(BackendEventListener listener);
	public void open() throws IOException;
	public void updateARGBPixels(int[] argbPixels, int x, int y, int widht, int heigth);
	public void close();

}
