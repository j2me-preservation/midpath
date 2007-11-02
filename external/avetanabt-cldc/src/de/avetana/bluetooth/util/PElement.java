package de.avetana.bluetooth.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * <b>COPYRIGHT:</b><br> (c) Copyright 2004 Avetana GmbH ALL RIGHTS RESERVED. <br><br>
 *
 * This file is part of the Avetana bluetooth API for Linux.<br><br>
 *
 * The Avetana bluetooth API for Linux is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version. <br><br>
 *
 * The Avetana bluetooth API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br><br>
 *
 * The development of the Avetana bluetooth API is based on the work of
 * Christian Lorenz (see the Javabluetooth Stack at http://www.javabluetooth.org) for some classes,
 * on the work of the jbluez team (see http://jbluez.sourceforge.net/) and
 * on the work of the bluez team (see the BlueZ linux Stack at http://www.bluez.org) for the C code.
 * Classes, part of classes, C functions or part of C functions programmed by these teams and/or persons
 * are explicitly mentioned.<br><br><br><br>
 *
 *
 * <b>Description: </b><br>
 * Das PElement ist ein nicht-ganz-DOM-Element, in welchem die Patienten-Informationen gespeichert werden.
 * Es dient als Ersatz f"ur das "uberladene DOM-Element mit grundlegenden Navigations-Funtkionen.
 * Die Kinder eines
 */

public class PElement {

	private int id = 0;
	protected PElement parent;
	protected HashMap children;
	protected EVector fastChildren; //Enth"alt auch alle Kinder ungeordnet
	protected Hashtable attributes;
	private String content;
	protected String name;
	private String owner = null;

	public String dummyContent; //Hier wird der Inhalt nur beim lesen gepuffert !

	public PElement(String name) {
		this(name, null);
	}

	public PElement() {
		this("Element", null);
	}

	public PElement(String name, String content) {
		this.name = name;
		this.content = content;
		this.id = 0;
	}

	public String getName() {
		return name;
	}

	public PElement getParent() {
		return parent;
	}

	public void setContent(String content) {
		while (content != null && content.length() >= 1 && (content.charAt(0) == ' ' || content.charAt(0) == '\n'))
			content = content.substring(1);
		while (content != null && content.length() >= 1
				&& (content.charAt(content.length() - 1) == ' ' || content.charAt(content.length() - 1) == '\n'))
			content = content.substring(0, content.length() - 1);
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAttribute(String name, String content) {
		if (content == null || content.trim().equals("")) {
			if (attributes == null)
				return;
			if (attributes.get(name) != null)
				attributes.remove(name);
			return;
		}
		if (attributes == null)
			attributes = new Hashtable();
		attributes.put(name, content);
	}

	public String getAttribute(String name) {
		if (attributes == null)
			return null;
		String v = (String) attributes.get(name);
		return v;
	}

	public void removeAttribute(String name) {
		if (attributes != null) {
			attributes.remove(name);
		}
	}

	/**
	 * F"ugt ein Kind an das Element an.
	 * Wenn schon ein Kind mit dem Namen existiert, werden ids automatisch verteilt.
	 *
	 * @param c Kind Element
	 */

	public PElement addChild(PElement c) {
		if (children == null)
			children = new HashMap();
		if (fastChildren == null)
			fastChildren = new EVector();
		fastChildren.add(c);

		Object o = children.get(c.getName());

		if (o == null)
			children.put(c.getName(), c);
		else if (o instanceof PElement) {
			EVector v = new EVector();
			v.add((PElement) o);
			v.add(c);
			children.put(c.getName(), v);
		} else if (o instanceof EVector) {
			((EVector) o).add(c);
		}
		c.parent = this;
		return c;
	}

	/**
	 * Gibt das Kind per Name mit id = 0 zur"uck. Sonst keins.
	 *
	 * @param name Name des Kindes
	 *
	 * @return Element oder null;
	 */

	public PElement getChild(String name) {
		PElement child0 = getChild(name);
		if (child0 != null)
			return child0;
		return null;
	}

	/**
	 * Verpflanzen eines Objektes oder Root-machen, indem pel = null
	 * @param pel neues Eltern-Element
	 */

	public void setRoot(PElement pel) {
		parent = pel;
	}

	/**
	 * Gibt alle Kind-Elemente zur"uck
	 *
	 * @return EVector mit Kind-Elementen
	 */

	public EVector getChildren() {
		if (!hasChildren())
			return new EVector();
		else
			return (EVector) fastChildren;
	}

	public PElement getFirstChild() {
		if (!hasChildren())
			return null;
		else
			return (PElement) fastChildren.elementAt(0);
	}

	protected Iterator getSortedChildren() {
		return children.values().iterator();
	}

	/**
	 * Gibt alle Kind elemente mit einem bestimmten Namen zur"uck
	 *
	 * @param name Name der gesuchten Elemente oder null f"ur alle Kinder
	 * @return EVector leerer Vector bei keinen Kindern
	 */

	public EVector getChildren(String name) {
		EVector v = new EVector();
		if (children == null)
			return v;

		Iterator iter = children.values().iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof EVector) {
				for (int i = 0; i < ((EVector) o).size(); i++) {
					PElement pe = ((EVector) o).elementAt(i);
					if (name == null || pe.name.equals(name))
						v.add(pe);
				}
			} else if (o instanceof PElement) {
				if (name == null || ((PElement) o).name.equals(name))
					v.add((PElement) o);
			}
		}

		return v;
	}

	public boolean hasAttributes() {
		return attributes != null;
	}

	public boolean hasChildren() {
		return ((children != null && children.size() > 0) ? true : false);
	}

	public void removeAllChildren() {
		fastChildren = null;
		children = null;
	}

	public void removeChild(PElement rc) {
		if (rc == null)
			return;

		if (hasChildren()) {
			fastChildren.remove(rc);
			Object o = children.get(rc.name);
			//       if(o!=null) rc.parent=null;
			if (o instanceof PElement) {
				PElement child = (PElement) o;
				children.remove(rc.name);
			} else if (o instanceof EVector)
				((EVector) o).remove(rc);
		}
	}

	//  /**
	//   * Ausgabe als formatiertes XML
	//   *
	//   * @return formatiertes XML
	//   */
	//
	//  public String serialize () {
	//    return serialize (true);
	//  }
	//
	//  public String serialize(boolean nice) {
	//    return serialize(0,nice);
	//  }
	//
	//  public String toXML(String encoding, boolean nice) {
	//      return ("<?xml version=\"1.0\" " + (encoding != null ? "encoding=\""+encoding+"\"" : "") + "?>\n"+this.serialize(nice));
	//   }
	//
	//   public void writeXML (OutputStream os) throws IOException {
	//     os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes("UTF-8"));
	//     serializeToStream(0, true, os);
	//   }
	//  /**
	//   * Interne abhandlung
	//   *
	//   * @param ident wieviele White-Spaces zum Einruecken
	//   * @return formatiertes XML
	//   */
	//
	//  public String serialize(int ident,boolean nice) {
	//    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	//    try {
	//      serializeToStream (ident, nice, bos);
	//      return bos.toString("UTF-8");
	//    } catch (Exception e) {}
	//    return null;
	//  }
	//
	//  private static byte[][] SIDENT = new byte[][] { { }, { ' ' }, { ' ', ' ' }, { ' ', ' ', ' ' },  { ' ', ' ', ' ', ' ' },
	//      { ' ', ' ', ' ', ' ', ' ' }, { ' ', ' ', ' ', ' ', ' ', ' ' }, { ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, { ' ', ' ', ' ', ' ', ' ', ' ',' ' ,' ' },
	// { ' ', ' ', ' ', ' ', ' ', ' ',' ' ,' ', ' ' }, { ' ', ' ', ' ', ' ', ' ', ' ',' ' ,' ', ' ',' ' } };
	//  private static byte[] LT = new byte[] { '<' }, GT = new byte[] { '>' }, SGT = new byte[] { '/', '>' },
	//  NL = new byte[] { '\n' }, LTS = new byte[] { '<', '/' };
	//
	// private void serializeToStream (int ident, boolean nice, OutputStream stream) throws IOException, UnsupportedEncodingException{
	//
	//    byte[] identS = new byte[0];
	//    byte[] line=nice ? NL: new byte[0];
	//    if(nice) {
	//      if (ident < 11) identS = SIDENT[ident];
	//      else { identS = new byte[ident]; for (int i = 0;i < ident;i++) identS[i] = ' '; }
	//    }
	//
	//    stream.write(identS); stream.write (LT); stream.write (name.getBytes("UTF-8"));
	//    if (attributes != null) {
	//      Enumeration en = attributes.keys();
	//      while (en.hasMoreElements()) {
	//        String name = (String)en.nextElement();
	//        String val = (String)attributes.get(name);
	//        stream.write((" " + name + "=\"" + val + "\"").getBytes("UTF-8"));
	//      }
	//    }
	//
	//    if (children == null && (content == null || content.trim().equals(""))) stream.write(SGT);
	//    else {
	//      stream.write(GT);
	//      if (children != null) {
	//        if (line != null) stream.write(line);
	//        EVector iter = getChildren();
	//        Object o;
	//        for (int i = 0;i < iter.size();i++) {
	//          o = iter.elementAt(i);
	//          if  (o instanceof PElement) {
	//            ((PElement)o).serializeToStream(ident + 1,nice, stream);
	//            stream.write(line);
	//          }
	//          else if (o instanceof EVector) {
	//            EVector v = (EVector)o;
	//            for (int j = 0;j < v.size();j++) {
	//              v.elementAt(j).serializeToStream(ident + 1,nice, stream);
	//              stream.write(line);
	//            }
	//          }
	//        }
	//      }
	//      if (content != null) { if (children != null) stream.write(identS ); stream.write(content.getBytes("UTF-8")); }
	//      else stream.write(identS);
	//
	//      stream.write(LTS); stream.write(name.getBytes("UTF-8")); stream.write(GT);
	//
	//    }
	//  }

	public String toString() {
		return getContent() != null ? getContent() : "";
	}

	/**
	 * Bequemlichkeitsverpackung f"ur addChild.
	 *
	 * @param p
	 */
	public void appendChild(PElement p) {
		addChild(p);
	}

}
