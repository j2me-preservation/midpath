/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
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

package com.sun.midp.chameleon.layers;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Graphics;

import com.sun.midp.chameleon.ChamDisplayTunnel;
import com.sun.midp.chameleon.skins.AlertSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.SoftButtonSkin;
import com.sun.midp.chameleon.skins.TickerSkin;

/**
 * AlertLayer IMPL_NOTE
 */
public class AlertLayer extends BodyLayer {

    /**
     * The AlertLayer constructor. Initializes background image if 
     * there is one set in AlertSkin.
     * @param tunnel - The ChamDisplayTunnel to do paint calls
     */
    public AlertLayer(ChamDisplayTunnel tunnel) {
        super(AlertSkin.IMAGE_BG, AlertSkin.COLOR_BG, tunnel);
        setVisible(false);
        this.layerID  = "AlertLayer";
    }

    /**
     * Sets content to be displayed in the Alert Layer.
     * This AlertLayer will be made visible if  <code>alertVisible</code>
     * is true and will be hidden - otherwise.
     * @param alertVisible - true if the AlertLayer should be shown,
     *                       and false - otherwise
     * @param alert - The <code>Alert</code> instance that is currently
     *                visible
     * @param height the preferred height for the Alert. This is accepted
     *               as long as it is less than AlertSkin.HEIGHT
     */
    public void setAlert(boolean alertVisible, Alert alert, int height) {
        this.alert = alert;
        
        alignAlert(height);
        dirty = true;
        setVisible(alertVisible);
    }
    
    /**
     * Toggle the visibility state of Alert layer within its containing
     * window.
     *
     * @param visible if alert should be visible, false otherwise
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setSupportsInput(visible);
    }


    /**
     * Initialize the bounds of this AlertLayer. Overrides
     * initialize in superclasses. The dimensions of the
     * specific AlertLayer are specified in AlertSkin.
     *
     * The X and Y coordinates represent the upper left position
     * of this CLayer in the physical display's coordinate space.
     */
    protected void initialize() {
        super.initialize();
        bounds[X] = 0; // set in alignAlert()
        bounds[Y] = 0; // set in alignAlert()
        bounds[W] = AlertSkin.WIDTH;
        bounds[H] = AlertSkin.HEIGHT;
    }
        
    protected void alignAlert(int height) {
        /*
        if (height < AlertSkin.MIN_HEIGHT) {
            bounds[H] = AlertSkin.MIN_HEIGHT;
        } else if (height < AlertSkin.MAX_HEIGHT) {
            bounds[H] = height;
        }
        */
        switch (AlertSkin.ALIGN_X) {
            case Graphics.LEFT:
                bounds[X] = 0;
                break;
            case Graphics.RIGHT:
                bounds[X] = ScreenSkin.WIDTH - bounds[W];
                break;
            case Graphics.HCENTER:
            default:
                bounds[X] = (ScreenSkin.WIDTH - bounds[W]) / 2;
                break;
        }
        switch (AlertSkin.ALIGN_Y) {
            case Graphics.TOP:
                bounds[Y] = 0;
                break;
            case Graphics.VCENTER:
                bounds[Y] = (ScreenSkin.HEIGHT - SoftButtonSkin.HEIGHT -
                    bounds[H]) / 2;
                if (alert != null && alert.getTicker() != null) {
                    bounds[Y] -= TickerSkin.HEIGHT;
                }
                break;
            case Graphics.BOTTOM:
            default:
                bounds[Y] = ScreenSkin.HEIGHT - SoftButtonSkin.HEIGHT -
                    bounds[H];
                if (alert != null && alert.getTicker() != null) {
                    bounds[Y] -= TickerSkin.HEIGHT;
                }
                break;
        }
    }

    /** The Alert instance which content is currently visible */
    private Alert alert;
}

