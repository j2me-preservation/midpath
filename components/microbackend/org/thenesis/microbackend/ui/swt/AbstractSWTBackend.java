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
package org.thenesis.microbackend.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.thenesis.microbackend.ui.BackendEventListener;
import org.thenesis.microbackend.ui.KeyConstants;
import org.thenesis.microbackend.ui.NullBackendEventListener;
import org.thenesis.microbackend.ui.UIBackend;

/**
 * TODO: Handle modifiers and shift/alt/control key codes
 */
public abstract class AbstractSWTBackend implements UIBackend, KeyListener, MouseListener, MouseMoveListener, DisposeListener {

    protected ImageData imageData;
    protected Image image;
    protected GC gc;
    protected Runnable painterRunnable;
    protected Display display;

    protected int canvasWidth;
    protected int canvasHeight;

    protected BackendEventListener listener = new NullBackendEventListener();


    public void setBackendEventListener(BackendEventListener listener) {
        this.listener = listener;
    }

    public void updateARGBPixels(int[] argbPixels, int x, int y, int width, int height) {

        //System.out.println("[DEBUG] SWTBackend.updateSurfacePixels(): " + x + " " + y + " " + width + " " + height);	

        //		for (int j = 0; j < canvasHeight; j++) {
        //			imageData.setPixels(0, j, canvasWidth, argbPixels, canvasWidth * j);
        //		}

        for (int j = 0; j < height; j++) {
            imageData.setPixels(x, y + j, width, argbPixels, canvasWidth * (y + j) + x);
        }

        if (display != null) {
            display.syncExec(painterRunnable);
        }

    }

    public int getWidth() {
        return canvasWidth;
    }

    public int getHeight() {
        return canvasHeight;
    }
    
    protected void configureCanvas(Canvas canvas) {
        PaletteData palette = new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF);
        imageData = new ImageData(canvasWidth, canvasHeight, 32, palette);
        gc = new GC(canvas);

        painterRunnable = new Runnable() {
            public void run() {
                image = new Image(display, imageData);
                gc.drawImage(image, 0, 0);
                image.dispose();
            }
        };

        canvas.addKeyListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseMoveListener(this);
        canvas.addDisposeListener(this);
    }


    public void keyPressed(KeyEvent e) {
        //System.out.println("[DEBUG] SWTBackend.keyPressed(): key code: " + e.keyCode + " char: " + e.character);
        char c = e.character;
        int keyCode = e.keyCode;

        if (c == 0) {
            listener.keyPressed(convertKeyCode(keyCode), KeyConstants.CHAR_UNDEFINED, 0);
        } else {
            listener.keyPressed(convertCharToKeyCode(c), c, 0);
        }
    }

    public void keyReleased(KeyEvent e) {
        //System.out.println("[DEBUG] SWTBackend.keyReleased(): key code: " + e.keyCode + " char: " + e.character);
        char c = e.character;
        int keyCode = e.keyCode;

        if (c == 0) {
            listener.keyReleased(convertKeyCode(keyCode), KeyConstants.CHAR_UNDEFINED, 0);
        } else {
            listener.keyReleased(convertCharToKeyCode(c), c, 0);
        }
    }

    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public void mouseDoubleClick(MouseEvent arg0) {
        // Not used
    }

    public void mouseDown(MouseEvent e) {
        //System.out.println("[DEBUG] SWTBackend.mouseDown()");
        listener.mousePressed(e.x, e.y, 0);
    }

    public void mouseUp(MouseEvent e) {
        //System.out.println("[DEBUG] SWTBackend.mouseUp()");
        listener.mouseReleased(e.x, e.y, 0);
    }

    public void mouseMove(MouseEvent e) {
        //System.out.println("[DEBUG] SWTBackend.mouseDragged(): " + dragEnabled);
        listener.mouseMoved(e.x, e.y, 0);
    }

    public void widgetDisposed(DisposeEvent e) {
        listener.windowClosed();
    }

    protected static int convertKeyCode(int keyCode) {

        // First check if it's a SWT key code
        switch (keyCode) {
        case SWT.ARROW_UP:
            return KeyConstants.VK_UP;
        case SWT.ARROW_DOWN:
            return KeyConstants.VK_DOWN;
        case SWT.ARROW_LEFT:
            return KeyConstants.VK_LEFT;
        case SWT.ARROW_RIGHT:
            return KeyConstants.VK_RIGHT;
        case SWT.PAGE_UP:
            return KeyConstants.VK_PAGE_UP;
        case SWT.PAGE_DOWN:
            return KeyConstants.VK_PAGE_DOWN;
        case SWT.HOME:
            return KeyConstants.VK_HOME;
        case SWT.END:
            return KeyConstants.VK_END;
        case SWT.INSERT:
            return KeyConstants.VK_INSERT;
        case SWT.F1:
            return KeyConstants.VK_F1;
        case SWT.F2:
            return KeyConstants.VK_F2;
        case SWT.F3:
            return KeyConstants.VK_F3;
        case SWT.F4:
            return KeyConstants.VK_F4;
        case SWT.F5:
            return KeyConstants.VK_F5;
        case SWT.F6:
            return KeyConstants.VK_F6;
        case SWT.F7:
            return KeyConstants.VK_F7;
        case SWT.F8:
            return KeyConstants.VK_F8;
        case SWT.F9:
            return KeyConstants.VK_F9;
        case SWT.F10:
            return KeyConstants.VK_F10;
        case SWT.F11:
            return KeyConstants.VK_F11;
        case SWT.F12:
            return KeyConstants.VK_F12;
        case SWT.F13:
            return KeyConstants.VK_F13;
        case SWT.F14:
            return KeyConstants.VK_F14;
        case SWT.F15:
            return KeyConstants.VK_F15;
        case SWT.KEYPAD_MULTIPLY:
            return KeyConstants.VK_MULTIPLY;
        case SWT.KEYPAD_ADD:
            return KeyConstants.VK_ADD;
        case SWT.KEYPAD_SUBTRACT:
            return KeyConstants.VK_SUBTRACT;
        case SWT.KEYPAD_DECIMAL:
            return KeyConstants.VK_DECIMAL;
        case SWT.KEYPAD_DIVIDE:
            return KeyConstants.VK_DIVIDE;
        case SWT.KEYPAD_0:
            return KeyConstants.VK_NUMPAD0;
        case SWT.KEYPAD_1:
            return KeyConstants.VK_NUMPAD1;
        case SWT.KEYPAD_2:
            return KeyConstants.VK_NUMPAD2;
        case SWT.KEYPAD_3:
            return KeyConstants.VK_NUMPAD3;
        case SWT.KEYPAD_4:
            return KeyConstants.VK_NUMPAD4;
        case SWT.KEYPAD_5:
            return KeyConstants.VK_NUMPAD5;
        case SWT.KEYPAD_6:
            return KeyConstants.VK_NUMPAD6;
        case SWT.KEYPAD_7:
            return KeyConstants.VK_NUMPAD7;
        case SWT.KEYPAD_8:
            return KeyConstants.VK_NUMPAD8;
        case SWT.KEYPAD_9:
            return KeyConstants.VK_NUMPAD9;
        case SWT.KEYPAD_EQUAL:
            return KeyConstants.VK_EQUALS;
        case SWT.KEYPAD_CR:
            return KeyConstants.VK_ENTER;
        case SWT.HELP:
            return KeyConstants.VK_HELP;
        case SWT.CAPS_LOCK:
            return KeyConstants.VK_CAPS_LOCK;
        case SWT.NUM_LOCK:
            return KeyConstants.VK_NUM_LOCK;
        case SWT.SCROLL_LOCK:
            return KeyConstants.VK_SCROLL_LOCK;
        case SWT.PAUSE:
            return KeyConstants.VK_PAUSE;
        case SWT.BREAK:
            return KeyConstants.VK_UNDEFINED;
        case SWT.PRINT_SCREEN:
            return KeyConstants.VK_PRINTSCREEN;
        default:
            return KeyConstants.VK_UNDEFINED;
        }
    }

    protected static int convertCharToKeyCode(char c) {

        if (((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z'))) {
            return c;
        }

        if (((c >= 'a') && (c <= 'z'))) {
            return c - 0x20;
        }

        switch (c) {
        case '\r': 
            return KeyConstants.VK_ENTER;
        case '@':
            return KeyConstants.VK_AT;
        case '`':
            return KeyConstants.VK_BACK_QUOTE;
        case '\\':
            return KeyConstants.VK_BACK_SLASH;
        case '^':
            return KeyConstants.VK_CIRCUMFLEX;
        case ']':
            return KeyConstants.VK_CLOSE_BRACKET;
        case ':':
            return KeyConstants.VK_COLON;
        case ',':
            return KeyConstants.VK_COMMA;
        case '$':
            return KeyConstants.VK_DOLLAR;
        case '=':
            return KeyConstants.VK_EQUALS;
        case '!':
            return KeyConstants.VK_EXCLAMATION_MARK;
        case '(':
            return KeyConstants.VK_LEFT_PARENTHESIS;
        case '-':
            return KeyConstants.VK_MINUS;
        case '*':
            return KeyConstants.VK_MULTIPLY;
        case '#':
            return KeyConstants.VK_NUMBER_SIGN;
        case '[':
            return KeyConstants.VK_OPEN_BRACKET;
        case '.':
            return KeyConstants.VK_PERIOD;
        case '+':
            return KeyConstants.VK_PLUS;
        case ')':
            return KeyConstants.VK_RIGHT_PARENTHESIS;
        case ';':
            return KeyConstants.VK_SEMICOLON;
        case '/':
            return KeyConstants.VK_SLASH;
        case '_':
            return KeyConstants.VK_UNDERSCORE;
        default:
            return KeyConstants.CHAR_UNDEFINED;
        }
    }

}
