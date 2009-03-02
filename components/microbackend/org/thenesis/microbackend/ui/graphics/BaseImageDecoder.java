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
    
    private VirtualToolkit toolkit;
    
    public BaseImageDecoder(VirtualToolkit toolkit) {
        this.toolkit = toolkit;
    }
    
    public VirtualImage decode(InputStream is) throws IOException {

        // Read PNG image from file
        PngImage png = new PngImage(is);

        final int pngWidth = png.getWidth();
        int pngHeight = png.getHeight();
        final int[] pngData = new int[pngWidth * pngHeight];
        png.setBuffer(pngData);

        png.startProduction(new ImageConsumer() {
            private ColorModel cm;

            public void imageComplete(int status) {
                for (int i = 0; i < pngData.length; i++) {
                    pngData[i] = cm.getRGB(pngData[i]);
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
        
        return toolkit.createRGBImage(pngData, pngWidth, pngHeight, true);

    }
    

}
