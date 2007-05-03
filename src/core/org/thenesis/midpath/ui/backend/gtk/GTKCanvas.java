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

import java.util.Vector;

import com.sun.cldchi.jvm.JVM;

public class GTKCanvas {

	static {
		JVM.loadLibrary("libgtkcanvas.so");
		//System.loadLibrary("gtkcanvas");
	}

	private int width;
	private int height;
	private GTKThread gtkThread;

	public GTKCanvas(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void start() {

		// Create a dedicated thread for the GTK main loop
		gtkThread = new GTKThread();
		gtkThread.start();

		// Initialize GTK.
		gtkThread.addToQueue(new Runnable() {
			public void run() {
				initialize(width, height);
			}
		});

	}
	
	public void stop() {
		gtkThread.stop();
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		int width = 200;
//		int height = 200;
//
//		int[] buffer = new int[width * height];
//		for (int i = 0; i < buffer.length; i++) {
//			buffer[i] = 0xFFFF0000;
//		}
//
//		GTKCanvas test = new GTKCanvas(width, height);
//		test.start();
//		//test.updateARGBPixels(buffer, 50, 50, 50, 50);
//
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		for (int i = 0; i < 100; i++) {
//			test.updateARGBPixels(buffer, i, i, 50, 50);
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//
//	}

	/**
	 * The GTK thread
	 */
	class GTKThread implements Runnable {

		private volatile Thread thread;
		private Vector repaintQueue = new Vector();

		public void start() {
			thread = new Thread(GTKThread.this);
			thread.start();
		}
		
		public void stop() {
			thread = null;
		}

		public void run() {

			// Start GTK loop
			while (Thread.currentThread() == thread) {
				synchronized (repaintQueue) {
					processQueue();
					gtkMainIterationDo();
				}

				Thread.yield();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}

		} // run()

		private void processQueue() {
			if (!repaintQueue.isEmpty()) {
				int size = repaintQueue.size();
				for (int i = 0; i < size; i++) {
					Runnable r = (Runnable) repaintQueue.elementAt(size - 1);
					r.run();
				}
				repaintQueue.removeAllElements();
				repaintQueue.notify();
			}
		}

		public void addToQueue(Runnable r) {
			// Block until queue is empty
			synchronized (repaintQueue) {
				if (repaintQueue.size() > 0) {
					try {
						repaintQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				repaintQueue.addElement(r);
			}
		}
	}

	public void updateARGBPixels(final int[] argbBuffer, final int x_src, final int y_src, final int width,
			final int height) {

		//writeARGB(argbBuffer, x_src, y_src, width, height);

		gtkThread.addToQueue(new Runnable() {
			public void run() {
				writeARGB(argbBuffer, x_src, y_src, width, height);
			}
		});

	}
	
	/* Event callback methods. Inherited classes should override them. */
	
	public void onKeyPressed(int eventType, int keyCode, int unicode) {
		if (eventType == GDK_KEY_PRESS) {
			System.out.println("key pressed: " + keyCode + " " + ((char) unicode));
		} else {
			System.out.println("key released: " + keyCode + " " + ((char) unicode));
		}
	}

	public void onMotionEvent(int x, int y, int state) {
		System.out.println("motion event: " + x + " " + y);
	}

	public void onButtonEvent(int eventType, int x, int y, int state) {
		if (eventType == GDK_BUTTON_PRESS) {
			System.out.println("button pressed: " + x + " " + y);
		} else {
			System.out.println("button released: " + x + " " + y);
		}
	}
	
	
	/* Native methods */

	native public int initialize(int width, int height);

	//native public int isMainLoopStarted(); 
	
	native private void writeARGB(int[] argbBuffer, int x_src, int y_src, int width, int height);

	native public int gtkMainIterationDo();

	native public int destroy();
	
	/* GDK constants */
	
	/* Grabbed from GdkEventType */
	public static final int GDK_NOTHING = -1, GDK_DELETE = 0, GDK_DESTROY = 1, GDK_EXPOSE = 2, GDK_MOTION_NOTIFY = 3,
			GDK_BUTTON_PRESS = 4, GDK_2BUTTON_PRESS = 5, GDK_3BUTTON_PRESS = 6, GDK_BUTTON_RELEASE = 7,
			GDK_KEY_PRESS = 8, GDK_KEY_RELEASE = 9, GDK_ENTER_NOTIFY = 10, GDK_LEAVE_NOTIFY = 11,
			GDK_FOCUS_CHANGE = 12, GDK_CONFIGURE = 13, GDK_MAP = 14, GDK_UNMAP = 15, GDK_PROPERTY_NOTIFY = 16,
			GDK_SELECTION_CLEAR = 17, GDK_SELECTION_REQUEST = 18, GDK_SELECTION_NOTIFY = 19, GDK_PROXIMITY_IN = 20,
			GDK_PROXIMITY_OUT = 21, GDK_DRAG_ENTER = 22, GDK_DRAG_LEAVE = 23, GDK_DRAG_MOTION = 24,
			GDK_DRAG_STATUS = 25, GDK_DROP_START = 26, GDK_DROP_FINISHED = 27, GDK_CLIENT_EVENT = 28,
			GDK_VISIBILITY_NOTIFY = 29, GDK_NO_EXPOSE = 30, GDK_SCROLL = 31, GDK_WINDOW_STATE = 32, GDK_SETTING = 33,
			GDK_OWNER_CHANGE = 34, GDK_GRAB_BROKEN = 35;

	/* Grabbed from GdkModifierType */
	public static final int GDK_SHIFT_MASK = 1 << 0, GDK_LOCK_MASK = 1 << 1, GDK_CONTROL_MASK = 1 << 2,
			GDK_MOD1_MASK = 1 << 3, GDK_MOD2_MASK = 1 << 4, GDK_MOD3_MASK = 1 << 5, GDK_MOD4_MASK = 1 << 6,
			GDK_MOD5_MASK = 1 << 7, GDK_BUTTON1_MASK = 1 << 8, GDK_BUTTON2_MASK = 1 << 9, GDK_BUTTON3_MASK = 1 << 10,
			GDK_BUTTON4_MASK = 1 << 11, GDK_BUTTON5_MASK = 1 << 12,
			/* The next few modifiers are used by XKB, so we skip to the end.
			 * Bits 16 - 28 are currently unused, but will eventually
			 * be used for "virtual modifiers". Bit 29 is used internally.
			 */
			GDK_RELEASE_MASK = 1 << 30, GDK_MODIFIER_MASK = GDK_RELEASE_MASK | 0x1fff;

	/* Grabbed from gdkkeysyms.h */
	public static final int GDK_VoidSymbol = 0xffffff, GDK_BackSpace = 0xff08, GDK_Tab = 0xff09, GDK_Linefeed = 0xff0a, GDK_Clear = 0xff0b,
			GDK_Return = 0xff0d, GDK_Pause = 0xff13, GDK_Scroll_Lock = 0xff14, GDK_Sys_Req = 0xff15,
			GDK_Escape = 0xff1b, GDK_Delete = 0xffff, GDK_Multi_key = 0xff20, GDK_Codeinput = 0xff37,
			GDK_SingleCandidate = 0xff3c, GDK_MultipleCandidate = 0xff3d, GDK_PreviousCandidate = 0xff3e,
			GDK_Kanji = 0xff21, GDK_Muhenkan = 0xff22, GDK_Henkan_Mode = 0xff23, GDK_Henkan = 0xff23,
			GDK_Romaji = 0xff24, GDK_Hiragana = 0xff25, GDK_Katakana = 0xff26, GDK_Hiragana_Katakana = 0xff27,
			GDK_Zenkaku = 0xff28, GDK_Hankaku = 0xff29, GDK_Zenkaku_Hankaku = 0xff2a, GDK_Touroku = 0xff2b,
			GDK_Massyo = 0xff2c, GDK_Kana_Lock = 0xff2d, GDK_Kana_Shift = 0xff2e, GDK_Eisu_Shift = 0xff2f,
			GDK_Eisu_toggle = 0xff30, GDK_Kanji_Bangou = 0xff37, GDK_Zen_Koho = 0xff3d, GDK_Mae_Koho = 0xff3e,
			GDK_Home = 0xff50, GDK_Left = 0xff51, GDK_Up = 0xff52, GDK_Right = 0xff53, GDK_Down = 0xff54,
			GDK_Prior = 0xff55, GDK_Page_Up = 0xff55, GDK_Next = 0xff56, GDK_Page_Down = 0xff56, GDK_End = 0xff57,
			GDK_Begin = 0xff58, GDK_Select = 0xff60, GDK_Print = 0xff61, GDK_Execute = 0xff62, GDK_Insert = 0xff63,
			GDK_Undo = 0xff65, GDK_Redo = 0xff66, GDK_Menu = 0xff67, GDK_Find = 0xff68, GDK_Cancel = 0xff69,
			GDK_Help = 0xff6a, GDK_Break = 0xff6b, GDK_Mode_switch = 0xff7e, GDK_script_switch = 0xff7e,
			GDK_Num_Lock = 0xff7f, GDK_KP_Space = 0xff80, GDK_KP_Tab = 0xff89, GDK_KP_Enter = 0xff8d,
			GDK_KP_F1 = 0xff91, GDK_KP_F2 = 0xff92, GDK_KP_F3 = 0xff93, GDK_KP_F4 = 0xff94, GDK_KP_Home = 0xff95,
			GDK_KP_Left = 0xff96, GDK_KP_Up = 0xff97, GDK_KP_Right = 0xff98, GDK_KP_Down = 0xff99,
			GDK_KP_Prior = 0xff9a, GDK_KP_Page_Up = 0xff9a, GDK_KP_Next = 0xff9b, GDK_KP_Page_Down = 0xff9b,
			GDK_KP_End = 0xff9c, GDK_KP_Begin = 0xff9d, GDK_KP_Insert = 0xff9e, GDK_KP_Delete = 0xff9f,
			GDK_KP_Equal = 0xffbd, GDK_KP_Multiply = 0xffaa, GDK_KP_Add = 0xffab, GDK_KP_Separator = 0xffac,
			GDK_KP_Subtract = 0xffad, GDK_KP_Decimal = 0xffae, GDK_KP_Divide = 0xffaf, GDK_KP_0 = 0xffb0,
			GDK_KP_1 = 0xffb1, GDK_KP_2 = 0xffb2, GDK_KP_3 = 0xffb3, GDK_KP_4 = 0xffb4, GDK_KP_5 = 0xffb5,
			GDK_KP_6 = 0xffb6, GDK_KP_7 = 0xffb7, GDK_KP_8 = 0xffb8, GDK_KP_9 = 0xffb9, GDK_F1 = 0xffbe,
			GDK_F2 = 0xffbf, GDK_F3 = 0xffc0, GDK_F4 = 0xffc1, GDK_F5 = 0xffc2, GDK_F6 = 0xffc3, GDK_F7 = 0xffc4,
			GDK_F8 = 0xffc5, GDK_F9 = 0xffc6, GDK_F10 = 0xffc7, GDK_F11 = 0xffc8, GDK_L1 = 0xffc8, GDK_F12 = 0xffc9,
			GDK_L2 = 0xffc9, GDK_F13 = 0xffca, GDK_L3 = 0xffca, GDK_F14 = 0xffcb, GDK_L4 = 0xffcb, GDK_F15 = 0xffcc,
			GDK_L5 = 0xffcc, GDK_F16 = 0xffcd, GDK_L6 = 0xffcd, GDK_F17 = 0xffce, GDK_L7 = 0xffce, GDK_F18 = 0xffcf,
			GDK_L8 = 0xffcf, GDK_F19 = 0xffd0, GDK_L9 = 0xffd0, GDK_F20 = 0xffd1, GDK_L10 = 0xffd1, GDK_F21 = 0xffd2,
			GDK_R1 = 0xffd2, GDK_F22 = 0xffd3, GDK_R2 = 0xffd3, GDK_F23 = 0xffd4, GDK_R3 = 0xffd4, GDK_F24 = 0xffd5,
			GDK_R4 = 0xffd5, GDK_F25 = 0xffd6, GDK_R5 = 0xffd6, GDK_F26 = 0xffd7, GDK_R6 = 0xffd7, GDK_F27 = 0xffd8,
			GDK_R7 = 0xffd8, GDK_F28 = 0xffd9, GDK_R8 = 0xffd9, GDK_F29 = 0xffda, GDK_R9 = 0xffda, GDK_F30 = 0xffdb,
			GDK_R10 = 0xffdb, GDK_F31 = 0xffdc, GDK_R11 = 0xffdc, GDK_F32 = 0xffdd, GDK_R12 = 0xffdd, GDK_F33 = 0xffde,
			GDK_R13 = 0xffde, GDK_F34 = 0xffdf, GDK_R14 = 0xffdf, GDK_F35 = 0xffe0, GDK_R15 = 0xffe0,
			GDK_Shift_L = 0xffe1, GDK_Shift_R = 0xffe2, GDK_Control_L = 0xffe3, GDK_Control_R = 0xffe4,
			GDK_Caps_Lock = 0xffe5, GDK_Shift_Lock = 0xffe6, GDK_Meta_L = 0xffe7, GDK_Meta_R = 0xffe8,
			GDK_Alt_L = 0xffe9, GDK_Alt_R = 0xffea, GDK_Super_L = 0xffeb, GDK_Super_R = 0xffec, GDK_Hyper_L = 0xffed,
			GDK_Hyper_R = 0xffee, GDK_ISO_Lock = 0xfe01, GDK_ISO_Level2_Latch = 0xfe02, GDK_ISO_Level3_Shift = 0xfe03,
			GDK_ISO_Level3_Latch = 0xfe04, GDK_ISO_Level3_Lock = 0xfe05, GDK_ISO_Group_Shift = 0xff7e,
			GDK_ISO_Group_Latch = 0xfe06, GDK_ISO_Group_Lock = 0xfe07, GDK_ISO_Next_Group = 0xfe08,
			GDK_ISO_Next_Group_Lock = 0xfe09, GDK_ISO_Prev_Group = 0xfe0a, GDK_ISO_Prev_Group_Lock = 0xfe0b,
			GDK_ISO_First_Group = 0xfe0c, GDK_ISO_First_Group_Lock = 0xfe0d, GDK_ISO_Last_Group = 0xfe0e,
			GDK_ISO_Last_Group_Lock = 0xfe0f, GDK_ISO_Left_Tab = 0xfe20, GDK_ISO_Move_Line_Up = 0xfe21,
			GDK_ISO_Move_Line_Down = 0xfe22, GDK_ISO_Partial_Line_Up = 0xfe23, GDK_ISO_Partial_Line_Down = 0xfe24,
			GDK_ISO_Partial_Space_Left = 0xfe25, GDK_ISO_Partial_Space_Right = 0xfe26,
			GDK_ISO_Set_Margin_Left = 0xfe27, GDK_ISO_Set_Margin_Right = 0xfe28, GDK_ISO_Release_Margin_Left = 0xfe29,
			GDK_ISO_Release_Margin_Right = 0xfe2a, GDK_ISO_Release_Both_Margins = 0xfe2b,
			GDK_ISO_Fast_Cursor_Left = 0xfe2c, GDK_ISO_Fast_Cursor_Right = 0xfe2d, GDK_ISO_Fast_Cursor_Up = 0xfe2e,
			GDK_ISO_Fast_Cursor_Down = 0xfe2f, GDK_ISO_Continuous_Underline = 0xfe30,
			GDK_ISO_Discontinuous_Underline = 0xfe31, GDK_ISO_Emphasize = 0xfe32, GDK_ISO_Center_Object = 0xfe33,
			GDK_ISO_Enter = 0xfe34, GDK_dead_grave = 0xfe50, GDK_dead_acute = 0xfe51, GDK_dead_circumflex = 0xfe52,
			GDK_dead_tilde = 0xfe53, GDK_dead_macron = 0xfe54, GDK_dead_breve = 0xfe55, GDK_dead_abovedot = 0xfe56,
			GDK_dead_diaeresis = 0xfe57, GDK_dead_abovering = 0xfe58, GDK_dead_doubleacute = 0xfe59,
			GDK_dead_caron = 0xfe5a, GDK_dead_cedilla = 0xfe5b, GDK_dead_ogonek = 0xfe5c, GDK_dead_iota = 0xfe5d,
			GDK_dead_voiced_sound = 0xfe5e, GDK_dead_semivoiced_sound = 0xfe5f, GDK_dead_belowdot = 0xfe60,
			GDK_dead_hook = 0xfe61, GDK_dead_horn = 0xfe62, GDK_First_Virtual_Screen = 0xfed0,
			GDK_Prev_Virtual_Screen = 0xfed1, GDK_Next_Virtual_Screen = 0xfed2, GDK_Last_Virtual_Screen = 0xfed4,
			GDK_Terminate_Server = 0xfed5, GDK_AccessX_Enable = 0xfe70, GDK_AccessX_Feedback_Enable = 0xfe71,
			GDK_RepeatKeys_Enable = 0xfe72, GDK_SlowKeys_Enable = 0xfe73, GDK_BounceKeys_Enable = 0xfe74,
			GDK_StickyKeys_Enable = 0xfe75, GDK_MouseKeys_Enable = 0xfe76, GDK_MouseKeys_Accel_Enable = 0xfe77,
			GDK_Overlay1_Enable = 0xfe78, GDK_Overlay2_Enable = 0xfe79, GDK_AudibleBell_Enable = 0xfe7a,
			GDK_Pointer_Left = 0xfee0, GDK_Pointer_Right = 0xfee1, GDK_Pointer_Up = 0xfee2, GDK_Pointer_Down = 0xfee3,
			GDK_Pointer_UpLeft = 0xfee4, GDK_Pointer_UpRight = 0xfee5, GDK_Pointer_DownLeft = 0xfee6,
			GDK_Pointer_DownRight = 0xfee7, GDK_Pointer_Button_Dflt = 0xfee8, GDK_Pointer_Button1 = 0xfee9,
			GDK_Pointer_Button2 = 0xfeea, GDK_Pointer_Button3 = 0xfeeb, GDK_Pointer_Button4 = 0xfeec,
			GDK_Pointer_Button5 = 0xfeed, GDK_Pointer_DblClick_Dflt = 0xfeee, GDK_Pointer_DblClick1 = 0xfeef,
			GDK_Pointer_DblClick2 = 0xfef0, GDK_Pointer_DblClick3 = 0xfef1, GDK_Pointer_DblClick4 = 0xfef2,
			GDK_Pointer_DblClick5 = 0xfef3, GDK_Pointer_Drag_Dflt = 0xfef4, GDK_Pointer_Drag1 = 0xfef5,
			GDK_Pointer_Drag2 = 0xfef6, GDK_Pointer_Drag3 = 0xfef7, GDK_Pointer_Drag4 = 0xfef8,
			GDK_Pointer_Drag5 = 0xfefd, GDK_Pointer_EnableKeys = 0xfef9, GDK_Pointer_Accelerate = 0xfefa,
			GDK_Pointer_DfltBtnNext = 0xfefb, GDK_Pointer_DfltBtnPrev = 0xfefc, GDK_3270_Duplicate = 0xfd01,
			GDK_3270_FieldMark = 0xfd02, GDK_3270_Right2 = 0xfd03, GDK_3270_Left2 = 0xfd04, GDK_3270_BackTab = 0xfd05,
			GDK_3270_EraseEOF = 0xfd06, GDK_3270_EraseInput = 0xfd07, GDK_3270_Reset = 0xfd08, GDK_3270_Quit = 0xfd09,
			GDK_3270_PA1 = 0xfd0a, GDK_3270_PA2 = 0xfd0b, GDK_3270_PA3 = 0xfd0c, GDK_3270_Test = 0xfd0d,
			GDK_3270_Attn = 0xfd0e, GDK_3270_CursorBlink = 0xfd0f, GDK_3270_AltCursor = 0xfd10,
			GDK_3270_KeyClick = 0xfd11, GDK_3270_Jump = 0xfd12, GDK_3270_Ident = 0xfd13, GDK_3270_Rule = 0xfd14,
			GDK_3270_Copy = 0xfd15, GDK_3270_Play = 0xfd16, GDK_3270_Setup = 0xfd17, GDK_3270_Record = 0xfd18,
			GDK_3270_ChangeScreen = 0xfd19, GDK_3270_DeleteWord = 0xfd1a, GDK_3270_ExSelect = 0xfd1b,
			GDK_3270_CursorSelect = 0xfd1c, GDK_3270_PrintScreen = 0xfd1d, GDK_3270_Enter = 0xfd1e, GDK_space = 0x020,
			GDK_exclam = 0x021, GDK_quotedbl = 0x022, GDK_numbersign = 0x023, GDK_dollar = 0x024, GDK_percent = 0x025,
			GDK_ampersand = 0x026, GDK_apostrophe = 0x027, GDK_quoteright = 0x027, GDK_parenleft = 0x028,
			GDK_parenright = 0x029, GDK_asterisk = 0x02a, GDK_plus = 0x02b, GDK_comma = 0x02c, GDK_minus = 0x02d,
			GDK_period = 0x02e, GDK_slash = 0x02f, GDK_0 = 0x030, GDK_1 = 0x031, GDK_2 = 0x032, GDK_3 = 0x033,
			GDK_4 = 0x034, GDK_5 = 0x035, GDK_6 = 0x036, GDK_7 = 0x037, GDK_8 = 0x038, GDK_9 = 0x039,
			GDK_colon = 0x03a, GDK_semicolon = 0x03b, GDK_less = 0x03c, GDK_equal = 0x03d, GDK_greater = 0x03e,
			GDK_question = 0x03f, GDK_at = 0x040, GDK_A = 0x041, GDK_B = 0x042, GDK_C = 0x043, GDK_D = 0x044,
			GDK_E = 0x045, GDK_F = 0x046, GDK_G = 0x047, GDK_H = 0x048, GDK_I = 0x049, GDK_J = 0x04a, GDK_K = 0x04b,
			GDK_L = 0x04c, GDK_M = 0x04d, GDK_N = 0x04e, GDK_O = 0x04f, GDK_P = 0x050, GDK_Q = 0x051, GDK_R = 0x052,
			GDK_S = 0x053, GDK_T = 0x054, GDK_U = 0x055, GDK_V = 0x056, GDK_W = 0x057, GDK_X = 0x058, GDK_Y = 0x059,
			GDK_Z = 0x05a, GDK_bracketleft = 0x05b, GDK_backslash = 0x05c, GDK_bracketright = 0x05d,
			GDK_asciicircum = 0x05e, GDK_underscore = 0x05f, GDK_grave = 0x060, GDK_quoteleft = 0x060, GDK_a = 0x061,
			GDK_b = 0x062, GDK_c = 0x063, GDK_d = 0x064, GDK_e = 0x065, GDK_f = 0x066, GDK_g = 0x067, GDK_h = 0x068,
			GDK_i = 0x069, GDK_j = 0x06a, GDK_k = 0x06b, GDK_l = 0x06c, GDK_m = 0x06d, GDK_n = 0x06e, GDK_o = 0x06f,
			GDK_p = 0x070, GDK_q = 0x071, GDK_r = 0x072, GDK_s = 0x073, GDK_t = 0x074, GDK_u = 0x075, GDK_v = 0x076,
			GDK_w = 0x077, GDK_x = 0x078, GDK_y = 0x079, GDK_z = 0x07a, GDK_braceleft = 0x07b, GDK_bar = 0x07c,
			GDK_braceright = 0x07d, GDK_asciitilde = 0x07e, GDK_nobreakspace = 0x0a0, GDK_exclamdown = 0x0a1,
			GDK_cent = 0x0a2, GDK_sterling = 0x0a3, GDK_currency = 0x0a4, GDK_yen = 0x0a5, GDK_brokenbar = 0x0a6,
			GDK_section = 0x0a7, GDK_diaeresis = 0x0a8, GDK_copyright = 0x0a9, GDK_ordfeminine = 0x0aa,
			GDK_guillemotleft = 0x0ab, GDK_notsign = 0x0ac, GDK_hyphen = 0x0ad, GDK_registered = 0x0ae,
			GDK_macron = 0x0af, GDK_degree = 0x0b0, GDK_plusminus = 0x0b1, GDK_twosuperior = 0x0b2,
			GDK_threesuperior = 0x0b3, GDK_acute = 0x0b4, GDK_mu = 0x0b5, GDK_paragraph = 0x0b6,
			GDK_periodcentered = 0x0b7, GDK_cedilla = 0x0b8, GDK_onesuperior = 0x0b9, GDK_masculine = 0x0ba,
			GDK_guillemotright = 0x0bb, GDK_onequarter = 0x0bc, GDK_onehalf = 0x0bd, GDK_threequarters = 0x0be,
			GDK_questiondown = 0x0bf, GDK_Agrave = 0x0c0, GDK_Aacute = 0x0c1, GDK_Acircumflex = 0x0c2,
			GDK_Atilde = 0x0c3, GDK_Adiaeresis = 0x0c4, GDK_Aring = 0x0c5, GDK_AE = 0x0c6, GDK_Ccedilla = 0x0c7,
			GDK_Egrave = 0x0c8, GDK_Eacute = 0x0c9, GDK_Ecircumflex = 0x0ca, GDK_Ediaeresis = 0x0cb,
			GDK_Igrave = 0x0cc, GDK_Iacute = 0x0cd, GDK_Icircumflex = 0x0ce, GDK_Idiaeresis = 0x0cf, GDK_ETH = 0x0d0,
			GDK_Eth = 0x0d0, GDK_Ntilde = 0x0d1, GDK_Ograve = 0x0d2, GDK_Oacute = 0x0d3, GDK_Ocircumflex = 0x0d4,
			GDK_Otilde = 0x0d5, GDK_Odiaeresis = 0x0d6, GDK_multiply = 0x0d7, GDK_Oslash = 0x0d8, GDK_Ooblique = 0x0d8,
			GDK_Ugrave = 0x0d9, GDK_Uacute = 0x0da, GDK_Ucircumflex = 0x0db, GDK_Udiaeresis = 0x0dc,
			GDK_Yacute = 0x0dd, GDK_THORN = 0x0de, GDK_Thorn = 0x0de, GDK_ssharp = 0x0df, GDK_agrave = 0x0e0,
			GDK_aacute = 0x0e1, GDK_acircumflex = 0x0e2, GDK_atilde = 0x0e3, GDK_adiaeresis = 0x0e4, GDK_aring = 0x0e5,
			GDK_ae = 0x0e6, GDK_ccedilla = 0x0e7, GDK_egrave = 0x0e8, GDK_eacute = 0x0e9, GDK_ecircumflex = 0x0ea,
			GDK_ediaeresis = 0x0eb, GDK_igrave = 0x0ec, GDK_iacute = 0x0ed, GDK_icircumflex = 0x0ee,
			GDK_idiaeresis = 0x0ef, GDK_eth = 0x0f0, GDK_ntilde = 0x0f1, GDK_ograve = 0x0f2, GDK_oacute = 0x0f3,
			GDK_ocircumflex = 0x0f4, GDK_otilde = 0x0f5, GDK_odiaeresis = 0x0f6, GDK_division = 0x0f7,
			GDK_oslash = 0x0f8, GDK_ooblique = 0x0f8, GDK_ugrave = 0x0f9, GDK_uacute = 0x0fa, GDK_ucircumflex = 0x0fb,
			GDK_udiaeresis = 0x0fc, GDK_yacute = 0x0fd, GDK_thorn = 0x0fe, GDK_ydiaeresis = 0x0ff, GDK_Aogonek = 0x1a1,
			GDK_breve = 0x1a2, GDK_Lstroke = 0x1a3, GDK_Lcaron = 0x1a5, GDK_Sacute = 0x1a6, GDK_Scaron = 0x1a9,
			GDK_Scedilla = 0x1aa, GDK_Tcaron = 0x1ab, GDK_Zacute = 0x1ac, GDK_Zcaron = 0x1ae, GDK_Zabovedot = 0x1af,
			GDK_aogonek = 0x1b1, GDK_ogonek = 0x1b2, GDK_lstroke = 0x1b3, GDK_lcaron = 0x1b5, GDK_sacute = 0x1b6,
			GDK_caron = 0x1b7, GDK_scaron = 0x1b9, GDK_scedilla = 0x1ba, GDK_tcaron = 0x1bb, GDK_zacute = 0x1bc,
			GDK_doubleacute = 0x1bd, GDK_zcaron = 0x1be, GDK_zabovedot = 0x1bf, GDK_Racute = 0x1c0, GDK_Abreve = 0x1c3,
			GDK_Lacute = 0x1c5, GDK_Cacute = 0x1c6, GDK_Ccaron = 0x1c8, GDK_Eogonek = 0x1ca, GDK_Ecaron = 0x1cc,
			GDK_Dcaron = 0x1cf, GDK_Dstroke = 0x1d0, GDK_Nacute = 0x1d1, GDK_Ncaron = 0x1d2, GDK_Odoubleacute = 0x1d5,
			GDK_Rcaron = 0x1d8, GDK_Uring = 0x1d9, GDK_Udoubleacute = 0x1db, GDK_Tcedilla = 0x1de, GDK_racute = 0x1e0,
			GDK_abreve = 0x1e3, GDK_lacute = 0x1e5, GDK_cacute = 0x1e6, GDK_ccaron = 0x1e8, GDK_eogonek = 0x1ea,
			GDK_ecaron = 0x1ec, GDK_dcaron = 0x1ef, GDK_dstroke = 0x1f0, GDK_nacute = 0x1f1, GDK_ncaron = 0x1f2,
			GDK_odoubleacute = 0x1f5, GDK_udoubleacute = 0x1fb, GDK_rcaron = 0x1f8, GDK_uring = 0x1f9,
			GDK_tcedilla = 0x1fe, GDK_abovedot = 0x1ff, GDK_Hstroke = 0x2a1, GDK_Hcircumflex = 0x2a6,
			GDK_Iabovedot = 0x2a9, GDK_Gbreve = 0x2ab, GDK_Jcircumflex = 0x2ac, GDK_hstroke = 0x2b1,
			GDK_hcircumflex = 0x2b6, GDK_idotless = 0x2b9, GDK_gbreve = 0x2bb, GDK_jcircumflex = 0x2bc,
			GDK_Cabovedot = 0x2c5, GDK_Ccircumflex = 0x2c6, GDK_Gabovedot = 0x2d5, GDK_Gcircumflex = 0x2d8,
			GDK_Ubreve = 0x2dd, GDK_Scircumflex = 0x2de, GDK_cabovedot = 0x2e5, GDK_ccircumflex = 0x2e6,
			GDK_gabovedot = 0x2f5, GDK_gcircumflex = 0x2f8, GDK_ubreve = 0x2fd, GDK_scircumflex = 0x2fe,
			GDK_kra = 0x3a2, GDK_kappa = 0x3a2, GDK_Rcedilla = 0x3a3, GDK_Itilde = 0x3a5, GDK_Lcedilla = 0x3a6,
			GDK_Emacron = 0x3aa, GDK_Gcedilla = 0x3ab, GDK_Tslash = 0x3ac, GDK_rcedilla = 0x3b3, GDK_itilde = 0x3b5,
			GDK_lcedilla = 0x3b6, GDK_emacron = 0x3ba, GDK_gcedilla = 0x3bb, GDK_tslash = 0x3bc, GDK_ENG = 0x3bd,
			GDK_eng = 0x3bf, GDK_Amacron = 0x3c0, GDK_Iogonek = 0x3c7, GDK_Eabovedot = 0x3cc, GDK_Imacron = 0x3cf,
			GDK_Ncedilla = 0x3d1, GDK_Omacron = 0x3d2, GDK_Kcedilla = 0x3d3, GDK_Uogonek = 0x3d9, GDK_Utilde = 0x3dd,
			GDK_Umacron = 0x3de, GDK_amacron = 0x3e0, GDK_iogonek = 0x3e7, GDK_eabovedot = 0x3ec, GDK_imacron = 0x3ef,
			GDK_ncedilla = 0x3f1, GDK_omacron = 0x3f2, GDK_kcedilla = 0x3f3, GDK_uogonek = 0x3f9, GDK_utilde = 0x3fd,
			GDK_umacron = 0x3fe, GDK_Babovedot = 0x1001e02, GDK_babovedot = 0x1001e03, GDK_Dabovedot = 0x1001e0a,
			GDK_Wgrave = 0x1001e80, GDK_Wacute = 0x1001e82, GDK_dabovedot = 0x1001e0b, GDK_Ygrave = 0x1001ef2,
			GDK_Fabovedot = 0x1001e1e, GDK_fabovedot = 0x1001e1f, GDK_Mabovedot = 0x1001e40, GDK_mabovedot = 0x1001e41,
			GDK_Pabovedot = 0x1001e56, GDK_wgrave = 0x1001e81, GDK_pabovedot = 0x1001e57, GDK_wacute = 0x1001e83,
			GDK_Sabovedot = 0x1001e60, GDK_ygrave = 0x1001ef3, GDK_Wdiaeresis = 0x1001e84, GDK_wdiaeresis = 0x1001e85,
			GDK_sabovedot = 0x1001e61, GDK_Wcircumflex = 0x1000174, GDK_Tabovedot = 0x1001e6a,
			GDK_Ycircumflex = 0x1000176, GDK_wcircumflex = 0x1000175, GDK_tabovedot = 0x1001e6b,
			GDK_ycircumflex = 0x1000177, GDK_OE = 0x13bc, GDK_oe = 0x13bd, GDK_Ydiaeresis = 0x13be,
			GDK_overline = 0x47e, GDK_kana_fullstop = 0x4a1, GDK_kana_openingbracket = 0x4a2,
			GDK_kana_closingbracket = 0x4a3, GDK_kana_comma = 0x4a4, GDK_kana_conjunctive = 0x4a5,
			GDK_kana_middledot = 0x4a5, GDK_kana_WO = 0x4a6, GDK_kana_a = 0x4a7, GDK_kana_i = 0x4a8,
			GDK_kana_u = 0x4a9, GDK_kana_e = 0x4aa, GDK_kana_o = 0x4ab, GDK_kana_ya = 0x4ac, GDK_kana_yu = 0x4ad,
			GDK_kana_yo = 0x4ae, GDK_kana_tsu = 0x4af, GDK_kana_tu = 0x4af, GDK_prolongedsound = 0x4b0,
			GDK_kana_A = 0x4b1, GDK_kana_I = 0x4b2, GDK_kana_U = 0x4b3, GDK_kana_E = 0x4b4, GDK_kana_O = 0x4b5,
			GDK_kana_KA = 0x4b6, GDK_kana_KI = 0x4b7, GDK_kana_KU = 0x4b8, GDK_kana_KE = 0x4b9, GDK_kana_KO = 0x4ba,
			GDK_kana_SA = 0x4bb, GDK_kana_SHI = 0x4bc, GDK_kana_SU = 0x4bd, GDK_kana_SE = 0x4be, GDK_kana_SO = 0x4bf,
			GDK_kana_TA = 0x4c0, GDK_kana_CHI = 0x4c1, GDK_kana_TI = 0x4c1, GDK_kana_TSU = 0x4c2, GDK_kana_TU = 0x4c2,
			GDK_kana_TE = 0x4c3, GDK_kana_TO = 0x4c4, GDK_kana_NA = 0x4c5, GDK_kana_NI = 0x4c6, GDK_kana_NU = 0x4c7,
			GDK_kana_NE = 0x4c8, GDK_kana_NO = 0x4c9, GDK_kana_HA = 0x4ca, GDK_kana_HI = 0x4cb, GDK_kana_FU = 0x4cc,
			GDK_kana_HU = 0x4cc, GDK_kana_HE = 0x4cd, GDK_kana_HO = 0x4ce, GDK_kana_MA = 0x4cf, GDK_kana_MI = 0x4d0,
			GDK_kana_MU = 0x4d1, GDK_kana_ME = 0x4d2, GDK_kana_MO = 0x4d3, GDK_kana_YA = 0x4d4, GDK_kana_YU = 0x4d5,
			GDK_kana_YO = 0x4d6, GDK_kana_RA = 0x4d7, GDK_kana_RI = 0x4d8, GDK_kana_RU = 0x4d9, GDK_kana_RE = 0x4da,
			GDK_kana_RO = 0x4db, GDK_kana_WA = 0x4dc, GDK_kana_N = 0x4dd, GDK_voicedsound = 0x4de,
			GDK_semivoicedsound = 0x4df, GDK_kana_switch = 0xff7e, GDK_Farsi_0 = 0x10006f0, GDK_Farsi_1 = 0x10006f1,
			GDK_Farsi_2 = 0x10006f2, GDK_Farsi_3 = 0x10006f3, GDK_Farsi_4 = 0x10006f4, GDK_Farsi_5 = 0x10006f5,
			GDK_Farsi_6 = 0x10006f6, GDK_Farsi_7 = 0x10006f7, GDK_Farsi_8 = 0x10006f8, GDK_Farsi_9 = 0x10006f9,
			GDK_Arabic_percent = 0x100066a, GDK_Arabic_superscript_alef = 0x1000670, GDK_Arabic_tteh = 0x1000679,
			GDK_Arabic_peh = 0x100067e, GDK_Arabic_tcheh = 0x1000686, GDK_Arabic_ddal = 0x1000688,
			GDK_Arabic_rreh = 0x1000691, GDK_Arabic_comma = 0x5ac, GDK_Arabic_fullstop = 0x10006d4,
			GDK_Arabic_0 = 0x1000660, GDK_Arabic_1 = 0x1000661, GDK_Arabic_2 = 0x1000662, GDK_Arabic_3 = 0x1000663,
			GDK_Arabic_4 = 0x1000664, GDK_Arabic_5 = 0x1000665, GDK_Arabic_6 = 0x1000666, GDK_Arabic_7 = 0x1000667,
			GDK_Arabic_8 = 0x1000668, GDK_Arabic_9 = 0x1000669, GDK_Arabic_semicolon = 0x5bb,
			GDK_Arabic_question_mark = 0x5bf, GDK_Arabic_hamza = 0x5c1, GDK_Arabic_maddaonalef = 0x5c2,
			GDK_Arabic_hamzaonalef = 0x5c3, GDK_Arabic_hamzaonwaw = 0x5c4, GDK_Arabic_hamzaunderalef = 0x5c5,
			GDK_Arabic_hamzaonyeh = 0x5c6, GDK_Arabic_alef = 0x5c7, GDK_Arabic_beh = 0x5c8,
			GDK_Arabic_tehmarbuta = 0x5c9, GDK_Arabic_teh = 0x5ca, GDK_Arabic_theh = 0x5cb, GDK_Arabic_jeem = 0x5cc,
			GDK_Arabic_hah = 0x5cd, GDK_Arabic_khah = 0x5ce, GDK_Arabic_dal = 0x5cf, GDK_Arabic_thal = 0x5d0,
			GDK_Arabic_ra = 0x5d1, GDK_Arabic_zain = 0x5d2, GDK_Arabic_seen = 0x5d3, GDK_Arabic_sheen = 0x5d4,
			GDK_Arabic_sad = 0x5d5, GDK_Arabic_dad = 0x5d6, GDK_Arabic_tah = 0x5d7, GDK_Arabic_zah = 0x5d8,
			GDK_Arabic_ain = 0x5d9, GDK_Arabic_ghain = 0x5da, GDK_Arabic_tatweel = 0x5e0, GDK_Arabic_feh = 0x5e1,
			GDK_Arabic_qaf = 0x5e2, GDK_Arabic_kaf = 0x5e3, GDK_Arabic_lam = 0x5e4, GDK_Arabic_meem = 0x5e5,
			GDK_Arabic_noon = 0x5e6, GDK_Arabic_ha = 0x5e7, GDK_Arabic_heh = 0x5e7, GDK_Arabic_waw = 0x5e8,
			GDK_Arabic_alefmaksura = 0x5e9, GDK_Arabic_yeh = 0x5ea, GDK_Arabic_fathatan = 0x5eb,
			GDK_Arabic_dammatan = 0x5ec, GDK_Arabic_kasratan = 0x5ed, GDK_Arabic_fatha = 0x5ee,
			GDK_Arabic_damma = 0x5ef, GDK_Arabic_kasra = 0x5f0, GDK_Arabic_shadda = 0x5f1, GDK_Arabic_sukun = 0x5f2,
			GDK_Arabic_madda_above = 0x1000653, GDK_Arabic_hamza_above = 0x1000654, GDK_Arabic_hamza_below = 0x1000655,
			GDK_Arabic_jeh = 0x1000698, GDK_Arabic_veh = 0x10006a4, GDK_Arabic_keheh = 0x10006a9,
			GDK_Arabic_gaf = 0x10006af, GDK_Arabic_noon_ghunna = 0x10006ba, GDK_Arabic_heh_doachashmee = 0x10006be,
			GDK_Farsi_yeh = 0x10006cc, GDK_Arabic_farsi_yeh = 0x10006cc, GDK_Arabic_yeh_baree = 0x10006d2,
			GDK_Arabic_heh_goal = 0x10006c1, GDK_Arabic_switch = 0xff7e, GDK_Cyrillic_GHE_bar = 0x1000492,
			GDK_Cyrillic_ghe_bar = 0x1000493, GDK_Cyrillic_ZHE_descender = 0x1000496,
			GDK_Cyrillic_zhe_descender = 0x1000497, GDK_Cyrillic_KA_descender = 0x100049a,
			GDK_Cyrillic_ka_descender = 0x100049b, GDK_Cyrillic_KA_vertstroke = 0x100049c,
			GDK_Cyrillic_ka_vertstroke = 0x100049d, GDK_Cyrillic_EN_descender = 0x10004a2,
			GDK_Cyrillic_en_descender = 0x10004a3, GDK_Cyrillic_U_straight = 0x10004ae,
			GDK_Cyrillic_u_straight = 0x10004af, GDK_Cyrillic_U_straight_bar = 0x10004b0,
			GDK_Cyrillic_u_straight_bar = 0x10004b1, GDK_Cyrillic_HA_descender = 0x10004b2,
			GDK_Cyrillic_ha_descender = 0x10004b3, GDK_Cyrillic_CHE_descender = 0x10004b6,
			GDK_Cyrillic_che_descender = 0x10004b7, GDK_Cyrillic_CHE_vertstroke = 0x10004b8,
			GDK_Cyrillic_che_vertstroke = 0x10004b9, GDK_Cyrillic_SHHA = 0x10004ba, GDK_Cyrillic_shha = 0x10004bb,
			GDK_Cyrillic_SCHWA = 0x10004d8, GDK_Cyrillic_schwa = 0x10004d9, GDK_Cyrillic_I_macron = 0x10004e2,
			GDK_Cyrillic_i_macron = 0x10004e3, GDK_Cyrillic_O_bar = 0x10004e8, GDK_Cyrillic_o_bar = 0x10004e9,
			GDK_Cyrillic_U_macron = 0x10004ee, GDK_Cyrillic_u_macron = 0x10004ef, GDK_Serbian_dje = 0x6a1,
			GDK_Macedonia_gje = 0x6a2, GDK_Cyrillic_io = 0x6a3, GDK_Ukrainian_ie = 0x6a4, GDK_Ukranian_je = 0x6a4,
			GDK_Macedonia_dse = 0x6a5, GDK_Ukrainian_i = 0x6a6, GDK_Ukranian_i = 0x6a6, GDK_Ukrainian_yi = 0x6a7,
			GDK_Ukranian_yi = 0x6a7, GDK_Cyrillic_je = 0x6a8, GDK_Serbian_je = 0x6a8, GDK_Cyrillic_lje = 0x6a9,
			GDK_Serbian_lje = 0x6a9, GDK_Cyrillic_nje = 0x6aa, GDK_Serbian_nje = 0x6aa, GDK_Serbian_tshe = 0x6ab,
			GDK_Macedonia_kje = 0x6ac, GDK_Ukrainian_ghe_with_upturn = 0x6ad, GDK_Byelorussian_shortu = 0x6ae,
			GDK_Cyrillic_dzhe = 0x6af, GDK_Serbian_dze = 0x6af, GDK_numerosign = 0x6b0, GDK_Serbian_DJE = 0x6b1,
			GDK_Macedonia_GJE = 0x6b2, GDK_Cyrillic_IO = 0x6b3, GDK_Ukrainian_IE = 0x6b4, GDK_Ukranian_JE = 0x6b4,
			GDK_Macedonia_DSE = 0x6b5, GDK_Ukrainian_I = 0x6b6, GDK_Ukranian_I = 0x6b6, GDK_Ukrainian_YI = 0x6b7,
			GDK_Ukranian_YI = 0x6b7, GDK_Cyrillic_JE = 0x6b8, GDK_Serbian_JE = 0x6b8, GDK_Cyrillic_LJE = 0x6b9,
			GDK_Serbian_LJE = 0x6b9, GDK_Cyrillic_NJE = 0x6ba, GDK_Serbian_NJE = 0x6ba, GDK_Serbian_TSHE = 0x6bb,
			GDK_Macedonia_KJE = 0x6bc, GDK_Ukrainian_GHE_WITH_UPTURN = 0x6bd, GDK_Byelorussian_SHORTU = 0x6be,
			GDK_Cyrillic_DZHE = 0x6bf, GDK_Serbian_DZE = 0x6bf, GDK_Cyrillic_yu = 0x6c0, GDK_Cyrillic_a = 0x6c1,
			GDK_Cyrillic_be = 0x6c2, GDK_Cyrillic_tse = 0x6c3, GDK_Cyrillic_de = 0x6c4, GDK_Cyrillic_ie = 0x6c5,
			GDK_Cyrillic_ef = 0x6c6, GDK_Cyrillic_ghe = 0x6c7, GDK_Cyrillic_ha = 0x6c8, GDK_Cyrillic_i = 0x6c9,
			GDK_Cyrillic_shorti = 0x6ca, GDK_Cyrillic_ka = 0x6cb, GDK_Cyrillic_el = 0x6cc, GDK_Cyrillic_em = 0x6cd,
			GDK_Cyrillic_en = 0x6ce, GDK_Cyrillic_o = 0x6cf, GDK_Cyrillic_pe = 0x6d0, GDK_Cyrillic_ya = 0x6d1,
			GDK_Cyrillic_er = 0x6d2, GDK_Cyrillic_es = 0x6d3, GDK_Cyrillic_te = 0x6d4, GDK_Cyrillic_u = 0x6d5,
			GDK_Cyrillic_zhe = 0x6d6, GDK_Cyrillic_ve = 0x6d7, GDK_Cyrillic_softsign = 0x6d8,
			GDK_Cyrillic_yeru = 0x6d9, GDK_Cyrillic_ze = 0x6da, GDK_Cyrillic_sha = 0x6db, GDK_Cyrillic_e = 0x6dc,
			GDK_Cyrillic_shcha = 0x6dd, GDK_Cyrillic_che = 0x6de, GDK_Cyrillic_hardsign = 0x6df,
			GDK_Cyrillic_YU = 0x6e0, GDK_Cyrillic_A = 0x6e1, GDK_Cyrillic_BE = 0x6e2, GDK_Cyrillic_TSE = 0x6e3,
			GDK_Cyrillic_DE = 0x6e4, GDK_Cyrillic_IE = 0x6e5, GDK_Cyrillic_EF = 0x6e6, GDK_Cyrillic_GHE = 0x6e7,
			GDK_Cyrillic_HA = 0x6e8, GDK_Cyrillic_I = 0x6e9, GDK_Cyrillic_SHORTI = 0x6ea, GDK_Cyrillic_KA = 0x6eb,
			GDK_Cyrillic_EL = 0x6ec, GDK_Cyrillic_EM = 0x6ed, GDK_Cyrillic_EN = 0x6ee, GDK_Cyrillic_O = 0x6ef,
			GDK_Cyrillic_PE = 0x6f0, GDK_Cyrillic_YA = 0x6f1, GDK_Cyrillic_ER = 0x6f2, GDK_Cyrillic_ES = 0x6f3,
			GDK_Cyrillic_TE = 0x6f4, GDK_Cyrillic_U = 0x6f5, GDK_Cyrillic_ZHE = 0x6f6, GDK_Cyrillic_VE = 0x6f7,
			GDK_Cyrillic_SOFTSIGN = 0x6f8, GDK_Cyrillic_YERU = 0x6f9, GDK_Cyrillic_ZE = 0x6fa,
			GDK_Cyrillic_SHA = 0x6fb, GDK_Cyrillic_E = 0x6fc, GDK_Cyrillic_SHCHA = 0x6fd, GDK_Cyrillic_CHE = 0x6fe,
			GDK_Cyrillic_HARDSIGN = 0x6ff, GDK_Greek_ALPHAaccent = 0x7a1, GDK_Greek_EPSILONaccent = 0x7a2,
			GDK_Greek_ETAaccent = 0x7a3, GDK_Greek_IOTAaccent = 0x7a4, GDK_Greek_IOTAdieresis = 0x7a5,
			GDK_Greek_IOTAdiaeresis = 0x7a5, GDK_Greek_OMICRONaccent = 0x7a7, GDK_Greek_UPSILONaccent = 0x7a8,
			GDK_Greek_UPSILONdieresis = 0x7a9, GDK_Greek_OMEGAaccent = 0x7ab, GDK_Greek_accentdieresis = 0x7ae,
			GDK_Greek_horizbar = 0x7af, GDK_Greek_alphaaccent = 0x7b1, GDK_Greek_epsilonaccent = 0x7b2,
			GDK_Greek_etaaccent = 0x7b3, GDK_Greek_iotaaccent = 0x7b4, GDK_Greek_iotadieresis = 0x7b5,
			GDK_Greek_iotaaccentdieresis = 0x7b6, GDK_Greek_omicronaccent = 0x7b7, GDK_Greek_upsilonaccent = 0x7b8,
			GDK_Greek_upsilondieresis = 0x7b9, GDK_Greek_upsilonaccentdieresis = 0x7ba, GDK_Greek_omegaaccent = 0x7bb,
			GDK_Greek_ALPHA = 0x7c1, GDK_Greek_BETA = 0x7c2, GDK_Greek_GAMMA = 0x7c3, GDK_Greek_DELTA = 0x7c4,
			GDK_Greek_EPSILON = 0x7c5, GDK_Greek_ZETA = 0x7c6, GDK_Greek_ETA = 0x7c7, GDK_Greek_THETA = 0x7c8,
			GDK_Greek_IOTA = 0x7c9, GDK_Greek_KAPPA = 0x7ca, GDK_Greek_LAMDA = 0x7cb, GDK_Greek_LAMBDA = 0x7cb,
			GDK_Greek_MU = 0x7cc, GDK_Greek_NU = 0x7cd, GDK_Greek_XI = 0x7ce, GDK_Greek_OMICRON = 0x7cf,
			GDK_Greek_PI = 0x7d0, GDK_Greek_RHO = 0x7d1, GDK_Greek_SIGMA = 0x7d2, GDK_Greek_TAU = 0x7d4,
			GDK_Greek_UPSILON = 0x7d5, GDK_Greek_PHI = 0x7d6, GDK_Greek_CHI = 0x7d7, GDK_Greek_PSI = 0x7d8,
			GDK_Greek_OMEGA = 0x7d9, GDK_Greek_alpha = 0x7e1, GDK_Greek_beta = 0x7e2, GDK_Greek_gamma = 0x7e3,
			GDK_Greek_delta = 0x7e4, GDK_Greek_epsilon = 0x7e5, GDK_Greek_zeta = 0x7e6, GDK_Greek_eta = 0x7e7,
			GDK_Greek_theta = 0x7e8, GDK_Greek_iota = 0x7e9, GDK_Greek_kappa = 0x7ea, GDK_Greek_lamda = 0x7eb,
			GDK_Greek_lambda = 0x7eb, GDK_Greek_mu = 0x7ec, GDK_Greek_nu = 0x7ed, GDK_Greek_xi = 0x7ee,
			GDK_Greek_omicron = 0x7ef, GDK_Greek_pi = 0x7f0, GDK_Greek_rho = 0x7f1, GDK_Greek_sigma = 0x7f2,
			GDK_Greek_finalsmallsigma = 0x7f3, GDK_Greek_tau = 0x7f4, GDK_Greek_upsilon = 0x7f5, GDK_Greek_phi = 0x7f6,
			GDK_Greek_chi = 0x7f7, GDK_Greek_psi = 0x7f8, GDK_Greek_omega = 0x7f9, GDK_Greek_switch = 0xff7e,
			GDK_leftradical = 0x8a1, GDK_topleftradical = 0x8a2, GDK_horizconnector = 0x8a3, GDK_topintegral = 0x8a4,
			GDK_botintegral = 0x8a5, GDK_vertconnector = 0x8a6, GDK_topleftsqbracket = 0x8a7,
			GDK_botleftsqbracket = 0x8a8, GDK_toprightsqbracket = 0x8a9, GDK_botrightsqbracket = 0x8aa,
			GDK_topleftparens = 0x8ab, GDK_botleftparens = 0x8ac, GDK_toprightparens = 0x8ad,
			GDK_botrightparens = 0x8ae, GDK_leftmiddlecurlybrace = 0x8af, GDK_rightmiddlecurlybrace = 0x8b0,
			GDK_topleftsummation = 0x8b1, GDK_botleftsummation = 0x8b2, GDK_topvertsummationconnector = 0x8b3,
			GDK_botvertsummationconnector = 0x8b4, GDK_toprightsummation = 0x8b5, GDK_botrightsummation = 0x8b6,
			GDK_rightmiddlesummation = 0x8b7, GDK_lessthanequal = 0x8bc, GDK_notequal = 0x8bd,
			GDK_greaterthanequal = 0x8be, GDK_integral = 0x8bf, GDK_therefore = 0x8c0, GDK_variation = 0x8c1,
			GDK_infinity = 0x8c2, GDK_nabla = 0x8c5, GDK_approximate = 0x8c8, GDK_similarequal = 0x8c9,
			GDK_ifonlyif = 0x8cd, GDK_implies = 0x8ce, GDK_identical = 0x8cf, GDK_radical = 0x8d6,
			GDK_includedin = 0x8da, GDK_includes = 0x8db, GDK_intersection = 0x8dc, GDK_union = 0x8dd,
			GDK_logicaland = 0x8de, GDK_logicalor = 0x8df, GDK_partialderivative = 0x8ef, GDK_function = 0x8f6,
			GDK_leftarrow = 0x8fb, GDK_uparrow = 0x8fc, GDK_rightarrow = 0x8fd, GDK_downarrow = 0x8fe,
			GDK_blank = 0x9df, GDK_soliddiamond = 0x9e0, GDK_checkerboard = 0x9e1, GDK_ht = 0x9e2, GDK_ff = 0x9e3,
			GDK_cr = 0x9e4, GDK_lf = 0x9e5, GDK_nl = 0x9e8, GDK_vt = 0x9e9, GDK_lowrightcorner = 0x9ea,
			GDK_uprightcorner = 0x9eb, GDK_upleftcorner = 0x9ec, GDK_lowleftcorner = 0x9ed, GDK_crossinglines = 0x9ee,
			GDK_horizlinescan1 = 0x9ef, GDK_horizlinescan3 = 0x9f0, GDK_horizlinescan5 = 0x9f1,
			GDK_horizlinescan7 = 0x9f2, GDK_horizlinescan9 = 0x9f3, GDK_leftt = 0x9f4, GDK_rightt = 0x9f5,
			GDK_bott = 0x9f6, GDK_topt = 0x9f7, GDK_vertbar = 0x9f8, GDK_emspace = 0xaa1, GDK_enspace = 0xaa2,
			GDK_em3space = 0xaa3, GDK_em4space = 0xaa4, GDK_digitspace = 0xaa5, GDK_punctspace = 0xaa6,
			GDK_thinspace = 0xaa7, GDK_hairspace = 0xaa8, GDK_emdash = 0xaa9, GDK_endash = 0xaaa,
			GDK_signifblank = 0xaac, GDK_ellipsis = 0xaae, GDK_doubbaselinedot = 0xaaf, GDK_onethird = 0xab0,
			GDK_twothirds = 0xab1, GDK_onefifth = 0xab2, GDK_twofifths = 0xab3, GDK_threefifths = 0xab4,
			GDK_fourfifths = 0xab5, GDK_onesixth = 0xab6, GDK_fivesixths = 0xab7, GDK_careof = 0xab8,
			GDK_figdash = 0xabb, GDK_leftanglebracket = 0xabc, GDK_decimalpoint = 0xabd, GDK_rightanglebracket = 0xabe,
			GDK_marker = 0xabf, GDK_oneeighth = 0xac3, GDK_threeeighths = 0xac4, GDK_fiveeighths = 0xac5,
			GDK_seveneighths = 0xac6, GDK_trademark = 0xac9, GDK_signaturemark = 0xaca, GDK_trademarkincircle = 0xacb,
			GDK_leftopentriangle = 0xacc, GDK_rightopentriangle = 0xacd, GDK_emopencircle = 0xace,
			GDK_emopenrectangle = 0xacf, GDK_leftsinglequotemark = 0xad0, GDK_rightsinglequotemark = 0xad1,
			GDK_leftdoublequotemark = 0xad2, GDK_rightdoublequotemark = 0xad3, GDK_prescription = 0xad4,
			GDK_minutes = 0xad6, GDK_seconds = 0xad7, GDK_latincross = 0xad9, GDK_hexagram = 0xada,
			GDK_filledrectbullet = 0xadb, GDK_filledlefttribullet = 0xadc, GDK_filledrighttribullet = 0xadd,
			GDK_emfilledcircle = 0xade, GDK_emfilledrect = 0xadf, GDK_enopencircbullet = 0xae0,
			GDK_enopensquarebullet = 0xae1, GDK_openrectbullet = 0xae2, GDK_opentribulletup = 0xae3,
			GDK_opentribulletdown = 0xae4, GDK_openstar = 0xae5, GDK_enfilledcircbullet = 0xae6,
			GDK_enfilledsqbullet = 0xae7, GDK_filledtribulletup = 0xae8, GDK_filledtribulletdown = 0xae9,
			GDK_leftpointer = 0xaea, GDK_rightpointer = 0xaeb, GDK_club = 0xaec, GDK_diamond = 0xaed,
			GDK_heart = 0xaee, GDK_maltesecross = 0xaf0, GDK_dagger = 0xaf1, GDK_doubledagger = 0xaf2,
			GDK_checkmark = 0xaf3, GDK_ballotcross = 0xaf4, GDK_musicalsharp = 0xaf5, GDK_musicalflat = 0xaf6,
			GDK_malesymbol = 0xaf7, GDK_femalesymbol = 0xaf8, GDK_telephone = 0xaf9, GDK_telephonerecorder = 0xafa,
			GDK_phonographcopyright = 0xafb, GDK_caret = 0xafc, GDK_singlelowquotemark = 0xafd,
			GDK_doublelowquotemark = 0xafe, GDK_cursor = 0xaff, GDK_leftcaret = 0xba3, GDK_rightcaret = 0xba6,
			GDK_downcaret = 0xba8, GDK_upcaret = 0xba9, GDK_overbar = 0xbc0, GDK_downtack = 0xbc2, GDK_upshoe = 0xbc3,
			GDK_downstile = 0xbc4, GDK_underbar = 0xbc6, GDK_jot = 0xbca, GDK_quad = 0xbcc, GDK_uptack = 0xbce,
			GDK_circle = 0xbcf, GDK_upstile = 0xbd3, GDK_downshoe = 0xbd6, GDK_rightshoe = 0xbd8, GDK_leftshoe = 0xbda,
			GDK_lefttack = 0xbdc, GDK_righttack = 0xbfc, GDK_hebrew_doublelowline = 0xcdf, GDK_hebrew_aleph = 0xce0,
			GDK_hebrew_bet = 0xce1, GDK_hebrew_beth = 0xce1, GDK_hebrew_gimel = 0xce2, GDK_hebrew_gimmel = 0xce2,
			GDK_hebrew_dalet = 0xce3, GDK_hebrew_daleth = 0xce3, GDK_hebrew_he = 0xce4, GDK_hebrew_waw = 0xce5,
			GDK_hebrew_zain = 0xce6, GDK_hebrew_zayin = 0xce6, GDK_hebrew_chet = 0xce7, GDK_hebrew_het = 0xce7,
			GDK_hebrew_tet = 0xce8, GDK_hebrew_teth = 0xce8, GDK_hebrew_yod = 0xce9, GDK_hebrew_finalkaph = 0xcea,
			GDK_hebrew_kaph = 0xceb, GDK_hebrew_lamed = 0xcec, GDK_hebrew_finalmem = 0xced, GDK_hebrew_mem = 0xcee,
			GDK_hebrew_finalnun = 0xcef, GDK_hebrew_nun = 0xcf0, GDK_hebrew_samech = 0xcf1, GDK_hebrew_samekh = 0xcf1,
			GDK_hebrew_ayin = 0xcf2, GDK_hebrew_finalpe = 0xcf3, GDK_hebrew_pe = 0xcf4, GDK_hebrew_finalzade = 0xcf5,
			GDK_hebrew_finalzadi = 0xcf5, GDK_hebrew_zade = 0xcf6, GDK_hebrew_zadi = 0xcf6, GDK_hebrew_qoph = 0xcf7,
			GDK_hebrew_kuf = 0xcf7, GDK_hebrew_resh = 0xcf8, GDK_hebrew_shin = 0xcf9, GDK_hebrew_taw = 0xcfa,
			GDK_hebrew_taf = 0xcfa, GDK_Hebrew_switch = 0xff7e, GDK_Thai_kokai = 0xda1, GDK_Thai_khokhai = 0xda2,
			GDK_Thai_khokhuat = 0xda3, GDK_Thai_khokhwai = 0xda4, GDK_Thai_khokhon = 0xda5,
			GDK_Thai_khorakhang = 0xda6, GDK_Thai_ngongu = 0xda7, GDK_Thai_chochan = 0xda8, GDK_Thai_choching = 0xda9,
			GDK_Thai_chochang = 0xdaa, GDK_Thai_soso = 0xdab, GDK_Thai_chochoe = 0xdac, GDK_Thai_yoying = 0xdad,
			GDK_Thai_dochada = 0xdae, GDK_Thai_topatak = 0xdaf, GDK_Thai_thothan = 0xdb0,
			GDK_Thai_thonangmontho = 0xdb1, GDK_Thai_thophuthao = 0xdb2, GDK_Thai_nonen = 0xdb3,
			GDK_Thai_dodek = 0xdb4, GDK_Thai_totao = 0xdb5, GDK_Thai_thothung = 0xdb6, GDK_Thai_thothahan = 0xdb7,
			GDK_Thai_thothong = 0xdb8, GDK_Thai_nonu = 0xdb9, GDK_Thai_bobaimai = 0xdba, GDK_Thai_popla = 0xdbb,
			GDK_Thai_phophung = 0xdbc, GDK_Thai_fofa = 0xdbd, GDK_Thai_phophan = 0xdbe, GDK_Thai_fofan = 0xdbf,
			GDK_Thai_phosamphao = 0xdc0, GDK_Thai_moma = 0xdc1, GDK_Thai_yoyak = 0xdc2, GDK_Thai_rorua = 0xdc3,
			GDK_Thai_ru = 0xdc4, GDK_Thai_loling = 0xdc5, GDK_Thai_lu = 0xdc6, GDK_Thai_wowaen = 0xdc7,
			GDK_Thai_sosala = 0xdc8, GDK_Thai_sorusi = 0xdc9, GDK_Thai_sosua = 0xdca, GDK_Thai_hohip = 0xdcb,
			GDK_Thai_lochula = 0xdcc, GDK_Thai_oang = 0xdcd, GDK_Thai_honokhuk = 0xdce, GDK_Thai_paiyannoi = 0xdcf,
			GDK_Thai_saraa = 0xdd0, GDK_Thai_maihanakat = 0xdd1, GDK_Thai_saraaa = 0xdd2, GDK_Thai_saraam = 0xdd3,
			GDK_Thai_sarai = 0xdd4, GDK_Thai_saraii = 0xdd5, GDK_Thai_saraue = 0xdd6, GDK_Thai_sarauee = 0xdd7,
			GDK_Thai_sarau = 0xdd8, GDK_Thai_sarauu = 0xdd9, GDK_Thai_phinthu = 0xdda,
			GDK_Thai_maihanakat_maitho = 0xdde, GDK_Thai_baht = 0xddf, GDK_Thai_sarae = 0xde0, GDK_Thai_saraae = 0xde1,
			GDK_Thai_sarao = 0xde2, GDK_Thai_saraaimaimuan = 0xde3, GDK_Thai_saraaimaimalai = 0xde4,
			GDK_Thai_lakkhangyao = 0xde5, GDK_Thai_maiyamok = 0xde6, GDK_Thai_maitaikhu = 0xde7,
			GDK_Thai_maiek = 0xde8, GDK_Thai_maitho = 0xde9, GDK_Thai_maitri = 0xdea, GDK_Thai_maichattawa = 0xdeb,
			GDK_Thai_thanthakhat = 0xdec, GDK_Thai_nikhahit = 0xded, GDK_Thai_leksun = 0xdf0, GDK_Thai_leknung = 0xdf1,
			GDK_Thai_leksong = 0xdf2, GDK_Thai_leksam = 0xdf3, GDK_Thai_leksi = 0xdf4, GDK_Thai_lekha = 0xdf5,
			GDK_Thai_lekhok = 0xdf6, GDK_Thai_lekchet = 0xdf7, GDK_Thai_lekpaet = 0xdf8, GDK_Thai_lekkao = 0xdf9,
			GDK_Hangul = 0xff31, GDK_Hangul_Start = 0xff32, GDK_Hangul_End = 0xff33, GDK_Hangul_Hanja = 0xff34,
			GDK_Hangul_Jamo = 0xff35, GDK_Hangul_Romaja = 0xff36, GDK_Hangul_Codeinput = 0xff37,
			GDK_Hangul_Jeonja = 0xff38, GDK_Hangul_Banja = 0xff39, GDK_Hangul_PreHanja = 0xff3a,
			GDK_Hangul_PostHanja = 0xff3b, GDK_Hangul_SingleCandidate = 0xff3c, GDK_Hangul_MultipleCandidate = 0xff3d,
			GDK_Hangul_PreviousCandidate = 0xff3e, GDK_Hangul_Special = 0xff3f, GDK_Hangul_switch = 0xff7e,
			GDK_Hangul_Kiyeog = 0xea1, GDK_Hangul_SsangKiyeog = 0xea2, GDK_Hangul_KiyeogSios = 0xea3,
			GDK_Hangul_Nieun = 0xea4, GDK_Hangul_NieunJieuj = 0xea5, GDK_Hangul_NieunHieuh = 0xea6,
			GDK_Hangul_Dikeud = 0xea7, GDK_Hangul_SsangDikeud = 0xea8, GDK_Hangul_Rieul = 0xea9,
			GDK_Hangul_RieulKiyeog = 0xeaa, GDK_Hangul_RieulMieum = 0xeab, GDK_Hangul_RieulPieub = 0xeac,
			GDK_Hangul_RieulSios = 0xead, GDK_Hangul_RieulTieut = 0xeae, GDK_Hangul_RieulPhieuf = 0xeaf,
			GDK_Hangul_RieulHieuh = 0xeb0, GDK_Hangul_Mieum = 0xeb1, GDK_Hangul_Pieub = 0xeb2,
			GDK_Hangul_SsangPieub = 0xeb3, GDK_Hangul_PieubSios = 0xeb4, GDK_Hangul_Sios = 0xeb5,
			GDK_Hangul_SsangSios = 0xeb6, GDK_Hangul_Ieung = 0xeb7, GDK_Hangul_Jieuj = 0xeb8,
			GDK_Hangul_SsangJieuj = 0xeb9, GDK_Hangul_Cieuc = 0xeba, GDK_Hangul_Khieuq = 0xebb,
			GDK_Hangul_Tieut = 0xebc, GDK_Hangul_Phieuf = 0xebd, GDK_Hangul_Hieuh = 0xebe, GDK_Hangul_A = 0xebf,
			GDK_Hangul_AE = 0xec0, GDK_Hangul_YA = 0xec1, GDK_Hangul_YAE = 0xec2, GDK_Hangul_EO = 0xec3,
			GDK_Hangul_E = 0xec4, GDK_Hangul_YEO = 0xec5, GDK_Hangul_YE = 0xec6, GDK_Hangul_O = 0xec7,
			GDK_Hangul_WA = 0xec8, GDK_Hangul_WAE = 0xec9, GDK_Hangul_OE = 0xeca, GDK_Hangul_YO = 0xecb,
			GDK_Hangul_U = 0xecc, GDK_Hangul_WEO = 0xecd, GDK_Hangul_WE = 0xece, GDK_Hangul_WI = 0xecf,
			GDK_Hangul_YU = 0xed0, GDK_Hangul_EU = 0xed1, GDK_Hangul_YI = 0xed2, GDK_Hangul_I = 0xed3,
			GDK_Hangul_J_Kiyeog = 0xed4, GDK_Hangul_J_SsangKiyeog = 0xed5, GDK_Hangul_J_KiyeogSios = 0xed6,
			GDK_Hangul_J_Nieun = 0xed7, GDK_Hangul_J_NieunJieuj = 0xed8, GDK_Hangul_J_NieunHieuh = 0xed9,
			GDK_Hangul_J_Dikeud = 0xeda, GDK_Hangul_J_Rieul = 0xedb, GDK_Hangul_J_RieulKiyeog = 0xedc,
			GDK_Hangul_J_RieulMieum = 0xedd, GDK_Hangul_J_RieulPieub = 0xede, GDK_Hangul_J_RieulSios = 0xedf,
			GDK_Hangul_J_RieulTieut = 0xee0, GDK_Hangul_J_RieulPhieuf = 0xee1, GDK_Hangul_J_RieulHieuh = 0xee2,
			GDK_Hangul_J_Mieum = 0xee3, GDK_Hangul_J_Pieub = 0xee4, GDK_Hangul_J_PieubSios = 0xee5,
			GDK_Hangul_J_Sios = 0xee6, GDK_Hangul_J_SsangSios = 0xee7, GDK_Hangul_J_Ieung = 0xee8,
			GDK_Hangul_J_Jieuj = 0xee9, GDK_Hangul_J_Cieuc = 0xeea, GDK_Hangul_J_Khieuq = 0xeeb,
			GDK_Hangul_J_Tieut = 0xeec, GDK_Hangul_J_Phieuf = 0xeed, GDK_Hangul_J_Hieuh = 0xeee,
			GDK_Hangul_RieulYeorinHieuh = 0xeef, GDK_Hangul_SunkyeongeumMieum = 0xef0,
			GDK_Hangul_SunkyeongeumPieub = 0xef1, GDK_Hangul_PanSios = 0xef2, GDK_Hangul_KkogjiDalrinIeung = 0xef3,
			GDK_Hangul_SunkyeongeumPhieuf = 0xef4, GDK_Hangul_YeorinHieuh = 0xef5, GDK_Hangul_AraeA = 0xef6,
			GDK_Hangul_AraeAE = 0xef7, GDK_Hangul_J_PanSios = 0xef8, GDK_Hangul_J_KkogjiDalrinIeung = 0xef9,
			GDK_Hangul_J_YeorinHieuh = 0xefa, GDK_Korean_Won = 0xeff, GDK_Armenian_ligature_ew = 0x1000587,
			GDK_Armenian_full_stop = 0x1000589, GDK_Armenian_verjaket = 0x1000589,
			GDK_Armenian_separation_mark = 0x100055d, GDK_Armenian_but = 0x100055d, GDK_Armenian_hyphen = 0x100058a,
			GDK_Armenian_yentamna = 0x100058a, GDK_Armenian_exclam = 0x100055c, GDK_Armenian_amanak = 0x100055c,
			GDK_Armenian_accent = 0x100055b, GDK_Armenian_shesht = 0x100055b, GDK_Armenian_question = 0x100055e,
			GDK_Armenian_paruyk = 0x100055e, GDK_Armenian_AYB = 0x1000531, GDK_Armenian_ayb = 0x1000561,
			GDK_Armenian_BEN = 0x1000532, GDK_Armenian_ben = 0x1000562, GDK_Armenian_GIM = 0x1000533,
			GDK_Armenian_gim = 0x1000563, GDK_Armenian_DA = 0x1000534, GDK_Armenian_da = 0x1000564,
			GDK_Armenian_YECH = 0x1000535, GDK_Armenian_yech = 0x1000565, GDK_Armenian_ZA = 0x1000536,
			GDK_Armenian_za = 0x1000566, GDK_Armenian_E = 0x1000537, GDK_Armenian_e = 0x1000567,
			GDK_Armenian_AT = 0x1000538, GDK_Armenian_at = 0x1000568, GDK_Armenian_TO = 0x1000539,
			GDK_Armenian_to = 0x1000569, GDK_Armenian_ZHE = 0x100053a, GDK_Armenian_zhe = 0x100056a,
			GDK_Armenian_INI = 0x100053b, GDK_Armenian_ini = 0x100056b, GDK_Armenian_LYUN = 0x100053c,
			GDK_Armenian_lyun = 0x100056c, GDK_Armenian_KHE = 0x100053d, GDK_Armenian_khe = 0x100056d,
			GDK_Armenian_TSA = 0x100053e, GDK_Armenian_tsa = 0x100056e, GDK_Armenian_KEN = 0x100053f,
			GDK_Armenian_ken = 0x100056f, GDK_Armenian_HO = 0x1000540, GDK_Armenian_ho = 0x1000570,
			GDK_Armenian_DZA = 0x1000541, GDK_Armenian_dza = 0x1000571, GDK_Armenian_GHAT = 0x1000542,
			GDK_Armenian_ghat = 0x1000572, GDK_Armenian_TCHE = 0x1000543, GDK_Armenian_tche = 0x1000573,
			GDK_Armenian_MEN = 0x1000544, GDK_Armenian_men = 0x1000574, GDK_Armenian_HI = 0x1000545,
			GDK_Armenian_hi = 0x1000575, GDK_Armenian_NU = 0x1000546, GDK_Armenian_nu = 0x1000576,
			GDK_Armenian_SHA = 0x1000547, GDK_Armenian_sha = 0x1000577, GDK_Armenian_VO = 0x1000548,
			GDK_Armenian_vo = 0x1000578, GDK_Armenian_CHA = 0x1000549, GDK_Armenian_cha = 0x1000579,
			GDK_Armenian_PE = 0x100054a, GDK_Armenian_pe = 0x100057a, GDK_Armenian_JE = 0x100054b,
			GDK_Armenian_je = 0x100057b, GDK_Armenian_RA = 0x100054c, GDK_Armenian_ra = 0x100057c,
			GDK_Armenian_SE = 0x100054d, GDK_Armenian_se = 0x100057d, GDK_Armenian_VEV = 0x100054e,
			GDK_Armenian_vev = 0x100057e, GDK_Armenian_TYUN = 0x100054f, GDK_Armenian_tyun = 0x100057f,
			GDK_Armenian_RE = 0x1000550, GDK_Armenian_re = 0x1000580, GDK_Armenian_TSO = 0x1000551,
			GDK_Armenian_tso = 0x1000581, GDK_Armenian_VYUN = 0x1000552, GDK_Armenian_vyun = 0x1000582,
			GDK_Armenian_PYUR = 0x1000553, GDK_Armenian_pyur = 0x1000583, GDK_Armenian_KE = 0x1000554,
			GDK_Armenian_ke = 0x1000584, GDK_Armenian_O = 0x1000555, GDK_Armenian_o = 0x1000585,
			GDK_Armenian_FE = 0x1000556, GDK_Armenian_fe = 0x1000586, GDK_Armenian_apostrophe = 0x100055a,
			GDK_Georgian_an = 0x10010d0, GDK_Georgian_ban = 0x10010d1, GDK_Georgian_gan = 0x10010d2,
			GDK_Georgian_don = 0x10010d3, GDK_Georgian_en = 0x10010d4, GDK_Georgian_vin = 0x10010d5,
			GDK_Georgian_zen = 0x10010d6, GDK_Georgian_tan = 0x10010d7, GDK_Georgian_in = 0x10010d8,
			GDK_Georgian_kan = 0x10010d9, GDK_Georgian_las = 0x10010da, GDK_Georgian_man = 0x10010db,
			GDK_Georgian_nar = 0x10010dc, GDK_Georgian_on = 0x10010dd, GDK_Georgian_par = 0x10010de,
			GDK_Georgian_zhar = 0x10010df, GDK_Georgian_rae = 0x10010e0, GDK_Georgian_san = 0x10010e1,
			GDK_Georgian_tar = 0x10010e2, GDK_Georgian_un = 0x10010e3, GDK_Georgian_phar = 0x10010e4,
			GDK_Georgian_khar = 0x10010e5, GDK_Georgian_ghan = 0x10010e6, GDK_Georgian_qar = 0x10010e7,
			GDK_Georgian_shin = 0x10010e8, GDK_Georgian_chin = 0x10010e9, GDK_Georgian_can = 0x10010ea,
			GDK_Georgian_jil = 0x10010eb, GDK_Georgian_cil = 0x10010ec, GDK_Georgian_char = 0x10010ed,
			GDK_Georgian_xan = 0x10010ee, GDK_Georgian_jhan = 0x10010ef, GDK_Georgian_hae = 0x10010f0,
			GDK_Georgian_he = 0x10010f1, GDK_Georgian_hie = 0x10010f2, GDK_Georgian_we = 0x10010f3,
			GDK_Georgian_har = 0x10010f4, GDK_Georgian_hoe = 0x10010f5, GDK_Georgian_fi = 0x10010f6,
			GDK_Xabovedot = 0x1001e8a, GDK_Ibreve = 0x100012c, GDK_Zstroke = 0x10001b5, GDK_Gcaron = 0x10001e6,
			GDK_Ocaron = 0x10001d1, GDK_Obarred = 0x100019f, GDK_xabovedot = 0x1001e8b, GDK_ibreve = 0x100012d,
			GDK_zstroke = 0x10001b6, GDK_gcaron = 0x10001e7, GDK_ocaron = 0x10001d2, GDK_obarred = 0x1000275,
			GDK_SCHWA = 0x100018f, GDK_schwa = 0x1000259, GDK_Lbelowdot = 0x1001e36, GDK_lbelowdot = 0x1001e37,
			GDK_Abelowdot = 0x1001ea0, GDK_abelowdot = 0x1001ea1, GDK_Ahook = 0x1001ea2, GDK_ahook = 0x1001ea3,
			GDK_Acircumflexacute = 0x1001ea4, GDK_acircumflexacute = 0x1001ea5, GDK_Acircumflexgrave = 0x1001ea6,
			GDK_acircumflexgrave = 0x1001ea7, GDK_Acircumflexhook = 0x1001ea8, GDK_acircumflexhook = 0x1001ea9,
			GDK_Acircumflextilde = 0x1001eaa, GDK_acircumflextilde = 0x1001eab, GDK_Acircumflexbelowdot = 0x1001eac,
			GDK_acircumflexbelowdot = 0x1001ead, GDK_Abreveacute = 0x1001eae, GDK_abreveacute = 0x1001eaf,
			GDK_Abrevegrave = 0x1001eb0, GDK_abrevegrave = 0x1001eb1, GDK_Abrevehook = 0x1001eb2,
			GDK_abrevehook = 0x1001eb3, GDK_Abrevetilde = 0x1001eb4, GDK_abrevetilde = 0x1001eb5,
			GDK_Abrevebelowdot = 0x1001eb6, GDK_abrevebelowdot = 0x1001eb7, GDK_Ebelowdot = 0x1001eb8,
			GDK_ebelowdot = 0x1001eb9, GDK_Ehook = 0x1001eba, GDK_ehook = 0x1001ebb, GDK_Etilde = 0x1001ebc,
			GDK_etilde = 0x1001ebd, GDK_Ecircumflexacute = 0x1001ebe, GDK_ecircumflexacute = 0x1001ebf,
			GDK_Ecircumflexgrave = 0x1001ec0, GDK_ecircumflexgrave = 0x1001ec1, GDK_Ecircumflexhook = 0x1001ec2,
			GDK_ecircumflexhook = 0x1001ec3, GDK_Ecircumflextilde = 0x1001ec4, GDK_ecircumflextilde = 0x1001ec5,
			GDK_Ecircumflexbelowdot = 0x1001ec6, GDK_ecircumflexbelowdot = 0x1001ec7, GDK_Ihook = 0x1001ec8,
			GDK_ihook = 0x1001ec9, GDK_Ibelowdot = 0x1001eca, GDK_ibelowdot = 0x1001ecb, GDK_Obelowdot = 0x1001ecc,
			GDK_obelowdot = 0x1001ecd, GDK_Ohook = 0x1001ece, GDK_ohook = 0x1001ecf, GDK_Ocircumflexacute = 0x1001ed0,
			GDK_ocircumflexacute = 0x1001ed1, GDK_Ocircumflexgrave = 0x1001ed2, GDK_ocircumflexgrave = 0x1001ed3,
			GDK_Ocircumflexhook = 0x1001ed4, GDK_ocircumflexhook = 0x1001ed5, GDK_Ocircumflextilde = 0x1001ed6,
			GDK_ocircumflextilde = 0x1001ed7, GDK_Ocircumflexbelowdot = 0x1001ed8, GDK_ocircumflexbelowdot = 0x1001ed9,
			GDK_Ohornacute = 0x1001eda, GDK_ohornacute = 0x1001edb, GDK_Ohorngrave = 0x1001edc,
			GDK_ohorngrave = 0x1001edd, GDK_Ohornhook = 0x1001ede, GDK_ohornhook = 0x1001edf,
			GDK_Ohorntilde = 0x1001ee0, GDK_ohorntilde = 0x1001ee1, GDK_Ohornbelowdot = 0x1001ee2,
			GDK_ohornbelowdot = 0x1001ee3, GDK_Ubelowdot = 0x1001ee4, GDK_ubelowdot = 0x1001ee5, GDK_Uhook = 0x1001ee6,
			GDK_uhook = 0x1001ee7, GDK_Uhornacute = 0x1001ee8, GDK_uhornacute = 0x1001ee9, GDK_Uhorngrave = 0x1001eea,
			GDK_uhorngrave = 0x1001eeb, GDK_Uhornhook = 0x1001eec, GDK_uhornhook = 0x1001eed,
			GDK_Uhorntilde = 0x1001eee, GDK_uhorntilde = 0x1001eef, GDK_Uhornbelowdot = 0x1001ef0,
			GDK_uhornbelowdot = 0x1001ef1, GDK_Ybelowdot = 0x1001ef4, GDK_ybelowdot = 0x1001ef5, GDK_Yhook = 0x1001ef6,
			GDK_yhook = 0x1001ef7, GDK_Ytilde = 0x1001ef8, GDK_ytilde = 0x1001ef9, GDK_Ohorn = 0x10001a0,
			GDK_ohorn = 0x10001a1, GDK_Uhorn = 0x10001af, GDK_uhorn = 0x10001b0, GDK_EcuSign = 0x10020a0,
			GDK_ColonSign = 0x10020a1, GDK_CruzeiroSign = 0x10020a2, GDK_FFrancSign = 0x10020a3,
			GDK_LiraSign = 0x10020a4, GDK_MillSign = 0x10020a5, GDK_NairaSign = 0x10020a6, GDK_PesetaSign = 0x10020a7,
			GDK_RupeeSign = 0x10020a8, GDK_WonSign = 0x10020a9, GDK_NewSheqelSign = 0x10020aa,
			GDK_DongSign = 0x10020ab, GDK_EuroSign = 0x20ac, GDK_zerosuperior = 0x1002070,
			GDK_foursuperior = 0x1002074, GDK_fivesuperior = 0x1002075, GDK_sixsuperior = 0x1002076,
			GDK_sevensuperior = 0x1002077, GDK_eightsuperior = 0x1002078, GDK_ninesuperior = 0x1002079,
			GDK_zerosubscript = 0x1002080, GDK_onesubscript = 0x1002081, GDK_twosubscript = 0x1002082,
			GDK_threesubscript = 0x1002083, GDK_foursubscript = 0x1002084, GDK_fivesubscript = 0x1002085,
			GDK_sixsubscript = 0x1002086, GDK_sevensubscript = 0x1002087, GDK_eightsubscript = 0x1002088,
			GDK_ninesubscript = 0x1002089, GDK_partdifferential = 0x1002202, GDK_emptyset = 0x1002205,
			GDK_elementof = 0x1002208, GDK_notelementof = 0x1002209, GDK_containsas = 0x100220b,
			GDK_squareroot = 0x100221a, GDK_cuberoot = 0x100221b, GDK_fourthroot = 0x100221c,
			GDK_dintegral = 0x100222c, GDK_tintegral = 0x100222d, GDK_because = 0x1002235, GDK_approxeq = 0x1002248,
			GDK_notapproxeq = 0x1002247, GDK_notidentical = 0x1002262, GDK_stricteq = 0x1002263;

}
