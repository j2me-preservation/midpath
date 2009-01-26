/*
 * MIDPath - Copyright (C) 2006-2008 Guillaume Legris, Mathieu Legris
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
 */
package org.thenesis.microbackend.ui.graphics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.thenesis.microbackend.ui.BackendEventListener;
import org.thenesis.microbackend.ui.Configuration;
import org.thenesis.microbackend.ui.Logging;
import org.thenesis.microbackend.ui.UIBackend;
import org.thenesis.microbackend.ui.UIBackendFactory;
import org.thenesis.microbackend.ui.graphics.toolkit.pure.PureToolkit;

public abstract class VirtualToolkit {
    
    protected Configuration backendConfig;
    protected BackendEventListener listener;
    
    private UIBackend backend;
    private BaseImageDecoder imageDecoder = new BaseImageDecoder();

    protected VirtualToolkit() {
    }

    public static VirtualToolkit createToolkit(Configuration backendConfig, BackendEventListener listener) { 
       VirtualToolkit toolkit = new PureToolkit();
       toolkit.configure(backendConfig, listener);
       return toolkit;
    }
    
    protected void configure(Configuration backendConfig, BackendEventListener listener) {
        this.backendConfig = backendConfig;
        this.listener = listener;
    }

    public void initialize(Object m) {
        backend = UIBackendFactory.createBackend(m, backendConfig, listener);
        
        int w = backend.getWidth();
        int h = backend.getHeight();
       
        try {
            backend.open();
        } catch (IOException e) {
            Logging.log("VirtualToolkit: Can't open '" + backend.getClass().getName() + "' backend", Logging.ERROR);
            e.printStackTrace();
        }
        
        initializeRoot(w, h);
    }
    
    /**
     * Initializes the root Surface and Graphics
     * @param rootWidth
     * @param rootHeight
     */
    protected abstract void initializeRoot(int rootWidth, int rootHeight);

    public abstract VirtualGraphics getRootGraphics();
    
    public abstract VirtualSurface getRootSurface();

    public void flushGraphics(int x, int y, long width, long height) {
        backend.updateARGBPixels(getRootSurface().getData(), x, y, (int) width, (int) height);
    }

    public abstract VirtualFont createFont(int face, int style, int size);

    public abstract VirtualFont getDefaultFont();

// 
//    public int getWidth() {
//        return backend.getWidth();
//    }
//
//    public int getHeight() {
//        return backend.getHeight();
//    }

    UIBackend getBackend() {
        return backend;
    }

    public abstract VirtualSurface createSurface(int w, int h);

    public void close() {
        backend.close();
    }
    
    public abstract VirtualImage createImage(int w, int h);
    
    public abstract VirtualImage createRGBImage(int[] rgb, int width, int height, boolean processAlpha);
    
    protected abstract VirtualImage createImage(VirtualSurface surface);

    public VirtualImage createImage(InputStream stream) {
        try {
            VirtualSurface surface = imageDecoder.decode(stream);
            return createImage(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public VirtualImage createImage(byte[] imageData, int imageOffset, int imageLength) {
        return createImage(new ByteArrayInputStream(imageData, imageOffset, imageLength));
    }

}
