package org.thenesis.microbackend.ui.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.thenesis.microbackend.ui.Logging;
import org.thenesis.microbackend.ui.image.png.ColorModel;
import org.thenesis.microbackend.ui.image.png.ImageConsumer;
import org.thenesis.microbackend.ui.image.png.PngImage;

public class BaseImageDecoder {
    
    static {
        PngImage.setProgressiveDisplay(false);
    }
    
    public VirtualSurface decode(InputStream is) throws IOException {

        // Read PNG image from file
        PngImage png = new PngImage(is);

        final int pngWidth = png.getWidth();
        int pngHeight = png.getHeight();
        PngSurface surface = new PngSurface(pngWidth, pngHeight);
        final int[] surfaceData = surface.getData();
        png.setBuffer(surfaceData);

        png.startProduction(new ImageConsumer() {
            private ColorModel cm;

            public void imageComplete(int status) {
                for (int i = 0; i < surfaceData.length; i++) {
                    surfaceData[i] = cm.getRGB(surfaceData[i]);
                    //if (surface.data[i] != 0xFF000000) {
                    //    System.out.print(Integer.toHexString(surface.data[i]) + " ");
                    //}
                }
            }

            public void setColorModel(ColorModel model) {
                cm = model;
            }

            public void setDimensions(int width, int height) {
            }

            public void setHints(int flags) {
            }

            public void setProperties(Hashtable props) {
            }

            public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scansize) {
            }

            public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scansize) {
            }

        });
        
        if (Logging.TRACE_ENABLED) {
            System.out.println("[DEBUG] BaseImageDecoder.<init>(InputStream stream): errors while loading ? " + png.hasErrors());
        }
        
        return surface;

    }
    
    private class PngSurface implements VirtualSurface {

        private int[] data;
        private int width;
        private int height;
        
        public PngSurface(int w, int h) {
            this.width = w;
            this.height = h;
            data = new int[w*h];
        }
        
        public int[] getData() {
            return data;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public void lock() {
            // Do nothing
        }

        public void unlock() {
            // Do nothing
        }
        
    }

}
