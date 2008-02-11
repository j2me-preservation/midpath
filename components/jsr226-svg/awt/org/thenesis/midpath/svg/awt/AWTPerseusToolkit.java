package org.thenesis.midpath.svg.awt;

import java.io.InputStream;

import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import com.sun.perseus.PerseusToolkit;
import com.sun.perseus.j2d.ImageLoaderUtil;
import com.sun.perseus.platform.GZIPSupport;

public class AWTPerseusToolkit extends PerseusToolkit {
	
	private GZIPSupportImpl gSupportImpl = new GZIPSupportImpl();
	
	public ImageLoaderUtil createImageLoaderUtil() {
		return new ImageLoaderUtilImpl();
	}
	
	public SVGAnimator createAnimator(SVGImage svgImage) {
		return SVGAnimatorImpl.createAnimator(svgImage, null);
	}
	
	public SVGAnimator createAnimator(SVGImage svgImage,
             String componentBaseClass) {
		return SVGAnimatorImpl.createAnimator(svgImage, componentBaseClass);
	}
	
	public GZIPSupport getGZIPSupport() {
		return gSupportImpl;
	}
	
	public InputStream getInitialFontResource() {
		return ResourceHandler.getInitialFontResource();
	}
	
	public InputStream getDefaultFontResource() {
		return ResourceHandler.getDefaultFontResource();
	}
	
	public String getConfigurationProperty(String s) {
		return System.getProperty(s);
	}
	
	 public ScalableGraphics createScalableGraphics() {
		 return new ScalableGraphicsImpl();
	 }

}
