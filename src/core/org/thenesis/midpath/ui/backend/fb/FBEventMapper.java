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
package org.thenesis.midpath.ui.backend.fb;

import javax.microedition.lcdui.Canvas;

import com.sun.midp.configurator.Constants;
import com.sun.midp.events.EventMapper;
import com.sun.midp.lcdui.EventConstants;

public class FBEventMapper implements EventMapper {

	public int getGameAction(int keyCode) {
		switch (keyCode) {
		case FBCanvas.FB_K_F3:
			return Canvas.GAME_A;
		case FBCanvas.FB_K_F4:
			return Canvas.GAME_B;
		case FBCanvas.FB_K_F5:
			return Canvas.GAME_C;
		case FBCanvas.FB_K_F6:
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
			return FBCanvas.FB_K_F3;
		case Canvas.GAME_B:
			return FBCanvas.FB_K_F4;
		case Canvas.GAME_C:
			return FBCanvas.FB_K_F5;
		case Canvas.GAME_D:
			return FBCanvas.FB_K_F6;
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
		case FBCanvas.FB_K_DELETE:
			return EventConstants.SYSTEM_KEY_CLEAR;
		case FBCanvas.FB_K_END:
			return EventConstants.SYSTEM_KEY_END;
		case FBCanvas.FB_K_F12:
			return EventConstants.SYSTEM_KEY_POWER;
		case FBCanvas.FB_K_RETURN:
		case FBCanvas.FB_K_ENTER:   
			return EventConstants.SYSTEM_KEY_SEND;
		default:
			return 0;
		}
		
	}

	static int mapToInternalEvent(char c) {

		switch (c) {
		case FBCanvas.FB_K_DOWN:
			return Constants.KEYCODE_DOWN;
		case FBCanvas.FB_K_LEFT:
			return Constants.KEYCODE_LEFT;
		case FBCanvas.FB_K_RIGHT:
			return Constants.KEYCODE_RIGHT;
		case FBCanvas.FB_K_UP:
			return Constants.KEYCODE_UP;
		case FBCanvas.FB_K_ENTER:
		case FBCanvas.FB_K_RETURN:
			return Constants.KEYCODE_SELECT;
		case FBCanvas.FB_K_F1:
			return EventConstants.SOFT_BUTTON1;
		case FBCanvas.FB_K_F2:
			return EventConstants.SOFT_BUTTON2;
		case FBCanvas.FB_K_DELETE:
			return FBCanvas.FB_K_DELETE;
		case FBCanvas.FB_K_END:
			return FBCanvas.FB_K_END;
		case FBCanvas.FB_K_F12:
			return FBCanvas.FB_K_F12;
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
