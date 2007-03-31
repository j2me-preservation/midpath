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

package javax.microedition.lcdui;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.thenesis.midpath.ui.SDLToolkit;

import com.sun.midp.chameleon.CGraphicsUtil;
import com.sun.midp.chameleon.SubMenuCommand;
import com.sun.midp.chameleon.input.BasicTextInputSession;
import com.sun.midp.chameleon.input.InputMode;
import com.sun.midp.chameleon.input.TextInputComponent;
import com.sun.midp.chameleon.input.TextInputSession;
import com.sun.midp.chameleon.layers.InputModeLayer;
import com.sun.midp.chameleon.layers.PTILayer;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.TextFieldSkin;
import com.sun.midp.chameleon.skins.resources.TextFieldResources;
import com.sun.midp.lcdui.DynamicCharacterArray;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.PhoneDial;
import com.sun.midp.lcdui.Text;
import com.sun.midp.lcdui.TextCursor;
import com.sun.midp.lcdui.TextPolicy;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;

/**
 * This is the look &amps; feel implementation for TextField.
 */
class TextFieldLFImpl extends ItemLFImpl implements TextFieldLF, TextInputComponent, CommandListener {
	/** TextField instance associated with this view */
	protected TextField tf;

	/** cursor to keep track of where to draw the cursor */
	protected TextCursor cursor;

	/** editable state of the text field */
	protected boolean editable;

	/** flag indicating the first time traverse is executed */
	protected boolean firstTimeInTraverse = true;

	/** The character set to use as the initial input mode, may be null */
	protected String initialInputMode;

	/** The input mode cause the traverse out for the text component */
	protected InputMode interruptedIM;

	/** 
	 * This SubMenuCommand holds the set of InputModes available on the
	 * InputMode pull-out menu
	 */
	protected SubMenuCommand inputMenu;

	/** The TextInputMediator which handles translating key presses */
	protected static TextInputSession inputSession;

	/** The PopupLayer that represents open state of this ChoiceGroup POPUP. */
	protected static PTILayer pt_popup;

	/**
	 * The set of InputModes available to process this text component
	 */
	protected InputMode[] inputModes;

	/**
	 * A special "popup" layer that shows the user an indicator of
	 * what the currently selected input mode is
	 */
	protected InputModeLayer inputModeIndicator;

	/** The state of the popup ChoiceGroup (false by default) */
	private boolean pt_popupOpen;

	/** predictive text options */
	String[] pt_matches;

	/**
	 * A four dimensional array holding the anchor point, item
	 * height, and space below the item which corresponds to the
	 * parameters to the InputModeLayer's setAnchor() method
	 */
	protected int[] inputModeAnchor;

	/**
	 * This is a flag to turn on the input mode indicator popup
	 */
	protected boolean showIMPopup;

	/** 
	 * true if the preferredX field in TextCursor should be updated with
	 * the latest TextCursor.x coordinate 
	 */
	protected boolean usePreferredX = true;

	/**
	 * cached size[] structure, or null size has been invalidated
	 * and needs to be re-computed
	 */
	protected int[] cachedSize = null;

	/**
	 * pixel offset to the start of the text field  (for example,  if 
	 * xScrollOffset is -60 it means means that the text in this 
	 * text field is scrolled 60 pixels left of the left edge of the
	 * text field)
	 */
	protected int xScrollOffset;

	/**
	 * Total width of the text contained in this TextField
	 */
	protected int textWidth;

	/**
	 * Width of the scroll area for text
	 */
	protected int scrollWidth;

	/** A Timer which will handle firing repaints of the ScrollPainter */
	protected static Timer textScrollTimer;

	/** A TimerTask which will repaint scrolling text  on a repeated basis */
	protected TextScrollPainter textScrollPainter;

	/**
	 * Creates TextFieldLF for the passed in TextField.
	 * @param tf The TextField associated with this TextFieldLF
	 */
	TextFieldLFImpl(TextField tf) {
		super(tf);

		TextFieldResources.load();

		this.tf = tf;

		cursor = new TextCursor(tf.buffer.length());
		cursor.visible = false;
		xScrollOffset = 0;

		if (inputSession == null) {
			inputSession = new BasicTextInputSession();
			pt_popup = new PTILayer(inputSession);
		}

		lSetConstraints();

		if (textScrollTimer == null) {
			textScrollTimer = new Timer();
		}

		inputModeIndicator = new InputModeLayer();
		inputModeAnchor = new int[4];
	}

	// *****************************************************
	// Public methods defined in interfaces
	// *****************************************************

	/**
	 * Update the character buffer in TextField with pending user input.
	 * Since Java TextField always keeps TextField.buffer up-to-date,
	 * there is no pending user input. Do nothing but return false here.
	 * @return true if there is new user input updated in the buffer.
	 */
	public boolean lUpdateContents() {
		return false; // nothing pending
	}

	/**
	 * Notifies L&F of a content change in the corresponding TextField.
	 */
	public void lSetChars() {
		cursor.index = tf.buffer.length(); // cursor at the end
		cursor.option = Text.PAINT_USE_CURSOR_INDEX;
		if (!editable) {
			resetUneditable();
		}
		lRequestPaint();
	}

	/**
	 * Notifies L&amps;F of a character insertion in the corresponding
	 * TextField.
	 * @param data the source of the character data
	 * @param offset the beginning of the region of characters copied
	 * @param length the number of characters copied
	 * @param position the position at which insertion occurred
	 */
	public void lInsert(char data[], int offset, int length, int position) {
		if (data == null) {
			return; // -au
		}
		if (position <= cursor.index) {
			cursor.index += length;
			cursor.option = Text.PAINT_USE_CURSOR_INDEX;
		}
		if (!editable) {
			resetUneditable();
		}
		if (item.owner == null) {
			return; // because owner is null, we just return.
		}
		lRequestPaint();
	}

	/**
	 * Notifies L&amsp;F of character deletion in the corresponding
	 * TextField.
	 * @param offset the beginning of the deleted region
	 * @param length the number of characters deleted
	 */
	public void lDelete(int offset, int length) {
		if (cursor.index >= offset) {
			int diff = cursor.index - offset;
			cursor.index -= (diff < length) ? diff : length;
			cursor.option = Text.PAINT_USE_CURSOR_INDEX;
		}
		if (!editable) {
			resetUneditable();
		}
		if (item.owner == null) {
			return; // because owner is null, we just return.
		}
		lRequestPaint();
	}

	/**
	 * Notifies L&amps;F of a maximum size change in the corresponding
	 * TextField.
	 * @param maxSize - the new maximum size
	 */
	public void lSetMaxSize(int maxSize) {
		cachedSize = null; // contentSize has changed, so flush cache
		int max = tf.getMaxSize();
		if (cursor.index > max) {
			cursor.index = max;
		}
		lRequestInvalidate(true, true);
	}

	/**
	 * Returns the available size (number of characters) that can be
	 * stored in this <code>TextInputComponent</code>.
	 * @return available size in characters
	 */
	public int getAvailableSize() {
		return tf.getMaxSize() - tf.buffer.length();
	}

	/**
	 * Gets the current input position.
	 * @return the current caret position, <code>0</code> if at the beginning
	 */
	public int lGetCaretPosition() {
		return cursor.index;
	}

	/**
	 * Update states associated with input constraints.
	 * 
	 * @param autoScrolling true if auto scrolling is allowed
	 *                      to show large uneditable contents
	 */
	void setConstraintsCommon(boolean autoScrolling) {
		// Cleanup old states that are constraints sensitive
		if (hasFocus && visible) {
			if (editable) {
				disableInput();
			} else if (autoScrolling) {
				stopScroll();
			}
		}

		// Change editability
		editable = (tf.constraints & TextField.UNEDITABLE) != TextField.UNEDITABLE;

		// Setup new states that are constraints sensitive
		if (hasFocus && visible) {
			if (editable) {
				enableInput();
			} else if (autoScrolling) {
				startScroll();
			}
		}
	}

	/**
	 * Notifies L&amps;F that constraints have to be changed.
	 */
	public void lSetConstraints() {
		setConstraintsCommon(true);

		// The layout might change if the constraints does not match
		// the current text, causing the text to be set empty,
		// or changed to "password", causing it to change width
		// Request relayout to based on updated contentSize
		cachedSize = null;
		if (item.owner == null) {
			return; // because owner is null, we just return.
		}
		lRequestInvalidate(true, true);
	}

	/**
	 * Validate a given character array against a constraints.
	 *
	 * @param buffer a character array
	 * @param constraints text input constraints
	 * @return true if constraints is met by the character array
	 */
	public boolean lValidate(DynamicCharacterArray buffer, int constraints) {
		return TextPolicy.isValidString(buffer, constraints);
	}

	/**
	 * Notifies L&amps;F that preferred initial input mode was changed.
	 * @param characterSubset a string naming a Unicode character subset,
	 * or <code>null</code>
	 */
	public void lSetInitialInputMode(String characterSubset) {
		this.initialInputMode = characterSubset;
	}

	// CommandListener interface

	/**
	 * This CommandListener is only for the selection of Commands on
	 * the Input sub-menu, which lists each of the available InputModes
	 * for this text component.
	 * @param c command
	 * @param d displayable
	 */
	public void commandAction(Command c, Displayable d) {
		Command[] inputCommands = inputMenu.getSubCommands();
		String label = c.getLabel();
		if (inputCommands != null && label != null) {
			for (int i = 0; i < inputCommands.length; i++) {
				if (label.equals(inputCommands[i].getLabel())) {
					inputSession.setCurrentInputMode(inputModes[i]);
					break;
				}
			}
		}
	}

	// *****************************************************
	//  Package private methods
	// *****************************************************

	/**
	 * Indicate whether or not traversing should occur.
	 *
	 * @return <code>false</code> always
	 */
	boolean shouldSkipTraverse() {
		return false;
	}

	/**
	 * Sets the content size in the passed in array.
	 * Content is calculated based on the availableWidth.
	 * size[WIDTH] and size[HEIGHT] should be set by this method.
	 * @param size The array that holds Item content size and location 
	 *             in Item internal bounds coordinate system.
	 * @param availableWidth The width available for this Item
	 */
	void lGetContentSize(int size[], int availableWidth) {
		if (cachedSize == null) {
			Font f = ScreenSkin.FONT_INPUT_TEXT;
			size[HEIGHT] = f.getHeight() + (2 * TextFieldSkin.PAD_V);
			size[WIDTH] = f.charWidth('W') * tf.buffer.capacity() + (2 * TextFieldSkin.PAD_H);
			if (size[WIDTH] > availableWidth) {
				size[WIDTH] = availableWidth;
			}
			cachedSize = new int[] { 0, 0, size[WIDTH], size[HEIGHT] };
		} else { // use cached size here
			size[WIDTH] = cachedSize[WIDTH];
			size[HEIGHT] = cachedSize[HEIGHT];
		}

		// update scrollWidth used in scrolling UE text
		scrollWidth = cachedSize[WIDTH] - (2 * TextFieldSkin.PAD_H) - 1;
	}

	/**
	 * Determine if this Item should have a newline after it
	 *
	 * @return true if it should have a newline after
	 */
	boolean equateNLA() {
		if (super.equateNLA()) {
			return true;
		}
		return ((tf.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
	}

	/**     
	 * Determine if this Item should have a newline before it
	 *
	 * @return true if it should have a newline before
	 */
	boolean equateNLB() {
		if (super.equateNLB()) {
			return true;
		}
		return ((tf.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
	}

	/**
	 * Paints the content area of this TextField.
	 * Graphics is translated to contents origin.
	 * @param g The graphics where Item content should be painted
	 * @param width The width available for the Item's content
	 * @param height The height available for the Item's content
	 */
	void lPaintContent(Graphics g, int width, int height) {
		// Draw the TextField background region 
		if (editable) {
			if (TextFieldSkin.IMAGE_BG != null) {
				CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height, TextFieldSkin.IMAGE_BG);
			} else {
				// draw widget instead of using images
				CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height, TextFieldSkin.COLOR_BORDER,
						TextFieldSkin.COLOR_BORDER_SHD, TextFieldSkin.COLOR_BG);
			}
		} else {
			if (TextFieldSkin.IMAGE_BG_UE != null) {
				CGraphicsUtil.draw9pcsBackground(g, 0, 0, width, height, TextFieldSkin.IMAGE_BG_UE);
			} else {
				// draw widget instead of using images
				CGraphicsUtil.drawDropShadowBox(g, 0, 0, width, height, TextFieldSkin.COLOR_BORDER_UE,
						TextFieldSkin.COLOR_BORDER_SHD_UE, TextFieldSkin.COLOR_BG_UE);
			}
		}

		// NOTE: for uneditable textfields that gain focus,
		// we're simply inverting their fg/bg color values
		// from the skin

		// draw hilight box if TextField has focus
		if (hasFocus) {
			g.setColor(editable ? ScreenSkin.COLOR_BG_HL : TextFieldSkin.COLOR_FG_UE);
			g.fillRect(2, 2, width - 3, height - 3);
		}

		// We need to translate by 1 more pixel horizontally 
		// to reserve space for cursor in the empty textfield
		g.setClip(TextFieldSkin.PAD_H, TextFieldSkin.PAD_V, width - (2 * TextFieldSkin.PAD_H), height
				- (2 * TextFieldSkin.PAD_V));
		g.translate(TextFieldSkin.PAD_H + 1, TextFieldSkin.PAD_V);

		int clr;
		if (hasFocus) {
			clr = (editable ? ScreenSkin.COLOR_FG_HL : TextFieldSkin.COLOR_BG_UE);
		} else {
			clr = (editable ? TextFieldSkin.COLOR_FG : TextFieldSkin.COLOR_FG_UE);
		}

		xScrollOffset = paint(g, tf.buffer, inputSession.getPendingChar(), tf.constraints, ScreenSkin.FONT_INPUT_TEXT,
				clr, width - (2 * TextFieldSkin.PAD_H), height - (2 * TextFieldSkin.PAD_V), xScrollOffset, Text.NORMAL,
				cursor);

		g.translate(-(TextFieldSkin.PAD_H + 1), -TextFieldSkin.PAD_V);

		if (usePreferredX) {
			cursor.preferredX = cursor.x;
		}
	}

	/**
	 * Returns the string that would be stored. This may not be the
	 * same as the string that is actually displayed because of the
	 * options argument that affects painting.
	 *
	 * @param dca the displayed text
	 * @param constraints text constraints
	 * @param cursor text cursor object to use to draw vertical bar
	 * @param modifyCursor true if this method can modify the cursor
	 *        object if necessary, false otherwise
	 * @return the string that will be stored in buffer. This may not be what
	 *                is actually drawn depending on the options. If it's
	 *                impossible to get the actual string null is returned 
	 */
	public String getBufferString(DynamicCharacterArray dca, int constraints, TextCursor cursor, boolean modifyCursor) {

		String ret = null;
		if (!bufferedTheSameAsDisplayed(constraints)) {
			DynamicCharacterArray out = new DynamicCharacterArray(dca.length());

			if (!modifyCursor) {
				cursor = new TextCursor(cursor);
			}

			if ((constraints & TextField.CONSTRAINT_MASK) == TextField.PHONENUMBER) {
				for (int i = 0, j = 0; i < dca.length(); i++) {
					char next = dca.charAt(i);
					if (next == ' ') {
						if (cursor.index > i)
							cursor.index--;
					} else {
						out.insert(j++, next);
					}
				}
			}
			ret = out.toString();
		} else {
			ret = dca.toString();
		}
		return ret;
	}

	/**
	 * Returns the string that would be painted. This may not be the
	 * same as the string that is actually displayed because of the
	 * options argument that affects painting.
	 *
	 * @param dca the text to paint
	 * @param opChar option char 
	 * @param constraints text constraints
	 * @param cursor text cursor object to use to draw vertical bar
	 * @param modifyCursor true if this method can modify the cursor
	 *        object if necessary, false otherwise
	 * @return the string that will be sent to be drawn. this may not be what
	 *                is actually drawn depending on the options
	 */
	// IMPL NOTE:  this is slow to call repeatedly...
	// perhaps we cache a DisplayString?
	public String getDisplayString(DynamicCharacterArray dca, char opChar, int constraints, TextCursor cursor,
			boolean modifyCursor) {
		log("[tf.getDisplayString]");
		DynamicCharacterArray out = new DynamicCharacterArray(dca.length() + 1);

		int index = cursor == null ? dca.length() : cursor.index;

		if ((constraints & TextField.PASSWORD) == TextField.PASSWORD) {
			index = getStringForPassword(dca, constraints, index, opChar, out);
		} else { // not password
			out.insert(dca.toCharArray(), 0, dca.length(), 0);

			switch (constraints & TextField.CONSTRAINT_MASK) {
			case TextField.PHONENUMBER:
				index = getStringForPhoneNumber(dca, index, opChar, out);
				break;
			case TextField.DECIMAL:
				index = getStringForDecimal(dca, index, opChar, out);
				break;
			case TextField.NUMERIC:
				index = getStringForNumeric(dca, index, opChar, out);
				break;
			case TextField.EMAILADDR:
			case TextField.URL:
			case TextField.ANY:
				if (opChar > 0 && dca.length() < tf.getMaxSize()) {
					out.insert(index++, opChar);
				}
				break;
			default:
				// for safety/completeness.
				Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI, "TextFieldLFImpl: constraints=" + constraints);
				if (opChar > 0 && dca.length() < tf.getMaxSize()) {
					out.insert(index++, opChar);
				}
				break;
			}
		}

		if (out == null) {
			out = dca;
		}

		if (modifyCursor && cursor != null) {
			cursor.index = index;
		}

		log("[TF.getDisplayString] getMatchList:");
		pt_matches = inputSession.getMatchList();

		return out.toString();
	}

	/**
	 * Returns the string that would be painted for the numeric constraints.
	 *
	 * @param dca the text to paint
	 * @param index cursor index 
	 * @param opChar option char 
	 * @param out the string that will be sent to be drawn. 
	 * @return new cursor index
	 */
	private int getStringForNumeric(DynamicCharacterArray dca, int index, char opChar, DynamicCharacterArray out) {
		// change the number sign
		if (' ' == opChar) {
			if (dca.charAt(0) == '-') {
				out.delete(0, 1);
				if (index > 0)
					index--;
			} else if (dca.length() < tf.getMaxSize()) {
				out.insert(0, '-');
				index++;
			}
		} else if (opChar > 0 && dca.length() < tf.getMaxSize()) {
			out.insert(index++, opChar);
		}
		return index;
	}

	/**
	 * Returns the string that would be painted for the decimal constraints.
	 *
	 * @param dca the text to paint
	 * @param index cursor index 
	 * @param opChar option char 
	 * @param out the string that will be sent to be drawn. 
	 * @return new cursor index
	 */
	private int getStringForDecimal(DynamicCharacterArray dca, int index, char opChar, DynamicCharacterArray out) {
		// it can be extended by '0' if '.' is added at the beginning 
		out.setCapacity(dca.length() + 1 + 1);
		// change the number sign
		if (' ' == opChar) {
			if (dca.charAt(0) == '-') {
				out.delete(0, 1);
				if (index > 0)
					index--;
			} else if (dca.length() < tf.getMaxSize()) {
				out.insert(0, '-');
				index++;
			}
			opChar = 0;
		} else if ('.' == opChar && dca.length() < tf.getMaxSize()) {
			/**
			 * insert the '.'. If it's inserted at the beginning of the number
			 * '0' has to be added before the '.' Number can not contain '.'
			 * twice
			 */
			char[] buf = dca.toCharArray();
			int i = 0;
			for (; i < dca.length(); i++) {
				if (buf[i] == '.')
					break;
			}
			if (i == dca.length()) {
				if (dca.charAt(0) == '-') {
					if (index == 0)
						index++;
					if (index == 1)
						out.insert(index++, '0');
				} else if (index == 0) {
					out.insert(index++, '0');
				}
			} else {
				opChar = 0;
			}
		}
		if (opChar > 0 && dca.length() < tf.getMaxSize()) {
			out.insert(index++, opChar);
		}
		return index;
	}

	/**
	 * Returns the string that would be painted for the phone number constraints
	 *
	 * @param dca the text to paint
	 * @param index cursor index 
	 * @param opChar option char 
	 * @param out the string that will be sent to be drawn. 
	 * @return new cursor index
	 */
	private int getStringForPhoneNumber(DynamicCharacterArray dca, int index, char opChar, DynamicCharacterArray out) {
		// +3 is the most characters we will need to insert here
		out.setCapacity(dca.length() + 1 + 3);

		switch (dca.length()) {
		case 5:
		case 6:
		case 7:
			if (out.length() < tf.getMaxSize()) {
				out.insert(3, ' ');
				if (index > 3)
					index++;
			}
			break;
		case 11:
			if (out.length() + 2 < tf.getMaxSize()) {
				out.insert(1, ' ');
				if (index > 1)
					index++;
				out.insert(5, ' ');
				if (index > 5)
					index++;
				out.insert(9, ' ');
				if (index > 9)
					index++;
			}
			break;
		case 8:
		case 9:
		case 10:
		default:
			if (out.length() + 1 < tf.getMaxSize()) {
				out.insert(3, ' ');
				if (index > 3)
					index++;
				out.insert(7, ' ');
				if (index > 7)
					index++;
			}
			break;
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			break;
		}
		if (opChar > 0 && out.length() + 1 < tf.getMaxSize()) {
			out.insert(index++, opChar);
		}
		return index;
	}

	/**
	 * Returns the string that would be painted for the password modifier.
	 *
	 * @param dca the text to paint
	 * @param index cursor index 
	 * @param opChar option char 
	 * @param out the string that will be sent to be drawn. 
	 * @param constraints text input constraints
	 * @return new cursor index
	 */
	private int getStringForPassword(DynamicCharacterArray dca, int constraints, int index, char opChar,
			DynamicCharacterArray out) {
		//
		// Handle password: if the constraints are ANY or NUMERIC
		// we can just set all the characters to a *. otherwise,
		// we must only change those characters which are not
		// considered to be symbols
		//
		if ((constraints & TextField.CONSTRAINT_MASK) == TextField.ANY
				|| (constraints & TextField.CONSTRAINT_MASK) == TextField.NUMERIC
				|| (constraints & TextField.CONSTRAINT_MASK) == TextField.DECIMAL) {

			for (int i = 0; i < dca.length(); i++) {
				out.append('*');
			}
		} else {
			for (int i = 0; i < dca.length(); i++) {
				if (!inputSession.isSymbol(dca.charAt(i))) {
					// log("**"+dca.charAt(i)+" is symbol!");
					/* special request from customer */
					out.append('*');
				} else {
					// log("**"+dca.charAt(i)+" is NOT symbol!");
					out.append(dca.charAt(i));
				}
			}
		}
		if (opChar > 0 && dca.length() < tf.getMaxSize()) {
			out.insert(index++, opChar);
		}
		return index;
	}

	/**
	 * Paint the text, scrolling left or right when necessary.
	 * (A TextField may only be one line hight)
	 *
	 * @param g the Graphics to use to paint with. If g is null then
	 *        only the first four arguments are used and nothing is
	 *        painted. Use this to return just the displayed string
	 * @param dca the text to paint
	 * @param opChar option char 
	 * @param constraints text constraints
	 * @param font the font to use to paint the text
	 * @param fgColor foreground color
	 * @param w the available width for the text
	 * @param h the available height for the text
	 * @param offset the first line pixel offset
	 * @param options any of Text.[NORMAL | INVERT | HYPERLINK | TRUNCATE]
	 * @param cursor text cursor object to use to draw vertical bar
	 *
	 * @return the current xScrollOffset value which may be changed
	 *         to match the return value of <code>Text.paintLine()</code>
	 */
	public int paint(Graphics g, DynamicCharacterArray dca, char opChar, int constraints, Font font, int fgColor,
			int w, int h, int offset, int options, TextCursor cursor) {

		int newXOffset = 0;

		g.setClip(0, 0, w, h);

		cursor = new TextCursor(cursor);
		String str = getDisplayString(dca, opChar, constraints, cursor, true);

		newXOffset = Text.paintLine(g, str, font, fgColor, w, h, cursor, offset);

		// We'll double check our anchor point in case the Form
		// has scrolled and we need to update our InputModeLayer's
		// location on the screen   
		if (hasFocus) {
			moveInputModeIndicator();
		}

		showPTPopup((int) 0, cursor, w, h);
		return newXOffset;
	}

	// Implementation of Chameleon's TextInputComponent Interface
	/**
	 * Retrieve the initial input mode of this text component as
	 * defined by the LCDUI TextField API.
	 *
	 * @return the initial input mode for this text component or 'null'
	 *         if none was set(?)
	 */
	public String getInitialInputMode() {
		synchronized (Display.LCDUILock) {
			return this.initialInputMode;
		}
	}

	/**
	 * Retrieve the display for the text component
	 *
	 * @return the display used for the text component
	 */
	public Display getDisplay() {
		return getCurrentDisplay();
	}

	/**
	 * Retrieve the constraints of this text component as defined
	 * by the LCDUI TextField API.
	 *
	 * @return a bitmask which defines the constraints set on this
	 *         text component, or 0 if none were set(?)
	 */
	public int getConstraints() {
		synchronized (Display.LCDUILock) {
			return tf.constraints;
		}
	}

	/**
	 * Returns true if the keyCode is used as 'clear'
	 * @param keyCode key code
	 * @return true if keu code is Clear one, false otherwise
	 */
	public boolean isClearKey(int keyCode) {
		return EventConstants.SYSTEM_KEY_CLEAR == Toolkit.getToolkit().getEventMapper().getSystemKey(keyCode);

		// return EventConstants.SYSTEM_KEY_CLEAR ==
		//     KeyConverter.getSystemKey(keyCode);        
	}

	/**
	 * Commit the given input to this TextInputComponent's buffer.
	 * This call constitutes a change to the value of this TextInputComponent
	 * and should result in any listeners being notified.
	 * @param input text to commit 
	 */
	public void commit(String input) {
		if (input == null) {
			System.err.println("TextFieldLFImpl Warning: commited text is null!");
			return;
		}

		log("TF.commit:: " + input);

		synchronized (Display.LCDUILock) {
			try {
				cursor.visible = true;
				DynamicCharacterArray in = tf.buffer;

				TextCursor newCursor = new TextCursor(cursor);
				for (int i = 0; i < input.length(); i++) {
					TextCursor currentCursor = new TextCursor(newCursor);
					String str = getDisplayString(in, input.charAt(i), tf.constraints, currentCursor, true);
					in = new DynamicCharacterArray(str);
					newCursor = currentCursor;
				}

				if (bufferedTheSameAsDisplayed(tf.constraints)) {
					tf.setString(in.toString());
					cursor = newCursor;
				} else if (tf.buffer.length() < tf.getMaxSize()) {
					tf.insert(input, cursor.index);
				}
			} catch (Exception ignore) {
			}
			tf.notifyStateChanged();
		}
	}

	/**
	 * Check if the string in the text buffer is the same as the string that
	 * is actually displayed
	 *
	 * @param constraints text input constraints
	 *
	 * @return true if the the string is the same otherwise false
	 */
	private boolean bufferedTheSameAsDisplayed(int constraints) {
		return !((constraints & TextField.PASSWORD) == TextField.PASSWORD || (constraints & TextField.CONSTRAINT_MASK) == TextField.PHONENUMBER);
	}

	/**
	 * Clear the particular number of symbols 
	 *
	 * @param num number of symbols
	 */
	public void clear(int num) {
		if (cursor.index <= 0) {
			return;
		}

		if (num == 0) {
			System.err.println("TextFieldLFImpl Warning: asked to delete 0!");
			return;
		}
		if (cursor.index == 0) {
			System.err.println("TextFieldLFImpl Warning: asked to delete when cursor index is 0!");
			return;
		}
		synchronized (Display.LCDUILock) {
			try {
				tf.delete(cursor.index - num, num);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			tf.notifyStateChanged();
		}

	}

	/**
	 * This is a notification from the input session that the selected
	 * input mode has changed. If the TextInputComponent is interested,
	 * it can query the session for the new InputMode.
	 */
	public void notifyModeChanged() {
		removeInputCommands();

		inputModes = inputSession.getAvailableModes();
		InputMode im = inputSession.getCurrentInputMode();

		inputMenu = new SubMenuCommand(im.getCommandName(), Command.OK, 100);
		inputMenu.setListener(this);

		addInputCommands();

		inputModeIndicator.setDisplayMode(im.getName());
	}

	// End Chameleon's TextInputComponent Interface

	/** 
	 * Called to commit any pending character from the input handler
	 */
	public void lCommitPendingInteraction() {
		// IMPL NOTE: fix needed? inputHandler.endComposition(false);
	}

	/**
	 * Handle a key press
	 *
	 * @param keyCode the code for the key which was pressed
	 */
	void uCallKeyPressed(int keyCode) {
		boolean theSameKey = timers.contains(new TimerKey(keyCode));

		if (!theSameKey) {
			setTimerKey(keyCode);
		}

		synchronized (Display.LCDUILock) {
			// IMPL NOTE: add back in the phone dial support after defining
			// more system keys like 'send'

			int systemKeyCode = Toolkit.getToolkit().getEventMapper().getSystemKey(keyCode);
			
			if (systemKeyCode == EventConstants.SYSTEM_KEY_SEND) {
				if ((getConstraints() & TextField.CONSTRAINT_MASK) == TextField.PHONENUMBER) {
					PhoneDial.call(tf.getString());
				}
				return;
			}

			if (!editable) {
				// play sound
				AlertType.WARNING.playSound(getCurrentDisplay());
				return;
			}

			int key;
			log("TF.processKey keyCode = " + keyCode + " longPress = " + theSameKey);
			if ((key = inputSession.processKey(keyCode, theSameKey)) == InputMode.KEYCODE_NONE) {
				// This means the key wasn't handled by the InputMode
				log("[TF.uCallKeyPressed] returned KEYCODE_NONE");
				handleClearKey(keyCode, theSameKey);
			} else if (key != InputMode.KEYCODE_INVISIBLE) {
				log("[TF.uCallKeyPressed] returned key = " + key);

				cursor.visible = true;
				lRequestPaint();
			}
		} // synchronized
	}

	/**
	 * Handle Clear key
	 *
	 * @param keyCode the code for the key which was pressed
	 * @param longPress true if long key press happens otherwise false
	 */
	private void handleClearKey(int keyCode, boolean longPress) {
		if (isClearKey(keyCode)) {
			if (longPress) {
				if (tf.buffer.length() > 0) {
					tf.delete(0, tf.buffer.length());
					tf.notifyStateChanged();
				}
			} else {
				clear(cursor.index - gePrevCursorIndex(tf.constraints, tf.buffer, cursor.index));
			}
		}
	}

	/**
	 * Get previous cursor position 
	 *
	 * @param constraints text input constraints. The semantics of the 
	 * constraints value are defined in the TextField API.
	 * @param str text
	 * @param cursor current cursor index
	 * @return previious cursor index
	 */
	private int gePrevCursorIndex(int constraints, DynamicCharacterArray str, int cursor) {
		cursor--;
		if (((constraints & TextField.CONSTRAINT_MASK) == TextField.DECIMAL || (constraints & TextField.CONSTRAINT_MASK) == TextField.NUMERIC)
				&& cursor == 1 && str.length() == 2 && (str.charAt(0) == '-' || str.charAt(0) == '.')) {
			cursor--;
		}
		return cursor;
	}

	/**
	 * Handle a key release event 
	 *
	 * @param keyCode key that was released
	 *
	 * Obsoleted: Java PhonePad input is replaced by platform input methods.
	 *
	 */
	void uCallKeyReleased(int keyCode) {
		/*
		 * IMPL NOTE: abstract the press/release to constants so that
		 * a port can be configured as to which one to use via the
		 * constants definition file
		 */

		cancelTimerKey(keyCode);
	}

	/**
	 * Handle a key repeated event 
	 *
	 * @param keyCode key that was repeated
	 */
	void uCallKeyRepeated(int keyCode) {
		uCallKeyPressed(keyCode);
	}

	/** Timer to indicate long key press */
	class TimerKey extends TimerTask {
		/** handling key */
		private int key;

		/**
		 * Default constructor for TimerKey
		 * @param key key code
		 */
		TimerKey(int key) {
			this.key = key;
		}

		/**
		 * As soon as timer occures uCallKeyPressed has to be called
		 * and timer has to be stopped
		 */

		public final void run() {
			uCallKeyPressed(key);
			stop();
		}

		/**
		 * Start the timer for the pressed key.
		 * Add this timer to the active pool. 
		 */
		public void start() {
			timerService.schedule(this, 700);
			timers.addElement(this);
		}

		/**
		 * Stop the timer for the pressed key.
		 * Remove this timer from the active pool.
		 */
		public void stop() {
			cancel();
			timers.removeElement(this);
		}

		/**
		 * just one timer can be started for the particular key
		 * @param obj another TimerKey object
		 * @return true if this TimerKey object equals to another one
		 */
		public boolean equals(Object obj) {
			return ((TimerKey) obj).key == key;
		}
	}

	/** Pool of the active timers */
	protected Vector timers = new Vector();

	/** The Timer to service TimerTasks. */
	protected Timer timerService = new Timer();

	/**
	 * Set a new timer.
	 *
	 * @param keyCode the key the timer is started for   
	 */
	protected synchronized void setTimerKey(int keyCode) {
		log("[setTimerKey] for " + keyCode);
		TimerKey timer = new TimerKey(keyCode);
		if (!timers.contains(timer)) {
			try {
				timer.start();
			} catch (IllegalStateException e) {
				log("Exception caught in setTimer");
				cancelTimerKey(keyCode);
			}
		}
	}

	/**
	 * Cancel any running Timer.
	 * @param keyCode key the timer is canceled for
	 */
	protected synchronized void cancelTimerKey(int keyCode) {
		log("[cancelTimerKey] for " + keyCode);
		TimerKey timer = new TimerKey(keyCode);
		int idx = timers.indexOf(timer);
		if (idx != -1) {
			((TimerKey) timers.elementAt(idx)).stop();
		}
	}

	/**
	 * Emulate the key click
	 * @param keyCode key code 
	 *
	 */
	protected void keyClicked(int keyCode) {
		uCallKeyPressed(keyCode);
		uCallKeyReleased(keyCode);
	}

	/**
	 * Move the text cursor in the given direction
	 *
	 * @param dir direction to move
	 * @return true if the cursor was moved, false otherwise
	 */
	boolean moveCursor(int dir) {
		boolean keyUsed = false;
		int newIndex;

		switch (dir) {
		case Canvas.LEFT:
			if (editable) {
				keyClicked(dir);
				if (cursor.index > 0) {
					cursor.option = Text.PAINT_USE_CURSOR_INDEX;
					keyUsed = true;
					cursor.index--;
				}
			}
			break;

		case Canvas.RIGHT:
			if (editable) {
				keyClicked(dir);
				if (cursor.index < tf.buffer.length()) {
					cursor.option = Text.PAINT_USE_CURSOR_INDEX;
					keyUsed = true;
					cursor.index++;
				}
			}
			break;

		case Canvas.UP:
		case Canvas.DOWN:
		default:
			break;
		}

		return keyUsed;
	}

	/**
	 * Called by the system
	 *
	 * <p>The default implementation of the traverse() method always returns
	 * false.</p>
	 *
	 * @param dir the direction of traversal
	 * @param viewportWidth the width of the container's viewport
	 * @param viewportHeight the height of the container's viewport
	 * @param visRect_inout passes the visible rectangle into the method, and
	 * returns the unmodified traversal rectangle from this method
	 * @return true if internal traversal had occurred, false if traversal
	 * should proceed out
	 *
	 * @see #getInteractionModes
	 * @see #traverseOut
	 * @see #TRAVERSE_HORIZONTAL
	 * @see #TRAVERSE_VERTICAL
	 */
	boolean uCallTraverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
		boolean ret = super.uCallTraverse(dir, viewportWidth, viewportHeight, visRect_inout);

		// Show indicator layer
		if (showIMPopup) {
			if (inputModeIndicator.getDisplayMode() != null) {
				getCurrentDisplay().showPopup(inputModeIndicator);
				showIMPopup = false;
			}
		}

		return ret;
	}

	/**
	 * Called by the system
	 *
	 * <p>The default implementation of the traverse() method always returns
	 * false.</p>
	 *
	 * @param dir the direction of traversal
	 * @param viewportWidth the width of the container's viewport
	 * @param viewportHeight the height of the container's viewport
	 * @param visRect_inout passes the visible rectangle into the method, and
	 * returns the unmodified traversal rectangle from this method
	 * @return true if internal traversal had occurred, false if traversal
	 * should proceed out
	 *
	 * @see #getInteractionModes
	 * @see #traverseOut
	 * @see #TRAVERSE_HORIZONTAL
	 * @see #TRAVERSE_VERTICAL
	 */
	boolean lCallTraverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
		// By design, traverse should ONLY happen after the item is notified
		// to be shown, including the very first show a Form.
		// ASSERT (visible == true)
		super.lCallTraverse(dir, viewportWidth, viewportHeight, visRect_inout);

		boolean ret = false;
		Display currentDisplay = getCurrentDisplay();

		if (firstTimeInTraverse || dir == CustomItem.NONE) {

			if (firstTimeInTraverse) {
				if (editable) {
					InputMode im = inputSession.getCurrentInputMode();
					if (im != null && im.hasDisplayable()) {
						enableTF();
					} else {
						// IMPL NOTE: try-catch has to be removed when the form
						// is fixed. Form resets the focus to the 1st item after
						// Symbol Table goes away
						try {
							enableInput();
						} catch (Exception ignore) {
						}
					}
					if (interruptedIM != null) {
						inputSession.setCurrentInputMode(interruptedIM);
						interruptedIM = null;
					}

				} else {
					cursor.option = Text.PAINT_USE_CURSOR_INDEX;
					cursor.visible = false;
					startScroll();
				}
				// Show Input Indicator regardless of editability
				// so we don't have to turn the layer on/off
				// when setConstraints() is called later, which
				// may cause deadlock between locks 'layers' and
				// LCDUILock.
				showIMPopup = true;
				firstTimeInTraverse = false;
			}

			lRequestPaint();
			// if (currentDisplay != null) {
			//    currentDisplay.serviceRepaints(tf.owner.getLF());
			// }
			ret = true;

		} else {

			if (moveCursor(dir)) {
				lRequestPaint();
				// if (currentDisplay != null) {
				//    currentDisplay.serviceRepaints(tf.owner.getLF());
				// }
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * Called by the system to indicate traversal has left this Item
	 *
	 * @see #getInteractionModes
	 * @see #traverse
	 * @see #TRAVERSE_HORIZONTAL
	 * @see #TRAVERSE_VERTICAL
	 */
	void uCallTraverseOut() {
		super.uCallTraverseOut();

		// Dismiss input mode indicator layer outside LCDUILock
		// to avoid deadlocking with Chameleon internal lock 'layers'.
		Display currentDisplay = getCurrentDisplay();
		if (currentDisplay != null) {
			currentDisplay.hidePopup(inputModeIndicator);
			hidePTILayer();
		}
	}

	/**
	 * Called by the system to indicate traversal has left this Item
	 *
	 * @see #getInteractionModes
	 * @see #traverse
	 * @see #TRAVERSE_HORIZONTAL
	 * @see #TRAVERSE_VERTICAL
	 */
	void lCallTraverseOut() {
		super.lCallTraverseOut();

		firstTimeInTraverse = true;

		if (editable) {
			InputMode im = inputSession.getCurrentInputMode();
			if (im != null && im.hasDisplayable()) {
				disableTF();
				((ScreenLFImpl) tf.owner.getLF()).resetToTop = false;
			} else {
				disableInput();

				if (im != null) {
					interruptedIM = im;
				} else {
					cursor.index = tf.buffer.length();
				}
			}
			// Leave the hide of indicator layer to uCallTraverseOut
			// to avoid deadlocking
		} else {
			cursor.option = Text.PAINT_USE_CURSOR_INDEX;
			cursor.visible = false;
			cursor.index = tf.buffer.length();
		}
		xScrollOffset = 0;

		if (textScrollPainter != null) {
			stopScroll();
		}

		lRequestPaint();
	}

	/**
	 * Turns the border on or off.  hasBorder=false used to indicate
	 * a textBox, but now that case in handled in the TextBoxLFImpl class
	 *
	 * @param state true to turn the border on, false to turn it off
	 */
	void setBorder(boolean state) {
		// API requires this method to exist, so no-op
	}

	/**
	 * Move input mode indicator
	 */
	void moveInputModeIndicator() {

		int[] anchor = getInputModeAnchor();
		if (inputModeAnchor[0] != anchor[0] || inputModeAnchor[1] != anchor[1] || inputModeAnchor[2] != anchor[2]
				|| inputModeAnchor[3] != anchor[3]) {
			inputModeAnchor = anchor;
			inputModeIndicator
					.setAnchor(inputModeAnchor[0], inputModeAnchor[1], inputModeAnchor[2], inputModeAnchor[3]);
		}
	}

	/**
	 * This is a utility function to calculate the anchor point
	 * for the InputModeIndicator layer. This takes into account
	 * the item's location as well as the containing form's scroll
	 * location. The array is a 4 element array corresponding to the
	 * the parameters to the InputModeLayer's setAnchor() method.
	 * @return input mode anchor (x, y, w, h)
	 */
	protected int[] getInputModeAnchor() {
		int[] anchor = new int[] { 0, 0, 0, 0 };
		try {
			ScreenLFImpl sLF = (ScreenLFImpl) tf.owner.getLF();
			int x = getInnerBounds(X) - sLF.viewable[X] + contentBounds[X];
			int y = getInnerBounds(Y) - sLF.viewable[Y] + contentBounds[Y];

			// anchor x-coordinate relative to InputModeLayer
			anchor[0] = x + contentBounds[WIDTH] + getCurrentDisplay().getWindow().getBodyAnchorX();
			// anchor y-coordinate relative to InputModeLayer
			anchor[1] = y + getCurrentDisplay().getWindow().getBodyAnchorY();
			// item height
			anchor[2] = contentBounds[HEIGHT];
			// space below the bottom of the item on the screen
			anchor[3] = (sLF.viewport[HEIGHT] + sLF.viewable[Y]) - (bounds[Y] + bounds[HEIGHT]);

		} catch (Throwable t) {
		}
		return anchor;
	}

	/**
	 * Initialize or reset variables used to auto-scroll 
	 * uneditable text across this TextField
	 */
	private void resetUneditable() {
		String text = getDisplayString(tf.buffer, inputSession.getPendingChar(), tf.constraints, cursor, true);

		textWidth = ScreenSkin.FONT_INPUT_TEXT.stringWidth(text);

		xScrollOffset = 0;
	}

	/**
	 * Start the scrolling of the text in textField
	 */
	public void startScroll() {
		if (editable || !hasFocus || textWidth <= scrollWidth) {
			return;
		}
		stopScroll();
		textScrollPainter = new TextScrollPainter();
		textScrollTimer.schedule(textScrollPainter, 0, TextFieldSkin.SCROLL_RATE);
	}

	/**
	 * Stop the scrolling of the text in TextField
	 */
	public void stopScroll() {
		if (textScrollPainter == null) {
			return;
		}
		textScrollPainter.cancel();
		textScrollPainter = null;
	}

	/**
	 * Called repeatedly to animate a side-scroll effect for
	 * uneditable text within a TextField
	 */
	public void repaintScrollText() {
		if (-xScrollOffset < (textWidth - scrollWidth)) {
			xScrollOffset -= TextFieldSkin.SCROLL_SPEED;
			lRequestPaint();
		} else {
			// already scrolled to the end of text
			stopScroll();
		}
	}

	/**
	 * Enable text field  
	 */
	private void enableTF() {
		cursor.option = Text.PAINT_USE_CURSOR_INDEX;
		cursor.visible = true;
	}

	/**
	 * Enable text field input
	 */
	private void enableInput() {
		enableTF();

		// ASSERT (editable && hasFocus)
		inputSession.beginSession(this);
	}

	/**
	 * Disable text field
	 */
	private void disableTF() {
		cursor.option = Text.PAINT_USE_CURSOR_INDEX;
		cursor.visible = false;
	}

	/**
	 * Disable text field input
	 */
	private void disableInput() {
		disableTF();
		removeInputCommands();
		inputSession.endSession();
	}

	/**
	 * Helper class used to repaint scrolling text 
	 * if needed.
	 */
	private class TextScrollPainter extends TimerTask {
		/**
		 * Repaint the TextField 
		 */
		public final void run() {
			if (!visible) {
				stopScroll();
			} else {
				repaintScrollText();
			}
		}
	}

	/**
	 * Add input modes specific commands 
	 */
	private void addInputCommands() {
		inputMenu.removeAll();
		if (inputModes != null) {
			Command[] inputCommands = new Command[inputModes.length];
			for (int i = 0; i < inputModes.length; i++) {
				inputCommands[i] = new Command(inputModes[i].getCommandName(), Command.OK, i);
			}
			inputMenu.addSubCommands(inputCommands);
			// NOTE : adding the command here relies on the
			// implementation of Item.addCommand() to not add
			// the same command twice, as stated in the MIDP spec
			tf.addCommand(inputMenu);
		}
	}

	/**
	 * Remove input modes specific commands 
	 */
	private void removeInputCommands() {
		tf.removeCommand(inputMenu);
	}

	/**
	 * Print debug message
	 * @param s debug message
	 */
	void log(String s) {
		//System.out.println("[DEBUG] TextFieldImpl LOG: " + s);
	}

	/**
	 * Show predictive text popup dialog 
	 * @param keyCode key code
	 * @param cursor text cursor
	 * @param width width
	 * @param height height
	 */
	protected void showPTPopup(int keyCode, TextCursor cursor, int width, int height) {
		log("[ showPTPopup] " + keyCode + "," + cursor + "," + width + "," + height);
		if (pt_matches.length > 1) { // show layer 
			log("[showPTPopup]    pt_matches.length =" + pt_matches.length);
			pt_popup.setList(pt_matches);
			showPTILayer();
		} else { // hide layer
			log("[hidePTPopup]    pt_matches=0");
			hidePTILayer();
		}
	}

	/**
	 * Show predictive text popup dialog 
	 */
	protected void showPTILayer() {
		Display d = getCurrentDisplay();
		if (!pt_popupOpen && d != null) {
			log("[showPTPopup] showing");
			d.showPopup(pt_popup);
			pt_popupOpen = true;
			lRequestInvalidate(true, true);
		}
	}

	/**
	 * Hide predictive text popup dialog 
	 */
	protected void hidePTILayer() {
		Display d = getCurrentDisplay();
		if (pt_popupOpen && d != null) {
			log("[showPTPopup] hiding");
			d.hidePopup(pt_popup);
			pt_popupOpen = false;
			lRequestInvalidate(true, true);
		}
	}

	/**
	 * Check if PTI popup is visible
	 * @return true if pti layer is visible, false - otherwise
	 */
	public boolean hasPTI() {
		return pt_popupOpen;
	}

} // TextFieldLFImpl

