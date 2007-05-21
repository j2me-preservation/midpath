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
package org.thenesis.midpath.sound.codec;

import java.io.IOException;
import java.io.InputStream;

import org.thenesis.midpath.sound.AudioFormat;

public class WaveDecoder implements AudioDecoder {
	
	public static final int BUFFER_SIZE_DEFAULT =  4 * 1024;
	
	private WaveFile waveFile;
	private InputStream is; 
	private AudioFormat waveFormat;
	private int bufferSize;
	private byte[] buffer;
	int bufferFrames;
	int bytesWritten;
	
	public WaveDecoder() {
	}
	
	public void initialize(InputStream is, int bufferSize) throws IOException {
		this.is = is;
		this.bufferSize = bufferSize;
		
		waveFile = new WaveFile(is);
		int bytesPerFrame = (waveFile.getBitsPerSample() * waveFile.getChannels()) / 8;
		int bytesPerSample = waveFile.getBitsPerSample() / 8;
		boolean signed = bytesPerSample > 1 ? true : false;
		waveFormat = new AudioFormat(waveFile.getRate(), bytesPerSample, waveFile.getChannels(), signed, false);
		buffer = new byte[bufferSize];
		bufferFrames = bufferSize / bytesPerFrame;
		bytesWritten = 0;
	}
	
	public void initialize(InputStream is) throws IOException {
		initialize(is, BUFFER_SIZE_DEFAULT);
	}
	
	public int decodeStep(DecoderCallback os) throws IOException {
		int framesRead = waveFile.readFrame(buffer, bufferFrames);
		
		if (framesRead <= 0) {
			return framesRead;
		}
		
		int samplesRead = framesRead * waveFormat.getBytesPerFrame();
		os.write(buffer, 0, samplesRead);
		bytesWritten += samplesRead;
		return samplesRead; 
	}
	
	public AudioFormat getOutputAudioFormat() {
		return waveFormat;
	}

}
