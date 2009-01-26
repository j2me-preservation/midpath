package org.thenesis.microbackend.ui.graphics.toolkit.pure;

import org.thenesis.microbackend.ui.graphics.VirtualSurface;

public class PureSurface implements VirtualSurface {
    
    private int width;
    private int height;
    private int[] data;
    
    PureSurface() {
    }
    
    PureSurface(int w, int h) {
        data = new int[w * h];
        this.width = w;
        this.height = h;
    }
    
    public void lock() { }
    public void unlock() { }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int[] getData() {
        return data;
    }

    
}
