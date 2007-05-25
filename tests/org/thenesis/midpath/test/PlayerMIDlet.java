/*
 * MIDPath - Copyright (C) 2006 Guillaume Legris, Mathieu Legris
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
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */
package org.thenesis.midpath.test;

import java.io.InputStream;

import javax.microedition.lcdui.Display;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class PlayerMIDlet extends MIDlet {

	private Display display;

	//@Override
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	//@Override
	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	//@Override
	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		
		try {
			
			//InputStream is = getClass().getResourceAsStream("tr51-glegris.mp3");
			InputStream is = getClass().getResourceAsStream("tr51-glegris.ogg");
			
			//Player player = Manager.createPlayer(is, "audio/mp3");
			Player player = Manager.createPlayer(is, "audio/ogg");
			player.addPlayerListener(new PlayerListener() {
				public void playerUpdate(Player player,  String event, Object eventData) {
					System.out.println("MP3 player event: " + event); 
				}
			});
			player.start();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
