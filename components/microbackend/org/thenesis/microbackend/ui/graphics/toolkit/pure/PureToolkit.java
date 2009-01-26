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
package org.thenesis.microbackend.ui.graphics.toolkit.pure;

import org.thenesis.microbackend.ui.graphics.VirtualFont;
import org.thenesis.microbackend.ui.graphics.VirtualGraphics;
import org.thenesis.microbackend.ui.graphics.VirtualImage;
import org.thenesis.microbackend.ui.graphics.VirtualSurface;
import org.thenesis.microbackend.ui.graphics.VirtualToolkit;

public class PureToolkit extends VirtualToolkit {

    private VirtualSurface rootSurface;
    private PureGraphics rootPeer;
    private VirtualFont defaultFont;

    public PureToolkit() {
    }

    public void initializeRoot(int rootWidth, int rootHeight) {
        rootSurface = new PureSurface(rootWidth, rootHeight);
        rootPeer = new PureGraphics(rootSurface);
        rootPeer.setDimensions(rootWidth, rootHeight);
        rootPeer.reset();
    }

    public VirtualGraphics getRootGraphics() {
        return rootPeer;
    }

    public VirtualFont createFont(int face, int style, int size) {
        return new PureFont(face, style, size);
    }

    public VirtualFont getDefaultFont() {
        if (defaultFont == null) {
            defaultFont = createFont(VirtualFont.FACE_MONOSPACE, VirtualFont.STYLE_PLAIN, VirtualFont.SIZE_LARGE);
        }
        return defaultFont;
    }

//    public void setDefaultFont(VirtualFont f) {
//        if (f != null) {
//            defaultFont = f;
//        }
//    }

    public VirtualSurface getRootSurface() {
        return rootSurface;
    }

    public VirtualSurface createSurface(int w, int h) {
        return new PureSurface(w, h);
    }

    public VirtualImage createImage(int w, int h) {
        return new PureImage(w, h);
    }

    public VirtualImage createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        return new PureImage(rgb, width, height, processAlpha);
    }

    protected VirtualImage createImage(VirtualSurface surface) {
        return new PureImage(surface);
    }


}
