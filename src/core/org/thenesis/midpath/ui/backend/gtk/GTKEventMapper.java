/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
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
package org.thenesis.midpath.ui.backend.gtk;

import javax.microedition.lcdui.Canvas;

import com.sun.midp.configurator.Constants;
import com.sun.midp.events.EventMapper;
import com.sun.midp.lcdui.EventConstants;

public class GTKEventMapper implements EventMapper {

	public int getGameAction(int keyCode) {
		switch (keyCode) {
		case GTKCanvas.GDK_F3:
			return Canvas.GAME_A;
		case GTKCanvas.GDK_F4:
			return Canvas.GAME_B;
		case GTKCanvas.GDK_F5:
			return Canvas.GAME_C;
		case GTKCanvas.GDK_F6:
			return Canvas.GAME_D;
		case Constants.KEYCODE_SELECT:
			return Canvas.FIRE;
		case Constants.KEYCODE_DOWN:
			return Canvas.DOWN;
		case Constants.KEYCODE_LEFT:
			return Canvas.LEFT;
		case Constants.KEYCODE_RIGHT:
			return Canvas.RIGHT;
		case Constants.KEYCODE_UP:
			return Canvas.UP;
		default:
			return -1;
		}
	}

	public int getKeyCode(int gameAction) {
		switch (gameAction) {
		case Canvas.GAME_A:
			return GTKCanvas.GDK_F3;
		case Canvas.GAME_B:
			return GTKCanvas.GDK_F4;
		case Canvas.GAME_C:
			return GTKCanvas.GDK_F5;
		case Canvas.GAME_D:
			return GTKCanvas.GDK_F6;
		case Canvas.FIRE:
			return Constants.KEYCODE_SELECT;
		case Canvas.DOWN:
			return Constants.KEYCODE_DOWN;
		case Canvas.LEFT:
			return Constants.KEYCODE_LEFT;
		case Canvas.RIGHT:
			return Constants.KEYCODE_RIGHT;
		case Canvas.UP:
			return Constants.KEYCODE_UP;
		default:
			return 0;
		}
	}

	public String getKeyName(int keyCode) {
		// TODO
		return "";
	}

	public int getSystemKey(int keyCode) {
		switch (keyCode) {
		case GTKCanvas.GDK_BackSpace:
		case GTKCanvas.GDK_Delete:
			return EventConstants.SYSTEM_KEY_CLEAR;
		case GTKCanvas.GDK_End:
			return EventConstants.SYSTEM_KEY_END;
		case GTKCanvas.GDK_F12:
			return EventConstants.SYSTEM_KEY_POWER;
		case GTKCanvas.GDK_cr:
		case GTKCanvas.GDK_Return:   
			return EventConstants.SYSTEM_KEY_SEND;
		default:
			return 0;
		}
	}

	static int mapToInternalEvent(int keyCode, char c) {
		
		switch (keyCode) {
		case GTKCanvas.GDK_Down:
			return Constants.KEYCODE_DOWN;
		case GTKCanvas.GDK_Left:
			return Constants.KEYCODE_LEFT;
		case GTKCanvas.GDK_Right:
			return Constants.KEYCODE_RIGHT;
		case GTKCanvas.GDK_Up:
			return Constants.KEYCODE_UP;
		case GTKCanvas.GDK_cr:
		case GTKCanvas.GDK_Return:
			return Constants.KEYCODE_SELECT;
		case GTKCanvas.GDK_F1:
			return EventConstants.SOFT_BUTTON1;
		case GTKCanvas.GDK_F2:
			return EventConstants.SOFT_BUTTON2;
		case GTKCanvas.GDK_KP_1: 
			return Canvas.KEY_NUM1;
		case GTKCanvas.GDK_KP_2:
			return Canvas.KEY_NUM2;
		case GTKCanvas.GDK_KP_3:
			return Canvas.KEY_NUM3;
		case GTKCanvas.GDK_KP_4:
			return Canvas.KEY_NUM4;
		case GTKCanvas.GDK_KP_5:
			return Canvas.KEY_NUM5;
		case GTKCanvas.GDK_KP_6:
			return Canvas.KEY_NUM6;
		case GTKCanvas.GDK_KP_7:
			return Canvas.KEY_NUM7;
		case GTKCanvas.GDK_KP_8:
			return Canvas.KEY_NUM8;
		case GTKCanvas.GDK_KP_9:
			return Canvas.KEY_NUM9;
		case GTKCanvas.GDK_KP_0:
			return Canvas.KEY_NUM0;
		case GTKCanvas.GDK_asterisk:
			return Canvas.KEY_STAR;
		case GTKCanvas.GDK_BackSpace:
		case GTKCanvas.GDK_Delete:
			return GTKCanvas.GDK_Delete;
		case GTKCanvas.GDK_End:
			return GTKCanvas.GDK_End;
		case GTKCanvas.GDK_F12:
			return GTKCanvas.GDK_F12;
		}

		switch (c) {
		case '1':
			return Canvas.KEY_NUM1;
		case '2':
			return Canvas.KEY_NUM2;
		case '3':
			return Canvas.KEY_NUM3;
		case '4':
			return Canvas.KEY_NUM4;
		case '5':
			return Canvas.KEY_NUM5;
		case '6':
			return Canvas.KEY_NUM6;
		case '7':
			return Canvas.KEY_NUM7;
		case '8':
			return Canvas.KEY_NUM8;
		case '9':
			return Canvas.KEY_NUM9;
		case '0':
			return Canvas.KEY_NUM0;
		case '*':
			return Canvas.KEY_STAR;
		case '#':
			return Canvas.KEY_POUND;
		default:
			return 0;
		}
		

	}

}
