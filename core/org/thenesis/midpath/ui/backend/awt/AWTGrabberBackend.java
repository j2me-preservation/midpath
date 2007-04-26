package org.thenesis.midpath.ui.backend.awt;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class AWTGrabberBackend extends AWTBackend {

	private static final int COUNTER_STRING_SIZE = 6;

	public int imageCounter = 0;
	private StringBuffer counterStringBuffer = new StringBuffer();
	//private static final float COMPRESSION_QUALITY = 1.0f;

	public AWTGrabberBackend(int w, int h) {
		super(w, h);
	}

	public void updateSurfacePixels(int x, int y, long width, long heigth) {

		super.updateSurfacePixels(x, y, width, heigth);

		imageCounter++;

		counterStringBuffer.setLength(0);
		counterStringBuffer.append(imageCounter);
		if (counterStringBuffer.length() < COUNTER_STRING_SIZE) {
			int leadingZeros = COUNTER_STRING_SIZE - counterStringBuffer.length();
			for (int i = 0; i < leadingZeros; i++) {
				counterStringBuffer.insert(0, "0");
			}
		}

		// Save image as BMP
		try {
			File file = new File(counterStringBuffer + ".bmp");
			ImageIO.write(screenImage, "bmp", file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//				// Save image as JPEG
		//				try {
		//					// Find a jpeg writer
		//					ImageWriter writer = null;
		//					Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
		//					if (iter.hasNext()) {
		//						writer = (ImageWriter) iter.next();
		//					}
		//
		//					// Prepare output file
		//					File file = new File(counterStringBuffer + ".jpg");
		//					ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		//					writer.setOutput(ios);
		//
		//					// Set the compression quality
		//					ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
		//					iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		//					iwparam.setCompressionQuality(COMPRESSION_QUALITY);
		//
		//					// Write the image
		//					writer.write(null, new IIOImage(screenImage, null, null), iwparam);
		//				} catch (IOException e) {
		//					e.printStackTrace();
		//				}

	}

}
