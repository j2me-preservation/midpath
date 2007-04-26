/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
 * 
 * Odonata - Copyright (C) 2002-2006 Stephane Meslin-Weber <steph@tangency.co.uk>
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
package org.thenesis.midpath.font.bdf;

import java.util.Vector;

/**
 * Parses a font in BDF format.
 * <p>The BDF Font specification from Adobe can be fount at
 * <a href="http://partners.adobe.com/public/developer/en/font/5005.BDF_Spec.pdf">Adobe's partner site</a></p>
 * <p>Bit-depths higher than 1 are curtesy of Fontforge's BDF 2.3 spec at
 * <a href="fontforge.sourceforge.net/BDFgrey.html">Fontforge's website</a>
 */
public class BDFParser implements BDFParserConstants {

	public static final int PLAIN = 0;
	public static final int BOLD = 1;
	public static final int ITALIC = 2;

	public BDFFontContainer createFont() throws Exception {
		return loadFont();
	}

	/**
	 * Value object, removes need for AWT's Dimension.
	 */
	public static class Dimension {
		public int width, height;

		public void setSize(int w, int h) {
			width = w;
			height = h;
		}
	}

	/**
	 * Value object, removes need for AWT's Rectangle.
	 */
	public static class Rectangle {
		public int x, y, width, height;

		public Rectangle() {
		}

		public Rectangle(Rectangle rect) {
			setBounds(rect.x, rect.y, rect.width, rect.height);
		}

		public void setBounds(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}
	}

	/**
	 * Value object, not really needed externally.
	 */
	public static class Version {
		private int major;
		private int minor;

		Version() {
		}

		void setVersion(int major, int minor) {
			this.major = major;
			this.minor = minor;
		}

		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

		public String toString() {
			return major + "." + minor;
		}
	}

	final public BDFFontContainer loadFont() throws ParseException {
		BDFFontContainer font = null;
		int[] version = null;
		String[] comments = null;
		String[] fontName = null;
		int[] size = null;
		int[] bBox = null;
		int contentVersion = 0;
		int metrics = 0;
		String[] properties = null;
		BDFGlyph[] chars = null;
		int style = PLAIN;
		jj_consume_token(STARTFONT);
		version = getVersion();
		label_1: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case FONTNAME:
				fontName = getFontName();
				break;
			case SIZE:
				size = getSize();
				break;
			case FONTBOUNDINGBOX:
				bBox = setBoundingBox();
				break;
			case METRICSSET:
				metrics = setMetricsSet();
				break;
			case COMMENT:
				comments = getComments();
				break;
			case STARTPROPERTIES:
				properties = properties();
				break;
			case CONTENTVERSION:
				contentVersion = setContentVersion();
				break;
			default:
				jj_la1[0] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case CONTENTVERSION:
			case FONTBOUNDINGBOX:
			case METRICSSET:
			case SIZE:
			case STARTPROPERTIES:
			case COMMENT:
			case FONTNAME:
				;
				break;
			default:
				jj_la1[1] = jj_gen;
				break label_1;
			}
		}
		chars = getChars();
		jj_consume_token(ENDFONT);
		jj_consume_token(0);
		if ("i".equalsIgnoreCase(fontName[BDFFontContainer.SLANT])
				|| "o".equalsIgnoreCase(fontName[BDFFontContainer.SLANT])) {
			style = ITALIC;
			System.err.println(fontName[0] + " " + fontName[BDFFontContainer.SLANT]);
		}
		if ("bold".equalsIgnoreCase(fontName[BDFFontContainer.WEIGHT]))
			style += BOLD;

		font = new BDFFontContainer(fontName, style, size[0]);
		font.setBoundingBox(bBox[0], bBox[1], bBox[2], bBox[3]);
		font.setResolution(size[1], size[2]);
		font.setComments(comments);
		font.setProperties(properties);

		if (size != null && size.length == 4)
			font.setDepth(size[3]);

		font.setGlyphs(chars);
		{
			if (true)
				return font;
		}
		throw new Error("Missing return statement in function");
	}

	// Parse the BDF version, currently 2.[123] are valid.
	final public int[] getVersion() throws ParseException {
		Token major, minor;
		major = jj_consume_token(INT);
		jj_consume_token(1);
		minor = jj_consume_token(INT);
		{
			if (true)
				return new int[] { Integer.parseInt(major.image), Integer.parseInt(minor.image) };
		}
		throw new Error("Missing return statement in function");
	}

	// Sets the metric set
	final public int setMetricsSet() throws ParseException {
		jj_consume_token(METRICSSET);
		jj_consume_token(INT);
		{
			if (true)
				return Integer.parseInt(token.image);
		}
		throw new Error("Missing return statement in function");
	}

	// Collates all raw glyphs
	final public BDFGlyph[] getChars() throws ParseException {
		BDFGlyph[] glyphs = null;
		int count = 0;
		jj_consume_token(CHARS);
		jj_consume_token(INT);
		glyphs = new BDFGlyph[Integer.parseInt(token.image)];
		label_2: while (true) {
			glyphs[count++] = getChar();
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case STARTCHAR:
				;
				break;
			default:
				jj_la1[2] = jj_gen;
				break label_2;
			}
		}
		{
			if (true)
				return glyphs;
		}
		throw new Error("Missing return statement in function");
	}

	// Gets font bounding box
	final public int[] setBoundingBox() throws ParseException {
		int x, y, width, height;
		jj_consume_token(FONTBOUNDINGBOX);
		jj_consume_token(INT);
		width = Integer.parseInt(token.image);
		jj_consume_token(INT);
		height = Integer.parseInt(token.image);
		jj_consume_token(INT);
		x = Integer.parseInt(token.image);
		jj_consume_token(INT);
		y = Integer.parseInt(token.image);
		{
			if (true)
				return new int[] { x, y, width, height };
		}
		throw new Error("Missing return statement in function");
	}

	// Gets this font's comments
	final public String[] getComments() throws ParseException {
		Vector list = new Vector();
		label_3: while (true) {
			jj_consume_token(COMMENT);
			jj_consume_token(CONTENTSTRING);
			list.addElement(token.image);
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case COMMENT:
				;
				break;
			default:
				jj_la1[3] = jj_gen;
				break label_3;
			}
		}
		String[] comments = new String[list.size()];
		{
			if (true) {
				list.copyInto(comments);
				return comments;
			}
			
//			if (true)
//				return (String[]) list.toArray(comments);
		}
		throw new Error("Missing return statement in function");
	}

	// Gets the font size, resolutions and bit depth
	final public int[] getSize() throws ParseException {
		int pointSize, xres, yres, depth = 1;
		jj_consume_token(SIZE);
		jj_consume_token(INT);
		pointSize = Integer.parseInt(token.image);
		jj_consume_token(INT);
		xres = Integer.parseInt(token.image);
		jj_consume_token(INT);
		yres = Integer.parseInt(token.image);
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case INT:
			jj_consume_token(INT);
			depth = Integer.parseInt(token.image);
			break;
		default:
			jj_la1[4] = jj_gen;
			;
		}
		{
			if (true)
				return new int[] { pointSize, xres, yres, depth };
		}
		throw new Error("Missing return statement in function");
	}

	// Gets the content version
	final public int setContentVersion() throws ParseException {
		jj_consume_token(CONTENTVERSION);
		jj_consume_token(INT);
		{
			if (true)
				return Integer.parseInt(token.image);
		}
		throw new Error("Missing return statement in function");
	}

	// Gets the font name... currently expects a PostScript name but will survive if it isn't in that format
	final public String[] getFontName() throws ParseException {
		Token string;
		String[] template = new String[14];
		jj_consume_token(FONTNAME);
		string = jj_consume_token(FONTFAMILYSTRING);
	
		//java.util.Arrays.fill(template, "");
		//String[] split = string.image.trim().split("-");
		
		for (int i = 0; i < template.length; i++) {
			template[i] = "";
		}
		//System.out.println("Font name:" + string.image.trim());
		String[] split = split(string.image.trim(), "-");
		
		
		System.arraycopy(split, 1, template, 0, split.length - 2);
		{
			if (true)
				return template;
		}
		throw new Error("Missing return statement in function");
	}
	
	private String[] split(String s, String separator) {
		
		Vector v = new Vector();
		
		int index = 0;
		int lastIndex = 0;
		if (s.startsWith(separator)) {
			lastIndex = separator.length();
		}
		
		while((index = s.indexOf(separator, lastIndex)) != -1) {
			String subString = s.substring(lastIndex, index);
			if (subString.length() > 0) {
				v.addElement(subString);
				//System.out.println(subString + ": " + subString.length()); 
			}
			lastIndex = index + 1;
		}
		
		String[] strings = new String[v.size()];
		v.copyInto(strings);
		
		return strings;
	}

	// Gets the font properties
	final public String[] properties() throws ParseException {
		String[] properties;
		int count = 0;
		jj_consume_token(STARTPROPERTIES);
		jj_consume_token(PROPERTYCOUNT);
		properties = new String[Integer.parseInt(token.image)];
		label_4: while (true) {
			jj_consume_token(PROPERTYTEXT);
			properties[count++] = token.image;
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case PROPERTYTEXT:
				;
				break;
			default:
				jj_la1[5] = jj_gen;
				break label_4;
			}
		}
		jj_consume_token(ENDPROPERTIES);
		{
			if (true)
				return properties;
		}
		throw new Error("Missing return statement in function");
	}

	// Gets a single character glyph
	final public BDFGlyph getChar() throws ParseException {
		BDFGlyph dat;
		Token encoding, swx0, swy0, dwx0, dwy0, swx1, swy1, dwx1, dwy1;
		Token x, y, width, height;
		StringBuffer buf = new StringBuffer();
		jj_consume_token(STARTCHAR);
		jj_consume_token(CHARTEXT);
		dat = new BDFGlyph(token.image);
		jj_consume_token(ENCODING);
		encoding = jj_consume_token(INT);
		jj_consume_token(SWIDTH);
		swx0 = jj_consume_token(INT);
		swy0 = jj_consume_token(INT);
		jj_consume_token(DWIDTH);
		dwx0 = jj_consume_token(INT);
		dwy0 = jj_consume_token(INT);
		jj_consume_token(BBX);
		// Again, width, height, x, y
		width = jj_consume_token(INT);
		height = jj_consume_token(INT);
		x = jj_consume_token(INT);
		y = jj_consume_token(INT);
		jj_consume_token(BITMAP);
		label_5: while (true) {
			jj_consume_token(HEX);
			buf.append(token.image);
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case HEX:
				;
				break;
			default:
				jj_la1[6] = jj_gen;
				break label_5;
			}
		}
		jj_consume_token(ENDCHAR);
		dat.setSWidth(Integer.parseInt(swx0.image), Integer.parseInt(swy0.image));
		dat.setDWidth(Integer.parseInt(dwx0.image), Integer.parseInt(dwy0.image));
		dat.setBBX(Integer.parseInt(x.image), Integer.parseInt(y.image), Integer.parseInt(width.image), Integer
				.parseInt(height.image));
		dat.setRawData(buf);
		{
			if (true)
				return dat;
		}
		throw new Error("Missing return statement in function");
	}

	public BDFParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	public Token token, jj_nt;
	private int jj_ntk;
	private int jj_gen;
	final private int[] jj_la1 = new int[7];
	static private int[] jj_la1_0;
	static private int[] jj_la1_1;
	static {
		jj_la1_0();
		jj_la1_1();
	}

	private static void jj_la1_0() {
		jj_la1_0 = new int[] { 0xa08003c0, 0xa08003c0, 0x800, 0x20000000, 0x8, 0x8000000, 0x100000, };
	}

	private static void jj_la1_1() {
		jj_la1_1 = new int[] { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, };
	}

	public BDFParser(java.io.InputStream stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new BDFParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(java.io.InputStream stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	public BDFParser(java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new BDFParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	public BDFParser(BDFParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	public void ReInit(BDFParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 7; i++)
			jj_la1[i] = -1;
	}

	final private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			jj_gen++;
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	final public Token getNextToken() {
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	final public Token getToken(int index) {
		Token t = token;
		for (int i = 0; i < index; i++) {
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	final private int jj_ntk() {
		if ((jj_nt = token.next) == null)
			return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private java.util.Vector jj_expentries = new java.util.Vector();
	private int[] jj_expentry;
	private int jj_kind = -1;

	public ParseException generateParseException() {
		jj_expentries.removeAllElements();
		boolean[] la1tokens = new boolean[33];
		for (int i = 0; i < 33; i++) {
			la1tokens[i] = false;
		}
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 7; i++) {
			if (jj_la1[i] == jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
					if ((jj_la1_1[i] & (1 << j)) != 0) {
						la1tokens[32 + j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 33; i++) {
			if (la1tokens[i]) {
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.addElement(jj_expentry);
			}
		}
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0; i < jj_expentries.size(); i++) {
			exptokseq[i] = (int[]) jj_expentries.elementAt(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	final public void enable_tracing() {
	}

	final public void disable_tracing() {
	}

}