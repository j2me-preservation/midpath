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
package org.thenesis.midpath.sound;

import java.io.IOException;

public class NullSoundBackend implements SoundBackend {
	
	//private static final int DEFAULT_BUFFER_SIZE = 8192;
	private Mixer mixer;
	private AudioFormat audioFormat;
	private int bufferSize;
	private boolean isOpen = false;

	public int available() {
		return bufferSize;
	}

	public void close() {
		// Do nothing
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public Mixer getMixer() {
		return mixer;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void open() throws IOException {
		if (!isOpen) {
			audioFormat = SoundToolkit.getAudioFormat();
			bufferSize = SoundToolkit.getBufferSize();
			mixer = new NullMixer();
			isOpen = true;
		}
	}

	public int write(byte[] buf, int offset, int len) {
		return len;
	}
	
	class NullMixer extends Mixer {

		public Line createLine(AudioFormat format) {
			return new NullLine(format);
		}
		
	}
	
	class NullLine implements Line {
		
		private AudioFormat lineAudioFormat;
		public int state = STOPPED;
		
		public NullLine(AudioFormat format) {
			lineAudioFormat = format;
		}

		public int available() {
			return bufferSize;
		}
		
		public void start() {
			state = STARTED;
		}

		public void stop() {
			state = STOPPED;
		}

		public void close() {
			state = CLOSED;
		}

		public void drain() {
			// Do nothing
		}

		public AudioFormat getFormat() {
			return lineAudioFormat;
		}

		public boolean isRunning() {
			return (state == STARTED) ? true : false;
		}


		public int write(byte[] b, int offset, int length) {
			return length;
		}
		
	}
	


}


