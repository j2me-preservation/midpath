package org.thenesis.microbackend.ui;

import java.io.IOException;

public class NullBackend implements UIBackend {

	public NullBackend() {
		// Do nothing
	}

	public void close() {
		// Do nothing
	}

	public void initialize(Configuration conf, int width, int height) {
		//	Do nothing
	}

	public void open() throws IOException {
		//	Do nothing
	}

	public void setBackendEventListener(BackendEventListener listener) {
		//	Do nothing
	}

	public void updateARGBPixels(int[] argbPixels, int x, int y, int widht, int heigth) {
		//	Do nothing
	}

}
