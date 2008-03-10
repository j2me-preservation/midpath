/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
 * 
 * com.sixlegs.image.png - Java package to read and display PNG images
 * Copyright (C) 1998-2004 Chris Nokleberg
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
package org.thenesis.midpath.image.png;

import java.io.IOException;

final class Chunk_hIST extends Chunk {
	Chunk_hIST() {
		super(hIST);
	}

	protected boolean multipleOK() {
		return false;
	}

	protected boolean beforeIDAT() {
		return true;
	}

	protected void readData() throws IOException {
		if (img.data.palette == null)
			throw new PngException("hIST chunk must follow PLTE chunk");
		int num = img.data.palette.r.length;
		if (length != num * 2)
			badLength(num * 2);
		int[] values = new int[num];
		for (int i = 0; i < num; i++)
			values[i] = in_data.readUnsignedShort();
		img.data.properties.put("histogram", values);
	}
	
	Chunk copy() {
		return new Chunk_hIST();
	}
}