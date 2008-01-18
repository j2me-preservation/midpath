package com.sun.perseus;

import java.io.InputStream;

import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import com.sun.perseus.j2d.ImageLoaderUtil;
import com.sun.perseus.platform.GZIPSupport;

public abstract class PerseusToolkit {

	private static PerseusToolkit instance;
	
	static {
		String toolkit = System.getProperty("jsr226.toolkit");
		if (toolkit == null) { // Default
			toolkit = "org.thenesis.midpath.svg.midp.MIDPPerseusToolkit";
		}
		try {
			Class toolkitClass = Class.forName(toolkit);
			instance = (PerseusToolkit) toolkitClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Error : No toolkit found for JSR226 (SVG)");
		}
		
	}

	protected PerseusToolkit() {
	}

	public static PerseusToolkit getInstance() {
		return instance;
	}

	public abstract ImageLoaderUtil createImageLoaderUtil();

	public abstract SVGAnimator createAnimator(SVGImage svgImage);

	public abstract SVGAnimator createAnimator(SVGImage svgImage, String componentBaseClass);

	public abstract GZIPSupport getGZIPSupport();

	public abstract InputStream getInitialFontResource();

	public abstract InputStream getDefaultFontResource();

	public abstract String getConfigurationProperty(String s);

	public abstract ScalableGraphics createScalableGraphics();

}
