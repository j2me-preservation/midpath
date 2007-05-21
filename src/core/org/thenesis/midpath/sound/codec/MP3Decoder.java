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

import javazoom.jlme.decoder.BitStream;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;

import org.thenesis.midpath.sound.AudioFormat;

public class MP3Decoder implements AudioDecoder {

	private Decoder decoder;
	private BitStream bitstream;
	private AudioFormat format;

	public MP3Decoder()  {
	}
	
	public void initialize(InputStream stream) throws IOException {
		bitstream = new BitStream(stream);
		// Read first frame and get audio format
		Header header = bitstream.readFrame();
		decoder = new Decoder(header, bitstream);
		SampleBuffer output = (SampleBuffer) decoder.decodeFrame();
		format = new AudioFormat(decoder.getOutputFrequency(), AudioFormat.BITS_16, decoder.getOutputChannels(), true, false);
		bitstream.closeFrame();
	}


	public int decodeStep(DecoderCallback callback) throws IOException {
		Header header = bitstream.readFrame();
		SampleBuffer output = (SampleBuffer) decoder.decodeFrame();
		int length = output.size();
		if (length == 0)
			return -1;
		callback.write(output.getBuffer(), 0, length);
		bitstream.closeFrame();
		return length;
	}

	public AudioFormat getOutputAudioFormat() {
		return format;
	}

	
}
