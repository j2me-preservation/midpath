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

package org.thenesis.microbackend.ui;

public interface KeyConstants {

	/* Same keycodes than AWT */ 
	public static final int KEY_FIRST = 400, KEY_LAST = 402, KEY_TYPED = 400, KEY_PRESSED = 401, KEY_RELEASED = 402,
			VK_ENTER = '\n', VK_BACK_SPACE = '\b', VK_TAB = '\t', VK_CANCEL = 3, VK_CLEAR = 12, VK_SHIFT = 16,
			VK_CONTROL = 17, VK_ALT = 18, VK_PAUSE = 19, VK_CAPS_LOCK = 20, VK_ESCAPE = 27, VK_SPACE = ' ',
			VK_PAGE_UP = 33, VK_PAGE_DOWN = 34, VK_END = 35, VK_HOME = 36, VK_LEFT = 37, VK_UP = 38, VK_RIGHT = 39,
			VK_DOWN = 40, VK_COMMA = ',', VK_MINUS = '-', VK_PERIOD = '.', VK_SLASH = '/', VK_0 = '0', VK_1 = '1',
			VK_2 = '2', VK_3 = '3', VK_4 = '4', VK_5 = '5', VK_6 = '6', VK_7 = '7', VK_8 = '8', VK_9 = '9',
			VK_SEMICOLON = ',', VK_EQUALS = '=', VK_A = 'A', VK_B = 'B', VK_C = 'C', VK_D = 'D', VK_E = 'E',
			VK_F = 'F', VK_G = 'G', VK_H = 'H', VK_I = 'I', VK_J = 'J', VK_K = 'K', VK_L = 'L', VK_M = 'M', VK_N = 'N',
			VK_O = 'O', VK_P = 'P', VK_Q = 'Q', VK_R = 'R', VK_S = 'S', VK_T = 'T', VK_U = 'U', VK_V = 'V', VK_W = 'W',
			VK_X = 'X', VK_Y = 'Y', VK_Z = 'Z', VK_OPEN_BRACKET = '[', VK_BACK_SLASH = '\\', VK_CLOSE_BRACKET = ']',
			VK_NUMPAD0 = 96, VK_NUMPAD1 = 97, VK_NUMPAD2 = 98, VK_NUMPAD3 = 99, VK_NUMPAD4 = 100, VK_NUMPAD5 = 101,
			VK_NUMPAD6 = 102, VK_NUMPAD7 = 103, VK_NUMPAD8 = 104, VK_NUMPAD9 = 105, VK_MULTIPLY = 106, VK_ADD = 107,
			VK_SEPARATER = 108, VK_SEPARATOR = 108, VK_SUBTRACT = 109, VK_DECIMAL = 110, VK_DIVIDE = 111,
			VK_DELETE = 127, VK_NUM_LOCK = 144, VK_SCROLL_LOCK = 145, VK_F1 = 112, VK_F2 = 113, VK_F3 = 114,
			VK_F4 = 115, VK_F5 = 116, VK_F6 = 117, VK_F7 = 118, VK_F8 = 119, VK_F9 = 120, VK_F10 = 121, VK_F11 = 122,
			VK_F12 = 123, VK_F13 = 61440, VK_F14 = 61441, VK_F15 = 61442, VK_F16 = 61443, VK_F17 = 61444,
			VK_F18 = 61445, VK_F19 = 61446, VK_F20 = 61447, VK_F21 = 61448, VK_F22 = 61449, VK_F23 = 61450,
			VK_F24 = 61451, VK_PRINTSCREEN = 154, VK_INSERT = 155, VK_HELP = 156, VK_META = 157, VK_BACK_QUOTE = 192,
			VK_QUOTE = 222, VK_KP_UP = 224, VK_KP_DOWN = 225, VK_KP_LEFT = 226, VK_KP_RIGHT = 227, VK_DEAD_GRAVE = 128,
			VK_DEAD_ACUTE = 129, VK_DEAD_CIRCUMFLEX = 130, VK_DEAD_TILDE = 131, VK_DEAD_MACRON = 132,
			VK_DEAD_BREVE = 133, VK_DEAD_ABOVEDOT = 134, VK_DEAD_DIAERESIS = 135, VK_DEAD_ABOVERING = 136,
			VK_DEAD_DOUBLEACUTE = 137, VK_DEAD_CARON = 138, VK_DEAD_CEDILLA = 139, VK_DEAD_OGONEK = 140,
			VK_DEAD_IOTA = 141, VK_DEAD_VOICED_SOUND = 142, VK_DEAD_SEMIVOICED_SOUND = 143, VK_AMPERSAND = 150,
			VK_ASTERISK = 151, VK_QUOTEDBL = 152, VK_LESS = 153, VK_GREATER = 160, VK_BRACELEFT = 161,
			VK_BRACERIGHT = 162, VK_AT = 512, VK_COLON = 513, VK_CIRCUMFLEX = 514, VK_DOLLAR = 515, VK_EURO_SIGN = 516,
			VK_EXCLAMATION_MARK = 517, VK_INVERTED_EXCLAMATION_MARK = 518, VK_LEFT_PARENTHESIS = 519,
			VK_NUMBER_SIGN = 520, VK_PLUS = 521, VK_RIGHT_PARENTHESIS = 522, VK_UNDERSCORE = 523, VK_FINAL = 24,
			VK_CONVERT = 28, VK_NONCONVERT = 29, VK_ACCEPT = 30, VK_MODECHANGE = 31, VK_KANA = 21, VK_KANJI = 25,
			VK_ALPHANUMERIC = 240, VK_KATAKANA = 241, VK_HIRAGANA = 242, VK_FULL_WIDTH = 243, VK_HALF_WIDTH = 244,
			VK_ROMAN_CHARACTERS = 245, VK_ALL_CANDIDATES = 256, VK_PREVIOUS_CANDIDATE = 257, VK_CODE_INPUT = 258,
			VK_JAPANESE_KATAKANA = 259, VK_JAPANESE_HIRAGANA = 260, VK_JAPANESE_ROMAN = 261, VK_KANA_LOCK = 262,
			VK_INPUT_METHOD_ON_OFF = 263, VK_CUT = 65489, VK_COPY = 65485, VK_PASTE = 65487, VK_UNDO = 65483,
			VK_AGAIN = 65481, VK_FIND = 65488, VK_PROPS = 65482, VK_STOP = 65480, VK_COMPOSE = 65312,
			VK_ALT_GRAPH = 65406, VK_BEGIN = 65368, VK_CONTEXT_MENU = 525, VK_WINDOWS = 524, VK_UNDEFINED = 0;

	public static final char CHAR_UNDEFINED = '\uffff';

}
