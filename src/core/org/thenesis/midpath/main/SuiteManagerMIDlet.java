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
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.thenesis.midpath.main;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.midlet.MIDlet;

import com.sun.midp.midletsuite.MIDletInfo;


public class SuiteManagerMIDlet extends MIDlet implements CommandListener, ItemCommandListener {

	private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
	private static final Command CMD_START = new Command("Start", Command.ITEM, 1);
	private static final Command CMD_REMOVE = new Command("Remove", Command.ITEM, 2);
	private static final Command CMD_INSTALL = new Command("Install", Command.ITEM, 1);
	
	private Display display;
	private Form mainForm;


	/**
	 * Signals the MIDlet to start and enter the Active state.
	 */
	protected void startApp() {
		display = Display.getDisplay(this);

		mainForm = new Form("MIDlet Manager");

		ChoiceGroup installedGroup = new ChoiceGroup("Installed", Choice.EXCLUSIVE);
		installedGroup.append("Suite 1", null);
		installedGroup.append("Suite 2", null);
		installedGroup.addCommand(CMD_REMOVE);
		installedGroup.setDefaultCommand(CMD_START);
		installedGroup.setItemCommandListener(this);
		mainForm.append(installedGroup);

		//put some space between the items to segregate
		Spacer spacer = new Spacer(5, 5);
		mainForm.append(spacer);

		ChoiceGroup notInstalledGroup = new ChoiceGroup("Not Installed", Choice.EXCLUSIVE);
		notInstalledGroup.append("Suite 3", null);
		notInstalledGroup.append("Suite 4", null);
		notInstalledGroup.addCommand(CMD_REMOVE);
		notInstalledGroup.setDefaultCommand(CMD_INSTALL);
		notInstalledGroup.setItemCommandListener(this);
		mainForm.append(notInstalledGroup);

		mainForm.addCommand(CMD_EXIT);
		mainForm.setCommandListener(this);
		display.setCurrent(mainForm);
	}

	public void commandAction(Command c, Item item) {

		System.out.println("commandAction " + c.getLabel());

		if (c == CMD_INSTALL) {
			String text = "Installing MIDlet Suite...";
			Alert a = new Alert("Action", text, null, AlertType.INFO);
			display.setCurrent(a);
		} else if (c == CMD_REMOVE) {
			String text = "Removing MIDlet Suite...";
			Alert a = new Alert("Action", text, null, AlertType.INFO);
			display.setCurrent(a);
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

	/**
	 * Signals the MIDlet to terminate and enter the Destroyed state.
	 */
	protected void destroyApp(boolean unconditional) {
	}

	/**
	 * Signals the MIDlet to stop and enter the Paused state.
	 */
	protected void pauseApp() {
	}
}
