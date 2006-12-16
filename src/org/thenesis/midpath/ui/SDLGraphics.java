/*
 * MIDPath - Copyright (C) 2006 Guillaume Legris, Mathieu Legris
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
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */
package org.thenesis.midpath.ui;

import javax.microedition.lcdui.FontPeer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.sun.midp.log.Logging;

import sdljava.SDLException;
import sdljava.video.SDLRect;
import sdljava.video.SDLSurface;
import sdljavax.gfx.SDLGfx;

public class SDLGraphics extends Graphics {

	private SDLSurface surface;

	long sdlGfxColor;
	long sdlColor;

	SDLGraphics(SDLSurface surface) {
		this.surface = surface;
	}

	public SDLSurface getSurface() {
		return surface;
	}

	public synchronized void setColor(int red, int green, int blue) {
		super.setColor(red, green, blue);
		setInternalColor(red, green, blue);
	}

	public synchronized void setColor(int RGB) {

		super.setColor(RGB);

		int red = (RGB >> 16) & 0xff;
		int green = (RGB >> 8) & 0xff;
		int blue = RGB & 0xff;

		setInternalColor(red, green, blue);

	}
	
	public synchronized void setGrayScale(int value) {
		super.setGrayScale(value);
		setInternalColor(value, value, value);
	}

	private void setInternalColor(int red, int green, int blue) {
		// RRGGBBAA format needed by SDL_gfx
		sdlGfxColor = ((red << 24) | (green << 16) | (blue << 8) | 0xFF) & 0x00000000FFFFFFFFL;

		try {
			sdlColor = surface.mapRGB(red, green, blue);
		} catch (SDLException e) {
		}
	}

	/**
	 * Get a specific pixel value
	 *
	 * @param rgb
	 * @param gray
	 * @param isGray
	 * @return int
	 */
	protected synchronized int getPixel(int rgb, int gray, boolean isGray) {
		// TODO 
		//System.out.println("[DEBUG]SDLGraphics.getPixel : " + Integer.toHexString(rgb));
		if (isGray) {
			return (gray << 16) | (gray << 8) | gray;
		} else {
			return rgb;
		}
	}
	
	public void drawRect(int x, int y, int width, int height) {
		x += transX;
		y += transY;
		SDLGfx.rectangleColor(surface, x, y, x + width, y + height, sdlGfxColor); 
    }

	/**
	 * Fills the specified rectangle.
	 */
	public synchronized void fillRect(int x, int y, int width, int height) {

		x += transX;
		y += transY;

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG]SDLGraphics.fillRect(): x=" + x + " y1=" + y + " width=" + width + " height= "
				+ height + " color=" + Long.toHexString(sdlColor));

		//System.out.println("[DEBUG]SDLGraphics.fillRect(): " + Long.toHexString(sdlColor));

		// SDLGfx.boxColor(surface, x, y, x + width, x + height, sdlCurrentColor); // doesn't work correctly. Why ?
		try {
			surface.fillRect(new SDLRect(x, y, width, height), sdlColor); //sdlColor); 0x0000FF00L
		} catch (SDLException e) {
			e.printStackTrace();
		}

	}

	public synchronized void drawString(String str, int x, int y, int anchor) {
		
		FontPeer fontPeer = getFont().getFontPeer();
		
		if (fontPeer != null) {
			fontPeer.render(this, str, x, y, anchor);
		}

//		x += transX;
//		y += transY;
//
//		Font font = getFont(); // Font.getDefaultFont();
//
//		if ((anchor & Graphics.BOTTOM) == Graphics.BOTTOM) {
//			System.out.println("SDLGraphics.drawString(): BOTTOM");
//			y -= font.getHeight() - 1;
//		}
//
//		if ((anchor & Graphics.RIGHT) == Graphics.RIGHT) {
//			System.out.println("SDLGraphics.drawString(): RIGHT");
//			x -= font.stringWidth(str) - 1;
//		} else if ((anchor & Graphics.HCENTER) == Graphics.HCENTER) {
//			x -= font.stringWidth(str) / 2 - 1;
//		}
//
//		// TODO 
//		System.out.println("SDLGraphics.drawString(): " + str + " x=" + x + " y=" + y + " color="
//				+ Long.toHexString(sdlColor));
//		SDLGfx.stringColor(surface, x, y, str, sdlGfxColor); //0xffff00ffL
	}

	//	public void drawSubstring(String str, int offset, int len,
	//            int x, int y, int anchor) {
	//		
	//		// Reject bad arguments
	//		if (str == null) {
	//			throw new NullPointerException();
	//		}
	//		
	//		int length = str.length();
	//		if ((offset >= length) || ((offset + len) >= length)) {
	//			throw new StringIndexOutOfBoundsException();
	//		}
	//	
	//		
	//		
	//		
	//		/*StringIndexOutOfBoundsException if <code>offset</code>
	//     * and <code>length</code> do not specify
	//     * a valid range within the <code>String</code> <code>str</code>
	//     * @throws IllegalArgumentException if <code>anchor</code>
	//     * is not a legal value
	//     * @throws NullPointerException if <code>str</code> is <code>null</code>*/
	//	}


	/**
	 * Draws a line from point (x1, y1) to point (x2, y2).
	 */
	public synchronized void drawLine(int x1, int y1, int x2, int y2) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] SDLGraphics.drawLine() : x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2= " + y2
					+ " color=" + Long.toHexString(sdlGfxColor));

		x1 += transX;
		y1 += transY;
		x2 += transX;
		y2 += transY;

		if (y1 == y2) {
			SDLGfx.hlineColor(surface, x1, x2, y1, sdlGfxColor);
		} else if (x1 == x2) {
			SDLGfx.vlineColor(surface, x1, y1, y2, sdlGfxColor);
		} else {
			SDLGfx.lineColor(surface, x1, y1, x2, y2, sdlGfxColor);
		}

	}
	
	public void drawRGB(int[] rgbData, int offset, int scanlength,
            int x, int y, int width, int height,
            boolean processAlpha) {
		
		int tx = x + transX;
		int ty = y + transY;
		
		int[] buf = new int[width * height]; 
		
		for (int b = 0; b < height; b++) {
			for (int a = 0; a < width; a++) {
				buf[a + b * scanlength] = rgbData[offset + (a - tx) + (b - ty) * scanlength];
				//P(a, b) = rgbData[offset + (a - x) + (b - y) * scanlength];
			}
		}
		
		Image image = Image.createRGBImage(buf, width, height, processAlpha);
		drawImage(image, x, y, Graphics.TOP | Graphics.LEFT);
		
		// TODO 
		System.out.println("SDLGraphics.drawRGB(): not yet implemented");
	}

	protected void doCopyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor) {

		x_src += transX;
		y_src += transY;
		x_dest += transX;
		y_dest += transY;

		//SDLImage transformedImage = new SDLImage(surface, x_src,y_src, width, height, Sprite.TRANS_NONE);

		if ((anchor & Graphics.BOTTOM) == Graphics.BOTTOM) {
			y_dest -= height - 1;
		} else if ((anchor & Graphics.VCENTER) == Graphics.VCENTER) {
			y_dest -= height / 2 - 1;
		}

		if ((anchor & Graphics.RIGHT) == Graphics.RIGHT) {
			x_dest -= width - 1;
		} else if ((anchor & Graphics.HCENTER) == Graphics.HCENTER) {
			x_dest -= width / 2 - 1;
		}

		try {
			// Copy the source area in an offscreen surface
			SDLSurface dstSurface = SDLToolkit.getToolkit().createSDLSurface(width, height);
			SDLRect srcRect = new SDLRect(x_src, y_src, width, height);
			SDLRect dstRect = new SDLRect(0, 0, width, height);
			surface.blitSurface(srcRect, dstSurface, dstRect);

			// Blit the offscreen surface on the current surface
			dstSurface.blitSurface(surface, new SDLRect(x_dest, y_dest, width, height));

		} catch (SDLException e) {
			e.printStackTrace();
		}

	}

	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
		
		x1 += transX;
		y1 += transY;
		x2 += transX;
		y2 += transY;
		x3 += transX;
		y3 += transY;
		
		SDLGfx.trigonColor(surface, x1, y1, x2, y2, x3, y3, sdlGfxColor);
	}
	


}
