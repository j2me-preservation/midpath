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


import java.io.IOException;

import javax.microedition.midlet.MIDlet;

import org.thenesis.midpath.ui.SDLToolkit;

import com.sun.midp.chameleon.CGraphicsQ;
import com.sun.midp.chameleon.ChamDisplayTunnel;
import com.sun.midp.chameleon.MIDPWindow;
import com.sun.midp.chameleon.layers.PopupLayer;
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.configurator.Constants;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.lcdui.DisplayAccess;
import com.sun.midp.lcdui.DisplayContainer;
import com.sun.midp.lcdui.DisplayDeviceAccess;
import com.sun.midp.lcdui.DisplayEventConsumer;
import com.sun.midp.lcdui.DisplayEventHandlerFactory;
import com.sun.midp.lcdui.DisplayEventProducer;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.lcdui.RepaintEventProducer;
import com.sun.midp.lcdui.SystemAlert;
import com.sun.midp.log.Logging;
import com.sun.midp.main.MIDletControllerEventProducer;
import com.sun.midp.midlet.MIDletEventConsumer;
import com.sun.midp.midlet.MIDletPeer;
import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.util.ResourceHandler;

/**
 * <code>Display</code> represents the manager of the display and
 * input devices of the
 * system. It includes methods for retrieving properties of the device and
 * for requesting that objects be displayed on the device. Other methods that
 * deal with device attributes are primarily used with {@link Canvas Canvas}
 * objects and are thus defined there instead of here. <p>
 *
 * There is exactly one instance of Display per {@link
 * javax.microedition.midlet.MIDlet MIDlet} and the application can get a
 * reference to that instance by calling the {@link
 * #getDisplay(javax.microedition.midlet.MIDlet) getDisplay()} method. The
 * application may call the <code>getDisplay()</code> method at any time
 * during course of
 * its execution. The <code>Display</code> object
 * returned by all calls to <code>getDisplay()</code> will remain the
 * same during this
 * time. <p>
 *
 * A typical application will perform the following actions in response to
 * calls to its <code>MIDlet</code> methods:
 * <UL>
 * <LI><STRONG>startApp</STRONG> - the application is moving from the
 * paused state to the active state.
 * Initialization of objects needed while the application is active should be
 * done.  The application may call
 * {@link #setCurrent(Displayable) setCurrent()} for the first screen if that
 * has not already been done. Note that <code>startApp()</code> can be
 * called several
 * times if <code>pauseApp()</code> has been called in between. This
 * means that one-time
 * initialization
 * should not take place here but instead should occur within the
 * <code>MIDlet's</code>
 * constructor.
 * </LI>
 * <LI><STRONG>pauseApp</STRONG> - the application may pause its threads.
 * Also, if it is
 * desirable to start with another screen when the application is re-activated,
 * the new screen should be set with <code>setCurrent()</code>.</LI>
 * <LI><STRONG>destroyApp</STRONG> - the application should free resources,
 * terminate threads, etc.
 * The behavior of method calls on user interface objects after
 * <code>destroyApp()</code> has returned is undefined. </li>
 * </UL>
 * <p>
 *
 * <P>The user interface objects that are shown on the display device are
 * contained within a {@link Displayable Displayable} object. At any time the
 * application may have at most one <code>Displayable</code> object
 * that it intends to be
 * shown on the display device and through which user interaction occurs.  This
 * <code>Displayable</code> is referred to as the <em>current</em>
 * <code>Displayable</code>. </p>
 *
 * <P>The <code>Display</code> class has a {@link
 * #setCurrent(Displayable) setCurrent()}
 * method for setting the current <code>Displayable</code> and a
 * {@link #getCurrent()
 * getCurrent()} method for retrieving the current
 * <code>Displayable</code>.  The
 * application has control over its current <code>Displayable</code>
 * and may call
 * <code>setCurrent()</code> at any time.  Typically, the application
 * will change the
 * current <code>Displayable</code> in response to some user action.
 * This is not always the
 * case, however.  Another thread may change the current
 * <code>Displayable</code> in
 * response to some other stimulus.  The current
 * <code>Displayable</code> will also be
 * changed when the timer for an {@link Alert Alert} elapses. </P>
 *
 * <p> The application's current <code>Displayable</code> may not
 * physically be drawn on the
 * screen, nor will user events (such as keystrokes) that occur necessarily be
 * directed to the current <code>Displayable</code>.  This may occur
 * because of the presence
 * of other <code>MIDlet</code> applications running simultaneously on
 * the same device. </p>
 *
 * <P>An application is said to be in the <em>foreground</em> if its current
 * <code>Displayable</code> is actually visible on the display device
 * and if user input
 * device events will be delivered to it. If the application is not in the
 * foreground, it lacks access to both the display and input devices, and it is
 * said to be in the <em>background</em>. The policy for allocation of these
 * devices to different <code>MIDlet</code> applications is outside
 * the scope of this
 * specification and is under the control of an external agent referred to as
 * the <em>application management software</em>. </p>
 *
 * <P>As mentioned above, the application still has a notion of its current
 * <code>Displayable</code> even if it is in the background. The
 * current <code>Displayable</code> is
 * significant, even for background applications, because the current
 * <code>Displayable</code> is always the one that will be shown the
 * next time the
 * application is brought into the foreground.  The application can determine
 * whether a <code>Displayable</code> is actually visible on the
 * display by calling {@link
 * Displayable#isShown isShown()}. In the case of <code>Canvas</code>,
 * the {@link
 * Canvas#showNotify() showNotify()} and {@link Canvas#hideNotify()
 * hideNotify()} methods are called when the <code>Canvas</code> is
 * made visible and is
 * hidden, respectively.</P>
 *
 * <P> Each <code>MIDlet</code> application has its own current
 * <code>Displayable</code>.  This means
 * that the {@link #getCurrent() getCurrent()} method returns the
 * <code>MIDlet's</code>
 * current <code>Displayable</code>, regardless of the
 * <code>MIDlet's</code> foreground/background
 * state.  For example, suppose a <code>MIDlet</code> running in the
 * foreground has current
 * <code>Displayable</code> <em>F</em>, and a <code>MIDlet</code>
 * running in the background has current
 * <code>Displayable</code> <em>B</em>.  When the foreground
 * <code>MIDlet</code> calls <code>getCurrent()</code>, it
 * will return <em>F</em>, and when the background <code>MIDlet</code>
 * calls <code>getCurrent()</code>, it
 * will return <em>B</em>.  Furthermore, if either <code>MIDlet</code>
 * changes its current
 * <code>Displayable</code> by calling <code>setCurrent()</code>, this
 * will not affect the any other
 * <code>MIDlet's</code> current <code>Displayable</code>. </p>
 *
 * <P>It is possible for <code>getCurrent()</code> to return
 * <code>null</code>. This may occur at startup
 * time, before the <code>MIDlet</code> application has called
 * <code>setCurrent()</code> on its first
 * screen.  The <code>getCurrent(</code>) method will never return a
 * reference to a
 * <code>Displayable</code> object that was not passed in a prior call
 * to <code>setCurrent()</code> call
 * by this <code>MIDlet</code>. </p>
 *
 * <a name="systemscreens"></a>
 * <h3>System Screens</h3>
 *
 * <P> Typically, the
 * current screen of the foreground <code>MIDlet</code> will be
 * visible on the display.
 * However, under certain circumstances, the system may create a screen that
 * temporarily obscures the application's current screen.  These screens are
 * referred to as <em>system screens.</em> This may occur if the system needs
 * to show a menu of commands or if the system requires the user to edit text
 * on a separate screen instead of within a text field inside a
 * <code>Form</code>.  Even
 * though the system screen obscures the application's screen, the notion of
 * the current screen does not change.  In particular, while a system screen is
 * visible, a call to <code>getCurrent()</code> will return the
 * application's current
 * screen, not the system screen.  The value returned by
 * <code>isShown()</code> is <code>false</code>
 * while the current <code>Displayable</code> is obscured by a system
 * screen. </p>
 *
 * <p> If system screen obscures a canvas, its
 * <code>hideNotify()</code> method is called.
 * When the system screen is removed, restoring the canvas, its
 * <code>showNotify()</code>
 * method and then its <code>paint()</code> method are called.  If the
 * system screen was used
 * by the user to issue a command, the <code>commandAction()</code>
 * method is called after
 * <code>showNotify()</code> is called. </p>
 *
 * <p>This class contains methods to retrieve the prevailing foreground and
 * background colors of the high-level user interface.  These methods are
 * useful for creating <CODE>CustomItem</CODE> objects that match the user
 * interface of other items and for creating user interfaces within
 * <CODE>Canvas</CODE> that match the user interface of the rest of the
 * system.  Implementations are not restricted to using foreground and
 * background colors in their user interfaces (for example, they might use
 * highlight and shadow colors for a beveling effect) but the colors returned
 * are those that match reasonably well with the implementation's color
 * scheme.  An application implementing a custom item should use the
 * background color to clear its region and then paint text and geometric
 * graphics (lines, arcs, rectangles) in the foreground color.</p>
 *
 * @since MIDP 1.0
 */

public class Display {

	/*
	 * ************* public member variables
	 */

	/**
	 * Image type for <code>List</code> element image.
	 *
	 * <P>The value of <code>LIST_ELEMENT</code> is <code>1</code>.</P>
	 * 
	 * @see #getBestImageWidth(int imageType)
	 * @see #getBestImageHeight(int imageType)
	 */
	public static final int LIST_ELEMENT = 1;

	/**
	 * Image type for <code>ChoiceGroup</code> element image.
	 * 
	 * <P>The value of <code>CHOICE_GROUP_ELEMENT</code> is <code>2</code>.</P>
	 * 
	 * @see #getBestImageWidth(int imageType)
	 * @see #getBestImageHeight(int imageType)
	 */
	public static final int CHOICE_GROUP_ELEMENT = 2;

	/**
	 * Image type for <code>Alert</code> image.
	 * 
	 * <P>The value of <code>ALERT</code> is <code>3</code>.</P>
	 * 
	 * @see #getBestImageWidth(int imageType)
	 * @see #getBestImageHeight(int imageType)
	 */
	public static final int ALERT = 3;

	/**
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_BACKGROUND</code> specifies the background color of
	 * the screen.
	 * The background color will always contrast with the foreground color.
	 *
	 * <p>
	 * <code>COLOR_BACKGROUND</code> has the value <code>0</code>.
	 *
	 * @see #getColor
	 */
	public static final int COLOR_BACKGROUND = 0;

	/** 
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_FOREGROUND</code> specifies the foreground color,
	 * for text characters
	 * and simple graphics on the screen.  Static text or user-editable
	 * text should be drawn with the foreground color.  The foreground color
	 * will always constrast with background color.
	 *
	 * <p> <code>COLOR_FOREGROUND</code> has the value <code>1</code>.
	 *
	 * @see #getColor 
	 */
	public static final int COLOR_FOREGROUND = 1;

	/**
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_HIGHLIGHTED_BACKGROUND</code> identifies the color for the
	 * focus, or focus highlight, when it is drawn as a
	 * filled in rectangle. The highlighted
	 * background will always constrast with the highlighted foreground.
	 *
	 * <p>
	 * <code>COLOR_HIGHLIGHTED_BACKGROUND</code> has the value <code>2</code>.
	 *
	 * @see #getColor
	 */
	public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;

	/**
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_HIGHLIGHTED_FOREGROUND</code> identifies the color for text
	 * characters and simple graphics when they are highlighted. 
	 * Highlighted
	 * foreground is the color to be used to draw the highlighted text
	 * and graphics against the highlighted background.
	 * The highlighted foreground will always constrast with
	 * the highlighted background.
	 *
	 * <p>
	 * <code>COLOR_HIGHLIGHTED_FOREGROUND</code> has the value <code>3</code>.
	 *
	 * @see #getColor
	 */
	public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;

	/**
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_BORDER</code> identifies the color for boxes and borders
	 * when the object is to be drawn in a
	 * non-highlighted state.  The border color is intended to be used with 
	 * the background color and will contrast with it.
	 * The application should draw its borders using the stroke style returned 
	 * by <code>getBorderStyle()</code>.
	 *
	 * <p> <code>COLOR_BORDER</code> has the value <code>4</code>.
	 *
	 * @see #getColor
	 */
	public static final int COLOR_BORDER = 4;

	/**
	 * A color specifier for use with <code>getColor</code>.
	 * <code>COLOR_HIGHLIGHTED_BORDER</code>
	 * identifies the color for boxes and borders when the object is to be
	 * drawn in a highlighted state.  The highlighted border color is intended
	 * to be used with the background color (not the highlighted background
	 * color) and will contrast with it.  The application should draw its
	 * borders using the stroke style returned <code>by getBorderStyle()</code>.
	 *
	 * <p> <code>COLOR_HIGHLIGHTED_BORDER</code> has the value <code>5</code>.
	 *
	 * @see #getColor
	 */
	public static final int COLOR_HIGHLIGHTED_BORDER = 5;

	/*
	 * ************* protected member variables
	 */

	/*
	 * ************* package private member variables
	 */

	/** Static lock object for LCDUI package. */
	static final Object LCDUILock = new Object();

	/** Static lock object for making calls into application code. */
	static final Object calloutLock = new Object();

	/** Current display width. */
	static int WIDTH;

	/** Current display height. */
	static int HEIGHT;

	/** ID of this Display. */
	int displayId;

	/** Display access helper class. */
	DisplayAccessImpl accessor;

	/** Display event consumer helper class. */
	DisplayEventConsumerImpl consumer;

	/** Chameleon Display tunnel helper class */
	ChamDisplayTunnel cham_tunnel;

	/** Chameleon loop variable for holding the refresh region */
	int[] region;

	/** Consumer of midlet lifecycle events. */
	MIDletEventConsumer midletEventConsumer;

	/** Producer of midlet lifecycle events. */
	MIDletControllerEventProducer midletControllerEventProducer;

	/** Display event handler with private methods. */
	/* private */static DisplayEventHandlerImpl displayEventHandlerImpl;

	/** 
	 * <code>true</code>, if the <code>Display</code> is the foreground 
	 * object. 
	 */
	boolean hasForeground = true; //  = false;

	/*
	 * ************* private member variables
	 */
	/** Device Access manager. */
	private static DisplayDeviceAccess deviceAccess;

	/** Cached reference to the MIDletControllerEventProducer. */
	private static MIDletControllerEventProducer defaultMidletControllerEventProducer;

	/** Producer for Display events. */
	private static DisplayEventProducer displayEventProducer;

	/** Producer for Repaint events. */
	private static RepaintEventProducer repaintEventProducer;

	/** Cached reference to Active Displays Container. */
	private static DisplayContainer displayContainer;

	/** About abstract command. */
	private static Command aboutCommand;

	/** Command to terminate a headless midlet */
	private static Command exitCommand;

	/** Command to bring a headless midlet into the background state */

	private static Command okCommand;

	/** Device <code>Graphics</code> associated with this Display. */
	private Graphics screenGraphics;

	/** wantsForeground state when the MIDlet wants to be in the background. */
	private static final int WANTS_BACKGROUND = 0;

	/** wantsForeground state when the MIDlet wants to be in the foreground. */
	private static final int WANTS_FOREGROUND = 1;

	/**
	 * wantsForeground state when the MIDlet wants to be in the foreground
	 * to display an alert.
	 */
	private static final int WANTS_FOREGROUND_FOR_ALERT = 2;

	/**
	 * Since the current Displayable can't be set to null we use a
	 * separate state to keep track of if the MIDlet wants the foreground or
	 * not.
	 */
	private int wantsForeground = WANTS_BACKGROUND;

	/**
	 * The actual current DisplayableLF instance. This will be null initially. 
	 */
	private DisplayableLF current;

	/**
	 * The DisplayableLF that is in the process of becoming current. This is
	 * non-null only in the midst of the actual screen change. It is null at
	 * all other times. This is used to modify the behavior getCurrent(). The
	 * value of 'current' is null momentarily during the screen change, and
	 * during these times getCurrent() returns the value of transitionCurrent
	 * instead of null.
	 */
	private DisplayableLF transitionCurrent;

	/**
	 * Holds a reference to the DisplayableLF most recently requested to 
	 * become current, whether through setCurrent() or through the dismissal 
	 * of an alert. Note that this variable is not cleared after screen change 
	 * processing completes. When the system is quiescent, pendingCurrent will 
	 * equal current. When there is a screen-change event in the queue, 
	 * pendingCurrent will generally differ from current. This is not true
	 * in the following special case: if the application hasn't called 
	 * setCurrent, current may point to a headlessAlert but pendingCurrent 
	 * will be null.
	 */
	private DisplayableLF pendingCurrent;

	/** Chameleon rendering window */
	private MIDPWindow window;

	/** Chameleon graphics queue */
	private CGraphicsQ graphicsQ;

	/** Chameleon tunnel to romized Image constructor */
	private static ChamImageTunnel chamImageTunnel;

	/**
	 * <code>True</code> if a system screen, like menu, is up.
	 */
	private boolean paintSuspended; // = false

	/** First queue of serialized repaint operations. */
	private static java.util.Vector queue1 = new java.util.Vector();

	/** Second queue of serialized repaint operations. */
	private static java.util.Vector queue2 = new java.util.Vector();

	/** Current active queue for serially repainted operations. */
	private static java.util.Vector currentQueue = queue1;

	/** Security token to allow access to implementation APIs */
	private static SecurityToken classSecurityToken;
	/*
	 * ************* Static initializer, constructor
	 */
	static {
		classSecurityToken = SecurityInitializer.getSecurityToken();

		WIDTH = Constants.CHAM_WIDTH;
		HEIGHT = Constants.CHAM_HEIGHT;

		chamImageTunnel = new ChamImageTunnel();

		com.sun.midp.chameleon.skins.resources.SkinResources.initSecurityToken(classSecurityToken, chamImageTunnel);

		/* Let com.sun.midp classes call in to this class. */
		displayEventHandlerImpl = new DisplayEventHandlerImpl();
		DisplayEventHandlerFactory.SetDisplayEventHandlerImpl(displayEventHandlerImpl);
		deviceAccess = new DisplayDeviceAccess();

		aboutCommand = new Command(Resource.getString(ResourceConstants.ABOUT), Command.HELP, 3);
		aboutCommand.setInternalID(EventConstants.ABOUT_REQUESTED);

		exitCommand = new Command(Resource.getString(ResourceConstants.EXIT), Command.EXIT, 1);

		okCommand = new Command(Resource.getString(ResourceConstants.OK), Command.OK, 2);
		
		SDLToolkit toolkit = SDLToolkit.getToolkit();
		toolkit.initialize(WIDTH, HEIGHT);

	}

	/**
	 * Initializes the display with an accessor helper class.
	 *
	 * @param c MIDletEventConsumer I/F implementor
	 */
	Display(MIDletEventConsumer c) {
		midletEventConsumer = c;

		/** create DisplayAccess I/F implementor */
		accessor = new DisplayAccessImpl();

		/** create DisplayEventConsumer I/F implementor */
		consumer = new DisplayEventConsumerImpl();

		/*
		 * Initialize midlet controller event producer from the default.
		 * This is useful so that subclasses can override the producer for 
		 * testing purposes.
		 */
		midletControllerEventProducer = defaultMidletControllerEventProducer;

		/** register display in container and unique Id for it 
		 * ID is set inside DisplayContainer.addDisplay()
		 * in DisplayAcccess.setdisplayId() call.
		 * Thus explicit displayId assignment here is redundant.    
		 */
		/* displayId = */displayContainer.addDisplay(accessor);

		screenGraphics = Graphics.getScreenGraphics(displayId, WIDTH, HEIGHT);

		drawTrustedIcon0(displayId, false);

		graphicsQ = new CGraphicsQ();
		cham_tunnel = new ChameleonTunnel();
		window = new MIDPWindow(cham_tunnel);
		region = new int[4];

	}

	/**
	 * Initializes the display class. Shall be called only from 
	 * displayEventHandlerImpl initialization methods.
	 *
	 * @param theMIDletControllerEventProducer producer for midlet events
	 * @param theDisplayEventProducer producer for display events
	 * @param theRepaintEventProducer producer for repaint events
	 */
	static void initClass(MIDletControllerEventProducer theMIDletControllerEventProducer,
			DisplayEventProducer theDisplayEventProducer, RepaintEventProducer theRepaintEventProducer,
			DisplayContainer theDisplayContainer) {

		defaultMidletControllerEventProducer = theMIDletControllerEventProducer;
		displayEventProducer = theDisplayEventProducer;
		repaintEventProducer = theRepaintEventProducer;
		displayContainer = theDisplayContainer;
	}

	/*
	 * ************* public methods
	 */

	/**
	 * Gets the <code>Display</code> object that is unique to this
	 * <code>MIDlet</code>.
	 * @param m <code>MIDlet</code> of the application
	 * @return the display object that application can use for its user
	 * interface
	 *
	 * @throws NullPointerException if <code>m</code> is <code>null</code>
	 */
	public static Display getDisplay(MIDlet m) {
		MIDletPeer ms;
		Display d;

		synchronized (LCDUILock) {
			// Find or create if necessary the Display
			ms = MIDletPeer.getMIDletPeer(classSecurityToken, m);
			if (ms != null) {
				d = ms.getDisplay();
				if (d != null) {
					return d;
				}
			}

			throw new IllegalStateException("No display created for given MIDlet");
		}
	}

	/**
	 * Returns one of the colors from the high level user interface
	 * color scheme, in the form <code>0x00RRGGBB</code> based on the
	 * <code>colorSpecifier</code> passed in.
	 *
	 * @param colorSpecifier the predefined color specifier;
	 *  must be one of
	 *  {@link #COLOR_BACKGROUND},
	 *  {@link #COLOR_FOREGROUND},
	 *  {@link #COLOR_HIGHLIGHTED_BACKGROUND},
	 *  {@link #COLOR_HIGHLIGHTED_FOREGROUND},
	 *  {@link #COLOR_BORDER}, or
	 *  {@link #COLOR_HIGHLIGHTED_BORDER}
	 * @return color in the form of <code>0x00RRGGBB</code>
	 * @throws IllegalArgumentException if <code>colorSpecifier</code>
	 * is not a valid color specifier
	 */
	public int getColor(int colorSpecifier) {

		switch (colorSpecifier) {
		case COLOR_BACKGROUND:
			return ScreenSkin.COLOR_BG;
		case COLOR_FOREGROUND:
			return ScreenSkin.COLOR_FG;
		case COLOR_HIGHLIGHTED_BACKGROUND:
			return ScreenSkin.COLOR_BG_HL;
		case COLOR_HIGHLIGHTED_FOREGROUND:
			return ScreenSkin.COLOR_FG_HL;
		case COLOR_BORDER:
			return ScreenSkin.COLOR_BORDER;
		case COLOR_HIGHLIGHTED_BORDER:
			return ScreenSkin.COLOR_BORDER_HL;
		default:
			throw new IllegalArgumentException();
		}

	}

	/**
	 * Returns the stroke style used for border drawing
	 * depending on the state of the component
	 * (highlighted/non-highlighted). For example, on a monochrome
	 * system, the border around a non-highlighted item might be
	 * drawn with a <code>DOTTED</code> stroke style while the border around a
	 * highlighted item might be drawn with a <code>SOLID</code> stroke style.
	 *
	 * @param highlighted <code>true</code> if the border style being
	 * requested is for the
	 * highlighted state, <code>false</code> if the border style being
	 * requested is for the
	 * non-highlighted state
	 * @return {@link Graphics#DOTTED} or {@link Graphics#SOLID}
	 */
	public int getBorderStyle(boolean highlighted) {

		return ScreenSkin.BORDER_STYLE;

	}

	/**
	 * Gets information about color support of the device.
	 * @return <code>true</code> if the display supports color, 
	 * <code>false</code> otherwise
	 */
	public boolean isColor() {
		return Constants.DISPLAY_IS_COLOR;
	}

	/**
	 * Gets the number of colors (if <code>isColor()</code> is
	 * <code>true</code>)
	 * or graylevels (if <code>isColor()</code> is <code>false</code>)
	 * that can be
	 * represented on the device.<P>
	 * Note that the number of colors for a black and white display is
	 * <code>2</code>.
	 * @return number of colors
	 */
	public int numColors() {
		return (Constants.DISPLAY_NUM_COLOR);
	}

	/**
	 * Gets the number of alpha transparency levels supported by this
	 * implementation.  The minimum legal return value is
	 * <code>2</code>, which indicates
	 * support for full transparency and full opacity and no blending.  Return
	 * values greater than <code>2</code> indicate that alpha blending
	 * is supported.  For
	 * further information, see <a href="Image.html#alpha">Alpha
	 * Processing</a>.
	 *
	 * @return number of alpha levels supported
	 */
	public int numAlphaLevels() {
		return Constants.ALPHA_LEVELS;
	}

	/**
	 * Gets the current <code>Displayable</code> object for this
	 * <code>MIDlet</code>.  The
	 * <code>Displayable</code> object returned may not actually be
	 * visible on the display
	 * if the <code>MIDlet</code> is running in the background, or if
	 * the <code>Displayable</code> is
	 * obscured by a system screen.  The {@link Displayable#isShown()
	 * Displayable.isShown()} method may be called to determine whether the
	 * <code>Displayable</code> is actually visible on the display. 
	 *
	 * <p> The value returned by <code>getCurrent()</code> may be
	 * <code>null</code>. This
	 * occurs after the application has been initialized but before the first
	 * call to <code>setCurrent()</code>. </p>
	 *
	 * @return the <code>MIDlet's</code> current <code>Displayable</code> object
	 * @see #setCurrent
	 */
	public Displayable getCurrent() {
		synchronized (LCDUILock) {
			return (current == null) ? ((transitionCurrent == null) ? null : transitionCurrent.lGetDisplayable())
					: (isHeadless(current) ? null : current.lGetDisplayable());
		}
	}

	/**
	 * Requests that a different <code>Displayable</code> object be
	 * made visible on the
	 * display.  The change will typically not take effect immediately.  It
	 * may be delayed so that it occurs between event delivery method
	 * calls, although it is not guaranteed to occur before the next event
	 * delivery method is called.  The <code>setCurrent()</code> method returns
	 * immediately, without waiting for the change to take place.  Because of
	 * this delay, a call to <code>getCurrent()</code> shortly after a
	 * call to <code>setCurrent()</code>
	 * is unlikely to return the value passed to <code>setCurrent()</code>.
	 *
	 * <p> Calls to <code>setCurrent()</code> are not queued.  A
	 * delayed request made by a
	 * <code>setCurrent()</code> call may be superseded by a subsequent call to
	 * <code>setCurrent()</code>.  For example, if screen
	 * <code>S1</code> is current, then </p>
	 *
	 * <TABLE BORDER="2">
	 * <TR>
	 * <TD ROWSPAN="1" COLSPAN="1">
	 *    <pre><code>
	 *     d.setCurrent(S2);
	 *     d.setCurrent(S3);     </code></pre>
	 * </TD>
	 * </TR>
	 * </TABLE>
	 *
	 * <p> may eventually result in <code>S3</code> being made
	 * current, bypassing <code>S2</code>
	 * entirely. </p>
	 *
	 * <p> When a <code>MIDlet</code> application is first started,
	 * there is no current
	 * <code>Displayable</code> object.  It is the responsibility of
	 * the application to
	 * ensure that a <code>Displayable</code> is visible and can
	 * interact with the user at
	 * all times.  Therefore, the application should always call
	 * <code>setCurrent()</code>
	 * as part of its initialization. </p>
	 *
	 * <p> The application may pass <code>null</code> as the argument to
	 * <code>setCurrent()</code>.  This does not have the effect of
	 * setting the current
	 * <code>Displayable</code> to <code>null</code>; instead, the
	 * current <code>Displayable</code>
	 * remains unchanged.  However, the application management software may
	 * interpret this call as a request from the application that it is
	 * requesting to be placed into the background.  Similarly, if the
	 * application is in the background, passing a non-null
	 * reference to <code>setCurrent()</code> may be interpreted by
	 * the application
	 * management software as a request that the application is
	 * requesting to be
	 * brought to the foreground.  The request should be considered to be made
	 * even if the current <code>Displayable</code> is passed to the
	 * <code>setCurrent()</code>.  For
	 * example, the code </p>
	 * <TABLE BORDER="2">
	 * <TR>
	 * <TD ROWSPAN="1" COLSPAN="1">
	 *    <pre><code>
	 *   d.setCurrent(d.getCurrent());    </code></pre>
	 * </TD>
	 * </TR>
	 * </TABLE>
	 * <p> generally will have no effect other than requesting that the
	 * application be brought to the foreground.  These are only requests,
	 * and there is no requirement that the application management
	 * software comply with these requests in a timely fashion if at all. </p>
	 *
	 * <p> If the <code>Displayable</code> passed to
	 * <code>setCurrent()</code> is an {@link Alert
	 * Alert}, the previously current <code>Displayable</code>, if
	 * any, is restored after
	 * the <code>Alert</code> has been dismissed.  If there is a
	 * current <code>Displayable</code>, the
	 * effect is as if <code>setCurrent(Alert, getCurrent())</code>
	 * had been called.  Note
	 * that this will result in an exception being thrown if the current
	 * <code>Displayable</code> is already an alert.  If there is no
	 * current <code>Displayable</code>
	 * (which may occur at startup time) the implementation's previous state
	 * will be restored after the <code>Alert</code> has been
	 * dismissed.  The automatic
	 * restoration of the previous <code>Displayable</code> or the
	 * previous state occurs
	 * only when the <code>Alert's</code> default listener is present
	 * on the <code>Alert</code> when it
	 * is dismissed.  See <a href="Alert.html#commands">Alert Commands and
	 * Listeners</a> for details.</p>
	 *
	 * <p>To specify the
	 * <code>Displayable</code> to be shown after an
	 * <code>Alert</code> is dismissed, the application
	 * should use the {@link #setCurrent(Alert,Displayable) setCurrent(Alert,
	 * Displayable)} method.  If the application calls
	 * <code>setCurrent()</code> while an
	 * <code>Alert</code> is current, the <code>Alert</code> is
	 * removed from the display and any timer
	 * it may have set is cancelled. </p>
	 *
	 * <p> If the application calls <code>setCurrent()</code> while a
	 * system screen is
	 * active, the effect may be delayed until after the system screen is
	 * dismissed.  The implementation may choose to interpret
	 * <code>setCurrent()</code> in
	 * such a situation as a request to cancel the effect of the system
	 * screen, regardless of whether <code>setCurrent()</code> has
	 * been delayed. </p>
	 *
	 * @param nextDisplayable the <code>Displayable</code> requested
	 * to be made current;
	 * <code>null</code> is allowed
	 * @see #getCurrent
	 */
	public void setCurrent(Displayable nextDisplayable) {
		synchronized (LCDUILock) {
			if (nextDisplayable instanceof Alert) {
				/*
				 * This implicitly goes back to the current screen, or to a 
				 * pending screen, if there is one. We disallow stacking of 
				 * alerts by throwing IAE. Exceptions to this rule: if the 
				 * pending screen is the headless alert, or if this alert is 
				 * the same one that is pending.
				 *
				 * Existing midlets may be intolerant of the rule that throws 
				 * IAE if alerts are stacked. One approach that would allow 
				 * this is to have the second alert inherit the return screen 
				 * from the first alert.
				 */
				if (pendingCurrent == null) {
					((Alert) nextDisplayable).setReturnScreen(getHeadlessDisplayable());
				} else if (nextDisplayable == pendingCurrent.lGetDisplayable()) {
					// do nothing; this case is allowed
				} else {
					Displayable returnScreen = pendingCurrent.lGetDisplayable();

					if (!isHeadless(pendingCurrent) && returnScreen instanceof Alert) {
						throw new IllegalArgumentException();
					}

					((Alert) nextDisplayable).setReturnScreen(returnScreen);
				}
			}

			setCurrentImpl(nextDisplayable, null);
		} // synchronized
	}

	/**
	 * Requests that this <code>Alert</code> be made current, and that
	 * <code>nextDisplayable</code> be
	 * made current
	 * after the <code>Alert</code> is dismissed.  This call returns
	 * immediately regardless
	 * of the <code>Alert's</code> timeout value or whether it is a
	 * modal alert.  The
	 * <code>nextDisplayable</code> must not be an <code>Alert</code>,
	 * and it must not be <code>null</code>.
	 *
	 * <p>The automatic advance to <code>nextDisplayable</code> occurs only 
	 * when the <code>Alert's</code> default listener is present on
	 * the <code>Alert</code> when it
	 * is dismissed.  See <a href="Alert.html#commands">Alert Commands and
	 * Listeners</a> for details.</p>
	 * 
	 * <p> In other respects, this method behaves identically to
	 * {@link #setCurrent(Displayable) setCurrent(Displayable)}. </p>
	 *
	 * @param alert the alert to be shown
	 * @param nextDisplayable the <code>Displayable</code> to be
	 * shown after this alert is  dismissed
	 * 
	 * @throws NullPointerException if alert or
	 * <code>nextDisplayable</code> is <code>null</code>
	 * @throws IllegalArgumentException if <code>nextDisplayable</code>
	 * is an <code>Alert</code>
	 * @see Alert
	 * @see #getCurrent
	 */
	public void setCurrent(Alert alert, Displayable nextDisplayable) {
		if ((alert == null) || (nextDisplayable == null)) {
			throw new NullPointerException();
		}

		if (nextDisplayable instanceof Alert) {
			throw new IllegalArgumentException();
		}

		synchronized (LCDUILock) {
			alert.setReturnScreen(nextDisplayable);
			setCurrentImpl(alert, null);
		}
	}

	/**
	 * Requests that the <code>Displayable</code> that contains this
	 * <code>Item</code> be made current,
	 * scrolls the <code>Displayable</code> so that this
	 * <code>Item</code> is visible, and possibly
	 * assigns the focus to this <code>Item</code>.  The containing
	 * <code>Displayable</code> is first
	 * made current as if {@link #setCurrent(Displayable)
	 * setCurrent(Displayable)} had been called.  When the containing
	 * <code>Displayable</code> becomes current, or if it is already
	 * current, it is
	 * scrolled if necessary so that the requested <code>Item</code>
	 * is made visible.
	 * Then, if the implementation supports the notion of input focus, and if
	 * the <code>Item</code> accepts the input focus, the input focus
	 * is assigned to the
	 * <code>Item</code>.
	 *
	 * <p>This method always returns immediately, without waiting for the
	 * switching of the <code>Displayable</code>, the scrolling, and
	 * the assignment of
	 * input focus to take place.</p>
	 *
	 * <p>It is an error for the <code>Item</code> not to be contained
	 * within a container.
	 * It is also an error if the <code>Item</code> is contained
	 * within an <code>Alert</code>.</p>
	 *
	 * @param item the item that should be made visible
	 * @throws IllegalStateException if the item is not owned by a container
	 * @throws IllegalStateException if the item is owned by an 
	 * <code>Alert</code>
	 * @throws NullPointerException if <code>item</code> is <code>null</code>
	 */
	public void setCurrentItem(Item item) {

		synchronized (LCDUILock) {
			Screen nextDisplayable = item.owner;

			if (nextDisplayable == null || nextDisplayable instanceof Alert) {
				throw new IllegalStateException();
			}

			setCurrentImpl(nextDisplayable, item);

		} // synchronized
	}

	/**
	 * Causes the <code>Runnable</code> object <code>r</code> to have
	 * its <code>run()</code> method
	 * called later, serialized with the event stream, soon after completion of
	 * the repaint cycle.  As noted in the
	 * <a href="./package-summary.html#events">Event Handling</a>
	 * section of the package summary,
	 * the methods that deliver event notifications to the application
	 * are all called serially. The call to <code>r.run()</code> will
	 * be serialized along with
	 * the event calls into the application. The <code>run()</code>
	 * method will be called exactly once for each call to
	 * <code>callSerially()</code>. Calls to <code>run()</code> will
	 * occur in the order in which they were requested by calls to
	 * <code>callSerially()</code>.
	 *
	 * <p> If the current <code>Displayable</code> is a <code>Canvas</code>
	 * that has a repaint pending at the time of a call to
	 * <code>callSerially()</code>, the <code>paint()</code> method of the
	 * <code>Canvas</code> will be called and
	 * will return, and a buffer switch will occur (if double buffering is in
	 * effect), before the <code>run()</code> method of the
	 * <code>Runnable</code> is called.
	 * If the current <code>Displayable</code> contains one or more
	 * <code>CustomItems</code> that have repaints pending at the time
	 * of a call to <code>callSerially()</code>, the <code>paint()</code>
	 * methods of the <code>CustomItems</code> will be called and will
	 * return before the <code>run()</code> method of the
	 * <code>Runnable</code> is called.
	 * Calls to the
	 * <code>run()</code> method will occur in a timely fashion, but
	 * they are not guaranteed
	 * to occur immediately after the repaint cycle finishes, or even before
	 * the next event is delivered. </p>
	 *
	 * <p> The <code>callSerially()</code> method may be called from
	 * any thread. The call to
	 * the <code>run()</code> method will occur independently of the
	 * call to <code>callSerially()</code>.
	 * In particular, <code>callSerially()</code> will <em>never</em>
	 * block waiting
	 * for <code>r.run()</code>
	 * to return. </p>
	 *
	 * <p> As with other callbacks, the call to <code>r.run()</code>
	 * must return quickly. If
	 * it is necessary to perform a long-running operation, it may be initiated
	 * from within the <code>run()</code> method. The operation itself
	 * should be performed
	 * within another thread, allowing <code>run()</code> to return. </p>
	 *
	 * <p> The <code>callSerially()</code> facility may be used by
	 * applications to run an
	 * animation that is properly synchronized with the repaint cycle. A
	 * typical application will set up a frame to be displayed and then call
	 * <code>repaint()</code>.  The application must then wait until
	 * the frame is actually
	 * displayed, after which the setup for the next frame may occur.  The call
	 * to <code>run()</code> notifies the application that the
	 * previous frame has finished
	 * painting.  The example below shows <code>callSerially()</code>
	 * being used for this
	 * purpose. </p>
	 * <TABLE BORDER="2">
	 * <TR>
	 * <TD ROWSPAN="1" COLSPAN="1">
	 *    <pre><code>
	 *     class Animation extends Canvas
	 *         implements Runnable {
	 *
	 *     // paint the current frame
	 *     void paint(Graphics g) { ... }
	 *
	 *        Display display; // the display for the application
	 *
	 *        void paint(Graphics g) { ... } // paint the current frame
	 *
	 *        void startAnimation() {
	 *            // set up initial frame
	 *            repaint();
	 *            display.callSerially(this);
	 *        }
	 *
	 *        // called after previous repaint is finished
	 *        void run() {
	 *            if ( &#47;* there are more frames *&#47; ) {
	 *                // set up the next frame
	 *                repaint();
	 *                display.callSerially(this);
	 *            }
	 *        }
	 *     }    </code></pre>
	 * </TD>
	 * </TR>
	 * </TABLE>
	 * @param r instance of interface <code>Runnable</code> to be called
	 */
	public void callSerially(Runnable r) {
		if (r == null) {
			throw new NullPointerException();
		}
		synchronized (LCDUILock) {
			currentQueue.addElement(r);
			displayEventProducer.sendCallSeriallyEvent(consumer);
		}
	}

	/**
	 * Requests a flashing effect for the device's backlight.  The flashing
	 * effect is intended to be used to attract the user's attention or as a
	 * special effect for games.  Examples of flashing are cycling the
	 * backlight on and off or from dim to bright repeatedly.
	 * The return value indicates if the flashing of the backlight
	 * can be controlled by the application.
	 *
	 * <p>The flashing effect occurs for the requested duration, or it is
	 * switched off if the requested duration is zero.  This method returns
	 * immediately; that is, it must not block the caller while the flashing
	 * effect is running.</p>
	 *
	 * <p>Calls to this method are honored only if the
	 * <code>Display</code> is in the
	 * foreground.  This method MUST perform no action
	 * and return <CODE>false</CODE> if the
	 * <code>Display</code> is in the background.
	 *
	 * <p>The device MAY limit or override the duration. For devices
	 * that do not include a controllable backlight, calls to this
	 * method return <CODE>false</CODE>.
	 *
	 * @param duration the number of milliseconds the backlight should be
	 * flashed, or zero if the flashing should be stopped
	 *
	 * @return <CODE>true</CODE> if the backlight can be controlled
	 *           by the application and this display is in the foreground,
	 *          <CODE>false</CODE> otherwise
	 *
	 * @throws IllegalArgumentException if <code>duration</code> is negative
	 *
	 */
	public boolean flashBacklight(int duration) {

		// Grey area of Spec.:
		// The spec. does not specify when negative number
		// is passed into this function AND the Display is not
		// in the foreground. To be safe here, we always
		// check for the parameters first.
		if (duration < 0) {
			throw new IllegalArgumentException();
		}

		if (!hasForeground) {
			return false;
		}

		return deviceAccess.flashBacklight(displayId, duration);
	}

	/**
	 * Requests operation of the device's vibrator.  The vibrator is
	 * intended to be used to attract the user's attention or as a
	 * special effect for games.  The return value indicates if the
	 * vibrator can be controlled by the application.
	 *
	 * <p>This method switches on the vibrator for the requested
	 * duration, or switches it off if the requested duration is zero.
	 * If this method is called while the vibrator is still activated
	 * from a previous call, the request is interpreted as setting a
	 * new duration. It is not interpreted as adding additional time
	 * to the original request. This method returns immediately; that
	 * is, it must not block the caller while the vibrator is
	 * running. </p>
	 *
	 * <p>Calls to this method are honored only if the
	 * <code>Display</code> is in the foreground.  This method MUST
	 * perform no action and return <CODE>false</CODE> if the
	 * <code>Display</code> is in the background.</p>
	 *
	 * <p>The device MAY limit or override the duration.  For devices
	 * that do not include a controllable vibrator, calls to this
	 * method return <CODE>false</CODE>.</p>
	 *
	 * @param duration the number of milliseconds the vibrator should be run,
	 * or zero if the vibrator should be turned off
	 *
	 * @return <CODE>true</CODE> if the vibrator can be controlled by the
	 *           application and this display is in the foreground,
	 *          <CODE>false</CODE> otherwise
	 *
	 * @throws IllegalArgumentException if <code>duration</code> is negative
	 *
	 */
	public boolean vibrate(int duration) {

		// Grey area of Spec.:
		// The spec. does not specify when negative number
		// is passed into this function AND the Display is not
		// in the foreground. To be safe here, we always
		// check for the parameters first.
		if (duration < 0) {
			throw new IllegalArgumentException();
		}

		if (!hasForeground) {
			return false;
		}

		return vibrate0(displayId, duration);
	}

	/**
	 * Returns the best image width for a given image type.
	 * The image type must be one of
	 * {@link #LIST_ELEMENT},
	 * {@link #CHOICE_GROUP_ELEMENT}, or
	 * {@link #ALERT}.
	 * 
	 * @param imageType the image type
	 * @return the best image width for the image type, may be zero if
	 * there is no best size; must not be negative
	 * @throws IllegalArgumentException if <code>imageType</code> is illegal
	 */
	public int getBestImageWidth(int imageType) {
		switch (imageType) {
		case LIST_ELEMENT:
		case CHOICE_GROUP_ELEMENT:

			return ChoiceGroupSkin.getBestImageWidth();

		case ALERT:

			return WIDTH; // IMPL NOTE: this is wrong

		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns the best image height for a given image type.
	 * The image type must be one of
	 * {@link #LIST_ELEMENT},
	 * {@link #CHOICE_GROUP_ELEMENT}, or
	 * {@link #ALERT}.
	 * 
	 * @param imageType the image type
	 * @return the best image height for the image type, may be zero if
	 * there is no best size; must not be negative
	 * @throws IllegalArgumentException if <code>imageType</code> is illegal
	 */
	public int getBestImageHeight(int imageType) {
		switch (imageType) {
		case LIST_ELEMENT:
		case CHOICE_GROUP_ELEMENT:

			return ChoiceGroupSkin.getBestImageHeight();

		case ALERT:

			return HEIGHT; // IMPL NOTE: this is wrong!

		default:
			throw new IllegalArgumentException();
		}
	}

	/*
	 * ************* package private methods
	 */

	void lSetTitle(DisplayableLF d, String title) {
		if (isShown(d)) {
			// This call will not result in any call into application code
			window.setTitle(title);
		}
	}

	void lSetTicker(DisplayableLF d, Ticker ticker) {
		if (isShown(d)) {
			// This call will not result in any call into application code
			window.setTicker(ticker);
		}
	}

	void showPopup(PopupLayer popup) {
		// Add a new layer to the Chameleon layer stack. 
		// This call will not result in any call into application code
		window.addLayer(popup);
	}

	void hidePopup(PopupLayer popup) {
		// Remove a layer from the Chameleon layer stack. 
		// This call will not result in any call into application code
		window.removeLayer(popup);
	}

	/**
	 * Schedule to change to a new <code>Displayable</code> and notify the 
	 * display controller if the current state has changed.
	 * NOTE: This method handles the <code>setCurrent(null)</code> then
	 * <code>setCurrent(getCurrent())</code> case.
	 *
	 * @param nextDisplayable The next <code>Displayable</code> to display
	 * @param nextItem The next <code>Item</code> to display 
	 *                 inside nextDisplayable
	 */
	void setCurrentImpl(Displayable nextDisplayable, Item nextItem) {
		if (nextDisplayable == null) {
			if (wantsForeground != WANTS_BACKGROUND) {
				/*
				 * State change.
				 * Since we can't set the current to null we need some
				 * other state.
				 */
				wantsForeground = WANTS_BACKGROUND;

				/*
				 * When next displayable is null, we only request to lose
				 * foreground. *** No real screen change is needed. ***
				 * Just request AMS to put this display in the background.
				 */
				midletControllerEventProducer.sendDisplayBackgroundRequestEvent(displayId);
			}

			return;
		}

		if (nextDisplayable instanceof Alert) {
			if (wantsForeground != WANTS_FOREGROUND_FOR_ALERT) {
				// A non-alert or null was replaced by an alert
				wantsForeground = WANTS_FOREGROUND_FOR_ALERT;

				midletControllerEventProducer.sendDisplayForegroundRequestEvent(displayId, true);
			}
		} else {
			if (wantsForeground != WANTS_FOREGROUND) {
				// An alert or null was replaced by non-alert
				wantsForeground = WANTS_FOREGROUND;

				midletControllerEventProducer.sendDisplayForegroundRequestEvent(displayId, false);
			}
		}

		if (hasForeground) {
			// If a call to setCurrent() is coming in while in a
			// suspended state, notify the event handler to dismiss
			// the system screen before changing to the new screen
			if (paintSuspended) {

				paintSuspended = false;
			}
		}

		if (nextDisplayable instanceof Form) {
			((FormLF) nextDisplayable.displayableLF).uItemMakeVisible(nextItem);
		}

		requestScreenChange(nextDisplayable);
	}

	/**
	 * Called by Alert when a modal alert has exited.
	 *
	 * @param returnScreen The <code>Displayable</code> to return to after this
	 *                     <code>Alert</code> is cleared.
	 */
	void clearAlert(Displayable returnScreen) {
		requestScreenChange(returnScreen);
	}

	/**
	 * Posts an event to change the screen.
	 */
	void requestScreenChange(Displayable nextDisplayable) {
		pendingCurrent = nextDisplayable.displayableLF;
		displayEventProducer.sendScreenChangeEvent(consumer, nextDisplayable);
	}

	/**
	 * Called by <code>DisplayEventConsumer</code> when screen change event  
	 * needs to be processed.
	 * Registers a new <code>DisplayableLF</code> object to this 
	 * <code>Display</code>.  When this method returns, the 
	 * <code>DisplayableLF</code> object will be current on this 
	 * <code>Display</code>, replacing the previously current 
	 * <code>DisplayableLF</code> object (if any).  The current 
	 * <code>DisplayableLF</code> object is the one that is eligible to receive
	 * input events and that is eligible to be painted.
	 * <p>
	 * If necessary, <code>hideNotify()</code> is called on the previously
	 * current <code>DisplayableLF</code> object and <code>showNotify()</code> 
	 * is called on the newly current <code>DisplayableLF</code> object.
	 * <p>
	 *
	 * This method is used to initialize a <code>DisplayableLF</code> as
	 * a result of a SCREEN change timerEvent()
	 * <p>
	 *
	 * This method is quite complex, because it must conform to several
	 * different requirements imposed by the specification, the LCDUI thread
	 * safety model, and implementation considerations.  These requirements
	 * are as follows.
	 * 
	 * <p> 1. The state describing which <code>DisplayableLF</code> is current
	 * must be maintained consistently.  If <code>DisplayableLF</code> object 
	 * A is current on <code>Display</code> object D, these objects will point 
	 * to each other.
	 * Specifically, D.current&nbsp;==&nbsp;A and
	 * A.currentDisplay&nbsp;==&nbsp;D must be true.  In addition, no other
	 * <code>DisplayableLF</code> object will point to this 
	 * <code>Display</code> object.  If no <code>DisplayableLF</code> is 
	 * current, then D.current&nbsp;==&nbsp;null and there must be no 
	 * <code>DisplayableLF</code> object that points to D.  These conditions
	 * imply that it must never be true that D.current points to a
	 * <code>DisplayableLF</code> that doesn't point back to D, and it must 
	 * also never be true that there is a <code>DisplayableLF</code> object 
	 * that points to D when D.current is null or points to another 
	 * <code>DisplayableLF</code> object.
	 *
	 * <p> In order for state to be updated consistently, updates to the 
	 * current state must be performed while LCDUILock is held.
	 * 
	 * <p> 2. Consider the scenario where A and B are 
	 * <code>DisplayableLF</code> objects, and A is current and B is to become 
	 * current.  Because of the specification's requirements on 
	 * <code>showNotify()</code>, <code>hideNotify()</code>, and
	 * <code>isShown()</code>, A.hideNotify() must be called after A is no 
	 * longer current, and B.showNotify() must be called before B becomes 
	 * current.
	 *
	 * <p> 3. This method calls potentially calls out to the application via
	 * the <code>showNotify()</code>, <code>hideNotify()</code>, and 
	 * <code>paint()</code> methods. Therefore, according to the LCDUI
	 * threading model, this method must be called only from the event 
	 * dispatch thread.
	 *
	 * <p> 4. These callouts to the application must be performed while 
	 * LCDUILock is not held.
	 *
	 * <p> 5. The call to <code>hideNotify()</code> should occur before the
	 * call to <code>showNotify()</code>.  This allows the old current
	 * <code>DisplayableLF</code> to release its resources before the new
	 * current <code>DisplayableLF</code> allocates its resources.  
	 * If <code>hideNotify()</code> were called after <code>showNotify()</code>,
	 * this might potentially consume twice as many resources.
	 *
	 * <p> The above requirements force the implementation of this method to 
	 * be along the following lines:
	 * <pre>
	 * synchronized (LCDUILock) {
	 *     D.current = null;  A.currentDisplay = null;
	 * }
	 * A.hideNotify();
	 * B.showNotify();
	 * synchronized (LCDUILock) {
	 *     D.current = B;     B.currentDisplay = D;
	 * }
	 * B.paint();
	 * </pre>
	 *
	 * <p> NOTE: One issue that prevents the implementation to follow the logic
	 * order specified above, is that <code>showNotify()</code> on new screen
	 * requires a handle to the Display for things like
	 * <code>playAlertSound</code> and <code>updateVerticalScroll</code>.
	 *
	 * @param da The <code>Displayable</code> object whose LF to make current
	 */
	void callScreenChange(Displayable da) {

		// Assert (da != null)
		// since the screen change event will not be posted if da == null
		DisplayableLF newCurrent = da.displayableLF;

		// A copy of current LF object
		DisplayableLF oldCurrent;
		boolean hasForegroundCopy;

		synchronized (LCDUILock) {

			if (current == newCurrent) {
				return;
			}

			hasForegroundCopy = hasForeground;

			// Before calling hideNotify() and showNotify(),
			// we set no screen as 'current' so that isShown()
			// will always return false.
			oldCurrent = current;
			current = null;
			transitionCurrent = newCurrent;

			if (oldCurrent != null) {
				oldCurrent.lSetDisplay(null);
			}

			// Pre-grant the new current with access to this Display
			// because its uCallShow() needs to call functions like
			// Display.playAlertSound and setVerticallScroll(), etc.
			if (newCurrent != null) {
				newCurrent.lSetDisplay(this);
			}

		} // synchronized

		// Notify old screen to hide and release its resource
		// and notify new screen to allocate resource and show.
		//
		// SYNC NOTE: for Canvas and CustomItem, dsHide/Show() will
		// call into app functions like hide/showNotify().
		// So we call it outside LCDUILock.
		if (oldCurrent != null) {

			// Notify Chameleon that the current displayable is being hidden
			// This call will not result in any call into application code
			window.hideDisplayable(oldCurrent.lGetDisplayable());

			oldCurrent.uCallHide();
		}

		if (newCurrent != null) {
			if (hasForegroundCopy) {
				newCurrent.uCallShow();

				// We need to establish the new title and ticker so that
				// the new displayable can lay itself out properly in its
				// callShow() routine.
				// This call will not result in any call into application code
				window.showDisplayable(newCurrent.lGetDisplayable(), newCurrent.lGetDisplayable().getHeight());

			} else {
				newCurrent.uCallFreeze();
			}
		}

		synchronized (LCDUILock) {
			// Switch to new current screen
			current = newCurrent;
			transitionCurrent = null;

			if (!hasForegroundCopy) {
				return;
			}

			// update the command set after uCallShow() in which new screen
			// commands could be created and current Item could be set.
			updateCommandSet();

		} // synchronized

		// SYNC NOTE: The implementation of dsPaint() could call
		// out to MIDlet's paint() function. So we have to call it
		// outside LCDUILock. dsPaint will use LCDUILock to lock
		// around its internal handling.
		//
		// At this point, current screen has been changed.
		// newCurrent.isShown() is true.
		callPaint(0, 0, Display.WIDTH, Display.HEIGHT, null);

		if (Constants.MEASURE_STARTUP) {
			if (oldCurrent == null && current != null) {
				System.err.println("Startup Time: End at " + System.currentTimeMillis());
			}
		}

	}

	/** 
	 * Set the current vertical scroll position and proportion.
	 *
	 * @param scrollPosition vertical scroll position.
	 * @param scrollProportion vertical scroll proportion.
	 */
	void setVerticalScroll(int scrollPosition, int scrollProportion) {

		// Establish the scroll properties in Chameleon
		// This call will not result in any call into application code
		window.setVerticalScroll(scrollPosition, scrollProportion);

	}

	/**
	 * Play a sound.
	 *
	 * @param t type of alert
	 *
	 * @return <code>true</code>, if sound was played.
	 */
	boolean playAlertSound(AlertType t) {

		if (!paintSuspended && hasForeground) {
			// SYNC NOTE: playAlertSound is a native method, no locking
			// necessary
			return playAlertSound0(displayId, t.getType());
		}

		return false;
	}

	/** 
	 * Schedule a <code>Form</code> invalidation. 
	 */
	void invalidate() {
		displayEventProducer.sendInvalidateEvent(consumer);
	}

	/**
	 * Schedule the notificaton of an <code>ItemStateListener</code> due to
	 * a change in the given item.
	 * 
	 * NOTE: should be renamed to reflect that it schedules an event
	 *
	 * @param item The <code>Item</code> which has changed
	 */
	static void itemStateChanged(Item item) {
		displayEventProducer.sendItemStateChangeEvent(item);
	}

	/**
	 * Schedule a refresh of the size information for this 
	 * <code>CustomItem</code>.
	 *
	 * NOTE: should be renamed to reflect that it schedules an event
	 *
	 * @param item the <code>CustomItem</code> whose size information is to 
	 *             be refreshed
	 */
	static void itemSizeRefresh(CustomItem item) {
		displayEventProducer.sendItemSizeRefreshEvent(item);
	}

	/**
	 * Request a repaint for the given <code>Displayable</code>.  The rectangle 
	 * to be painted is in x,y,w,h.  If delay is greater than zero, the repaint
	 * may be deferred until that interval (in milliseconds) has elapsed.
	 *
	 * If the given <code>DisplayableLF</code> is not current, the request is 
	 * ignored.
	 * This is safe because whenever a <code>Displayable</code> becomes current,
	 * it gets a full repaint anyway.
	 *
	 * The target Object is optional. It will be packaged along with the
	 * repaint request and arrive later when the repaint is serviced in
	 * the dsPaint() routine - IF this paint request has not been
	 * coalesced with other repaints.
	 *
	 * @param d displayable object to be drawn
	 * @param x upper left corner x-coordinate
	 * @param y upper left corner y-coordinate
	 * @param w horizontal width
	 * @param h vertical height
	 * @param target an optional paint target
	 */
	void repaintImpl(DisplayableLF d, int x, int y, int w, int h, Object target) {

		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.repaintImpl():");
		
		synchronized (LCDUILock) {
			if (paintSuspended || !hasForeground || d != current) {
				return;
			}
		}

		// Chameleon redirects this repaint request to go through the
		// layer paint logic - marking the displayable layer and others
		// as needing a repaint, and then scheduling the event back 
		// through the chameleon display tunnel
		if (window != null) {
			// This call will not result in any call into application code        
			window.repaintDisplayable(x, y, w, h);
		}

	}

	// IMPL_NOTE: harden this up so non-current displayables can't
	// schedule repaints
	void scheduleRepaint() {
		// IMPL NOTE: The bounds sent to the event handler are irrelevent
		// in Chameleon
		repaintEventProducer.scheduleRepaint(consumer, 0, 0, 5, 5, null);
	}

	/**
	 * Process any pending repaint requests immediately.
	 *
	 * @param d The DisplayableLF which is requesting the repaint
	 *          (Used to determine if the <code>DisplayableLF</code> making the
	 *           request is currently being shown)
	 *
	 * SYNC NOTE: This method performs its own locking of LCDUILock.  
	 * Therefore, callers must not hold any locks when they call this method.
	 */
	void serviceRepaints(DisplayableLF d) {
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.serviceRepaints():" );
		synchronized (LCDUILock) {
			if (paintSuspended || !hasForeground || d != current) {
				return;
			}
		}
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.serviceRepaints(): displaying " + d.getClass());

		repaintEventProducer.serviceRepaints();
	}

	/**
	 * Process the specified repaint request immediately.
	 *
	 * @param x1 The x origin of the paint bounds
	 * @param y1 The y origin of the paint bounds
	 * @param x2 The x coordinate of the lower right bounds
	 * @param y2 The y coordinate of the lower right bounds
	 * @param target The optional paint target
	 *
	 * SYNC NOTE: This method performs its own locking of 
	 * LCDUILock and calloutLock.  Therefore, callers
	 * must not hold any locks when they call this method.
	 */
	void callPaint(int x1, int y1, int x2, int y2, Object target) {
		DisplayableLF currentCopy = null;

		synchronized (LCDUILock) {
			if (paintSuspended || !hasForeground || current == null) {
				return;
			}

			currentCopy = current;
		}

		if (window.setGraphicsForCanvas(screenGraphics)) {
			// If the window is not dirty and we are repainting it 
			// is likely the optimization whereby there is a canvas 
			// performing repaints and we can bypass chameleon 

			// Copy the clip now (before the application has a chance
			// to modify it in its paint method) for later use as the
			// dirty region for refresh0(). The getClip() method
			// returns the clip in translated coordinates, so we need
			// to untranslate it before passing to refresh0(), which
			// requires absolute coordinates.

			screenGraphics.getClip(region); /* x1, y1, x2, y2 */
			int tx = screenGraphics.getTranslateX();
			int ty = screenGraphics.getTranslateY();
			region[0] += tx;
			region[1] += ty;
			region[2] += tx;
			region[3] += ty;

			currentCopy.uCallPaint(screenGraphics, null);
			// SetFullScreenMode TCK test fails unless the following
			// translate is present
			screenGraphics.translate(-screenGraphics.getTranslateX(), -screenGraphics.getTranslateY());
			refresh0(displayId, region[0], region[1], region[2], region[3]);
		} else {
			// Chameleon redirects the paint call through its layer logic,
			// eventually resulting in a call back to the Chameleon Display
			// Tunnel, and possible into application code if the current
			// displayable is a canvas or gamecanvas.
			window.callPaint(screenGraphics, graphicsQ);

			// IMPL NOTE: This code block should really be a method inside CGraphics
			// with its own native call to the refresh function. Until that
			// can be refactored, we'll do it here.
			Object[] refreshQ = graphicsQ.getRefreshRegions();
			int[] subregion;
			for (int i = 0; i < refreshQ.length; i++) {
				subregion = (int[]) refreshQ[i]; /* x, y, w, h */
				if (CGraphicsQ.DEBUG) {
					System.err.println("Refresh(): " + subregion[0] + ", " + subregion[1] + ", " + subregion[2] + ", "
							+ subregion[3]);
				}
				// Be sure to convert the regions which are x,y,w,h to
				// x1, y1, x2, y2
				refresh0(displayId, subregion[0], subregion[1], subregion[0] + subregion[2], subregion[1]
						+ subregion[3]);
			}
		}

	}

	/**
	 * Changes Display dimensions.
	 * @param wantFullScnMode The new screen size mode. 
	 *        True if Display should give
	 *        all the space available to the content of the current 
	 *        Displayable (including command area and any other status bars);
	 *        False - otherwise
	 */
	void lSetFullScreen(boolean wantFullScnMode) {
		if (wantFullScnMode) {

			Display.WIDTH = Constants.CHAM_WIDTH;
			Display.HEIGHT = Constants.CHAM_FULLHEIGHT;

		} else {

			Display.WIDTH = Constants.CHAM_WIDTH;
			Display.HEIGHT = Constants.CHAM_HEIGHT;

		}
		screenGraphics.setDimensions(Display.WIDTH, Display.HEIGHT);

		// Notify Chameleon that the current displayable should be shown
		// in fullscreen mode.
		// This call will not result in any call into application code        
		window.setFullScreen(wantFullScnMode);

		setFullScreen0(displayId, wantFullScnMode);
	}

	/**
	 * Update the system's account of abstract commands.
	 *
	 * SYNC NOTE: Caller of this method should synchronize on the LCDUILock
	 */
	void updateCommandSet() {

		if (current == null) {
			// clear abstract commands in Chameleon
			// This call will not result in any call into application code        
			window.updateCommandSet(null, 0, null, null, 0, null);
			return;
		}

		Displayable d = current.lGetDisplayable();

		// Item commands
		Item curItem = (current instanceof FormLF) ? ((FormLF) current).lGetCurrentItem() : null;

		if (curItem == null) {
			// This call will not result in any call into application code        
			window.updateCommandSet(null, 0, null, d.commands, d.numCommands, d.listener);
		} else {
			// This call will not result in any call into application code        
			window.updateCommandSet(curItem.commands, curItem.numCommands, curItem.commandListener, d.commands,
					d.numCommands, d.listener);
		}

	}

	/**
	 * Is the current display visible.
	 *
	 * @param d <code>DisplaybleLF</code> instance to check, 
	 *          if current and visible.
	 *
	 * @return <code>true</code>, if the <code>Display</code> is visible and 
	 *         the object is current.
	 */
	boolean isShown(DisplayableLF d) {
		// SYNC NOTE: calls to isShown() should be synchronized on
		// the LCDUILock.

		return hasForeground && !paintSuspended && (current == d);
	}

	/**
	 * Create an ImmutableImage from the named system icon resource.
	 * The icons are stored in the configuration directory ($MIDP_HOME/lib).
	 * If the named resource can not be found a placeholder
	 * image is used instead.
	 * @param imageName name of the image
	 * @return a new ImmutableImage
	 * @throws IllegalArgumentException if imageName contains a "/" or "\\",
	 *        or imageName is null or imageName is empty
	 */
	static Image getSystemImage(String imageName) {

		byte[] imageData = ResourceHandler.getSystemResource(classSecurityToken, imageName);

		if (imageData != null) {
			return Image.createImage(imageData, 0, imageData.length);
		} else {
			// Use a empty immutable image as placeholder
			return Image.createImage(Image.createImage(16, 16));
		}
	}

	/**
	 * This is a utility method used to handle any Throwables
	 * caught while calling application code. The default
	 * implementation will simply call Logging.trace on 
	 * the <code>Throwable</code>. Note, the parameter must be non-null or
	 * this method will generate a <code>NullPointerException</code>.
	 *
	 * @param t The <code>Throwable</code> caught during the call into
	 *          application code.
	 */
	static void handleThrowable(Throwable t) {
		if (Logging.TRACE_ENABLED) {
			Logging.trace(t, "Exception caught in Display class");
		}
	}

	/*
	 * ************* private methods
	 */
	/**
	 * Resets native resources on a foreground change
	 *
	 * @param displayId The display ID associated with this Display
	 */
	private native void gainedForeground0(int displayId);

	/*
	 * ************* private methods
	 */

	/**
	 * Control the drawing of the trusted <code>MIDlet<code> icon in native.
	 *
	 * @param displayId The display ID associated with this Display
	 * @param drawTrusted <code>true</code> if the icon should be drawn, 
	 *                    <code>false</code> if it should not.
	 */
	private void drawTrustedIcon0(int displayId, boolean drawTrusted) {
    	// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.drawTrustedIcon0: not yet implemented");
    }

	/**
	 * Run the serially repainted operations.
	 */
	private void getCallSerially() {
		java.util.Vector q = null;

		synchronized (LCDUILock) {
			q = currentQueue;
			currentQueue = (q == queue1) ? queue2 : queue1;
		}

		// SYNC NOTE: we synch on calloutLock for the call into
		// application code
		synchronized (calloutLock) {
			for (int i = 0; i < q.size(); i++) {
				try {
					Runnable r = (Runnable) q.elementAt(i);
					r.run();
				} catch (Throwable thr) {
					handleThrowable(thr);
				}
			}
		}

		q.removeAllElements();
	}

	/**
	 * Show about box as a preempting alert.
	 */
	private void showAboutBox() {
		Image logo = getSystemImage("about.png");

		// Shows About Box as a preempting Alert
		SystemAlert about = new SystemAlert(displayEventHandlerImpl, aboutCommand.getLabel(), Resource
				.getString(ResourceConstants.AMS_PROJECT_NAME)
				+ "\n\n" + Resource.getString(ResourceConstants.AMS_COPYRIGHT_LONG), logo, null);
	}

	/**
	 * Get a handle to the Chameleon MIDPWindow object doing
	 * the rendering
	 *
	 * @return the MIDPWindow doing the rendering
	 */
	MIDPWindow getWindow() {
		return window;
	}

	/**
	 * Redraw a portion of the display.
	 *
	 * @param displayId The display ID associated with this Display
	 * @param x1 upper left corner x-coordinate
	 * @param y1 upper left corner y-coordinate
	 * @param x2 lower right corner x-coordinate
	 * @param y2 lower right corner y-coordinat
	 */
	private void refresh0(int displayId, int x1, int y1, int x2, int y2) {
		
		SDLToolkit.getToolkit().refresh(displayId, x1, y1, x2- x1, y2 - y1);
		// TODO
		//System.out.println("[DEBUG] Display.refresh0(): not implemented yet : x1=" + x1 + " y1=" + y1 + " x2=" + x2 + " y2= "+ y2);
	}

	/**
	 * Play an Alert sound.
	 *
	 * @param displayId The display ID associated with this Display
	 * @param alertType type of alert
	 *
	 * @return <code>true</code>, if sound was played.
	 */
	private boolean playAlertSound0(int displayId, int alertType) {
		// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.playAlertSound0(): not implemented yet");
		return false;
	}

	/**
	 * Plays the vibration if this Display is current and in foreground
	 * and device has vibration capability.
	 *
	 * @param displayId The display ID of this Display
	 * @param dur the duration in milli seconds
	 *
	 * @return if it's successful, return <code>true</code>,
	 *         othewise return <code>false</code>
	 *
	 */
	private boolean vibrate0(int displayId, int dur) {
		// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.vibrate0(): not implemented yet");
		return false;
	}

	/**
	 * Sets full screen on the device.
	 * @param mode The new screen size mode to be set. True if all
	 *             available space should be given to content; false -
	 *             some areas can be reserved for ticker, title, commands.
	 */
	private void setFullScreen0(int displayId, boolean mode) {
			// TODO
		if (Logging.TRACE_ENABLED)
			System.out.println("[DEBUG] Display.setFullScreen0(): not implemented yet");
	}

	/**
	 * ************* Inner Class, for Chameleon access
	 */
	class ChameleonTunnel implements ChamDisplayTunnel {

		/**
		 * Called from Chameleon to paint the current Displayable.
		 * This may call into application code if the current displayable
		 * is a canvas or canvas subclass.
		 *
		 * @param g the graphics context to paint with
		 */
		public void callPaint(Graphics g) {
			DisplayableLF currentCopy;

			synchronized (Display.LCDUILock) {
				currentCopy = current;
			}

			if (currentCopy != null) {
				currentCopy.uCallPaint(g, null);
			}
		}

		/**
		 * Called from Chameleon to schedule a paint event with the
		 * event scheduler
		 */
		public void scheduleRepaint() {
			Display.this.scheduleRepaint();
		}

		/**
		 * Called from Chameleon to notify a listener of an item
		 * command selection. This will almost certainly call into
		 * application code.
		 *
		 * @param cmd the Command which was selected
		 * @param listener the ItemCommandListener to notify
		 */
		public void callItemListener(Command cmd, ItemCommandListener listener) {
			Item curItem = null;

			try {
				synchronized (LCDUILock) {
					curItem = ((FormLF) current).lGetCurrentItem();

					if (curItem == null) {
						return;
					}
				}

				// SYNC NOTE: We release the lock on LCDUILock and acquire
				// calloutLock before calling into application code
				synchronized (calloutLock) {
					listener.commandAction(cmd, curItem);
				}

			} catch (Throwable thr) {
				handleThrowable(thr);
			}
		}

		/**
		 * Called from Chameleon to notify a listener of a screen
		 * command selection. This will almost certainly call into
		 * application code.
		 *
		 * @param cmd the Command which was selected
		 * @param listener the CommandListener to notify
		 */
		public void callScreenListener(Command cmd, CommandListener listener) {
			Displayable currentDisplayable = null;

			synchronized (LCDUILock) {
				currentDisplayable = current.lGetDisplayable();
			}

			try {
				// SYNC NOTE: We release the lock on LCDUILock and acquire
				// calloutLock before calling into application code
				synchronized (calloutLock) {
					listener.commandAction(cmd, currentDisplayable);
				}
			} catch (Throwable thr) {
				handleThrowable(thr);
			}
		}

		/**
		 * This method is used by Chameleon to invoke 
		 * Displayable.sizeChanged() method. This may call into 
		 * application code if the current displayable
		 * is a canvas or canvas subclass.
		 *
		 * @param w the new width
		 * @param h the new height
		 */
		public void callSizeChanged(int w, int h) {
			DisplayableLF currentCopy = Display.this.current;
			if (currentCopy != null) {
				currentCopy.uCallSizeChanged(w, h);
			}
		}
	}

	/**
	 * ************* Inner Class, DisplayAccesImpl
	 */

	/** This is nested inside Display so that it can see the private fields. */
	class DisplayAccessImpl implements DisplayAccess {

		/**
		 * get a hnadle to the current display
		 * DisplayAccess I/F method.
		 *
		 * @return current Display handle.
		 */
		public Display getDisplay() {
			// return display;
			return Display.this;
		}

		/**
		 * Called to get key mask of all the keys that were pressed.
		 * DisplayAccess I/F method.
		 *
		 * @return keyMask  The key mask of all the keys that were pressed.
		 */
		public int getKeyMask() {
			DisplayableLF currentCopy = current;

			if (currentCopy instanceof CanvasLF) {
				return ((CanvasLF) currentCopy).uGetKeyMask();
			} else {
				return 0;
			}
		}

		/**
		 * Flushes the entire off-screen buffer to the display.
		 * DisplayAccess I/F method.
		 * 
		 * @param d The Displayable 
		 * @param offscreen_buffer The image buffer 
		 * @param x The left edge of the region to be flushed
		 * @param y The top edge of the region to be flushed
		 * @param width The width of the region to be flushed
		 * @param height The height of the region to be flushed
		 */
		public void flush(Displayable d, Image offscreen_buffer, int x, int y, int width, int height) {

			synchronized (LCDUILock) {

				if (paintSuspended || !hasForeground || d.displayableLF != current
						|| ((Display.this.window).systemMenuUp())) {
					return;
				}

				int x2 = x + width;
				int y2 = y + height;

				screenGraphics.reset(x, y, x2, y2);

				// Draw the off-screen buffer into the area that takes the title
				// and ticker bar heights into account.
				screenGraphics.drawImage(offscreen_buffer, (Display.this.window).getBodyAnchorX(),
						(Display.this.window).getBodyAnchorY(), Graphics.TOP | Graphics.LEFT);

				refresh0(displayId, x, y, x2, y2);
			}
		}

		/**
		 * Set the trusted icon for this Display. When ever this display is in
		 * the foreground the given icon will be displayed in the area
		 * reserved 
		 * for the trusted icon. Only callers with the internal MIDP 
		 * permission can use this method.
		 * DisplayAccess I/F method.
		 *
		 * @param token security token of the call that has internal MIDP
		 *              permission
		 * @param drawTrusted true to draw the trusted icon
		 */
		public void setTrustedIcon(SecurityToken token, boolean drawTrusted) {

			token.checkIfPermissionAllowed(Permissions.MIDP);

			drawTrustedIcon0(displayId, drawTrusted);
		}

		/**
		 * Get the ID of this display.
		 * DisplayAccess I/F method.
		 *
		 * @return Display ID
		 */
		public int getDisplayId() {
			return Display.this.displayId;
		}

		/**
		 * Sets the ID of this display.
		 * Shall be called only from DisplayContainer.addDisplay() during 
		 * Display construction and registration in the container.
		 * DisplayAccess I/F method.
		 *
		 * @param newId new ID for Display associated with this DisplayAccess
		 */
		public void setDisplayId(int newId) {
			Display.this.displayId = newId;
		}

		/**
		 * Get the MIDletEventConsumer associated with this display.
		 * DisplayAccess I/F method.
		 *
		 * @return Consumer of midlet events that go through this display
		 */
		public MIDletEventConsumer getMIDletEventConsumer() {
			return Display.this.midletEventConsumer;
		}

		/**
		 * Get the DisplayEventConsumer associated with this display.
		 * DisplayAccess I/F method.
		 *
		 * @return Consumer of midlet events that go through this display
		 */
		public DisplayEventConsumer getDisplayEventConsumer() {
			return Display.this.consumer;
		}
	} // END DisplayAccessImpl Class

	/** This is nested inside Display so that it can see the private fields. */
	class DisplayEventConsumerImpl implements DisplayEventConsumer {

		/**
		 * Suspend any further painting of current displayable.
		 *
		 * SYNC NOTE: This function should only be called from
		 * Event Dispatch thread.
		 */
		private void suspendPainting() {
			DisplayableLF currentCopy;

			synchronized (Display.LCDUILock) {

				paintSuspended = true;

				if (current == null) {
					return;
				}

				currentCopy = current;
			}

			// SYNC NOTE: The implementation of dsFreeze() could call
			// app's hideNotify(). So do this outside LCDUILock.
			currentCopy.uCallFreeze();

			// We want to re-set the scroll indicators when we suspend so
			// that the overtaking screen doesn't have to do it
			setVerticalScroll(0, 100);
		}

		/**
		 * Resume paininting , if doing repaint operations.
		 *
		 * SYNC NOTE: This function should only be called from
		 * Event Dispatch thread.
		 */
		private void resumePainting() {
			DisplayableLF currentCopy = current;

			// At this point, current.isShown() returns false.
			// SYNC NOTE: unfreeze could call into app's showNotify().
			// So do it outside the LCDUILock.
			if (currentCopy != null) {
				currentCopy.uCallShow();
			}

			synchronized (LCDUILock) {

				paintSuspended = false;

				// Command set could have been changed during suspending and
				// previous showNotify() and paint()
				updateCommandSet();

			} // synchronized

			// At this point, current.isShown() is true if current != null.
			//
			// SYNC NOTE: could call into app's paint().
			// So do it outside the LCDUILock
			callPaint(0, 0, Display.WIDTH, Display.HEIGHT, null);
		}

		/**
		 * Called from the event delivery loop when a key event is seen.
		 * DisplayEventConsumer I/F method.
		 *
		 * @param keyType kind of key event - pressed, release, repeated, typed
		 * @param keyCode key code of entered key
		 */
		public void handleKeyEvent(int keyType, int keyCode) {
			// SYNC NOTE: assignment is atomic.
			// Since we may call into application code,
			// we do so outside of LCDUILock
			DisplayableLF currentCopy = current;

			// Chameleon is given first priority in processing the
			// key event. If the return value is false, the event
			// is then sent on to the current displayable
			// This call will not result in any call into application code            
			if (!window.keyInput(keyType, keyCode) && currentCopy != null) {
				currentCopy.uCallKeyEvent(keyType, keyCode);
			}

		} // handleKeyEvent()

		/**
		 * Called from the event delivery loop when an input method
		 * event is seen.
		 * DisplayEventConsumer I/F method.
		 * 
		 * @param inputText input text string
		 */
		public void handleInputMethodEvent(String inputText) {
			TextBox textBoxCopy = null;

			synchronized (LCDUILock) {
				if (current instanceof TextBox) {
					textBoxCopy = (TextBox) current;
				}
			}

			// SYNC NOTE: TextBox.insert() does its own locking so we
			// move the call outside of our lock using a local variable
			if (textBoxCopy != null) {
				textBoxCopy.insert(inputText, textBoxCopy.getCaretPosition());
			}
		} // handleInputMethodEvent

		/**
		 * Called from the event delivery loop when a pointer event is seen.
		 * DisplayEventConsumer I/F method.
		 *
		 * @param pointerType kind of pointer event
		 * @param x x-coordinate of pointer event
		 * @param y y-coordinate of pointer event
		 */
		public void handlePointerEvent(int pointerType, int x, int y) {
			// SYNC NOTE: assignment is atomic.
			DisplayableLF currentCopy = current;

			// Chameleon is given first priority in processing the
			// pointer event. If the return value is false, the event
			// is then sent on to the current displayable
			// This call will not result in any call into application code            
			if (!window.pointerInput(pointerType, x, y) && currentCopy != null) {
				// We carefully translate the pointer events coordinates
				// into the space of the body layer before calling into
				// application code
				currentCopy.uCallPointerEvent(pointerType, x - window.getBodyAnchorX(), y - window.getBodyAnchorY());
			}

		} // handlePointerEvent()

		/**
		 * Called from the event delivery system when a command is seen.
		 * The id parameter is an index into the list of Commands that are
		 * current, i.e. those associated with the visible Screen.
		 * DisplayEventConsumer I/F method.
		 *
		 * TBD: param screenId Id of the command target (Displayable) 
		 * @param cmdId The id of the command for listener notification
		 *           (as is returned by Command.getID())
		 */
		public void handleCommandEvent( /* int screenId, */int cmdId) {

			// NOT USED BY CHAMELEON
			// Chameleon handles commands on the event thread as caused
			// by a key press or pen tap on the soft button layer

		} // handleCommandEvent

		/**
		 * Called by event delivery to notify an ItemLF in current DisplayableLF
		 * of a change in its native peer state.
		 * DisplayEventConsumer I/F method.
		 *
		 * @param modelVersion the version of the peer's data model
		 * @param itemPeerId the id of the ItemLF's peer whose state has changed
		 * @param hint some value that is interpretted only between the peers
		 */
		public void handlePeerStateChangeEvent(int modelVersion, int itemPeerId, int hint) {
			DisplayableLF currentCopy = Display.this.current;

			if (currentCopy instanceof FormLF) {
				((FormLF) currentCopy).uCallPeerStateChanged(modelVersion, itemPeerId, hint);
			}
		}

		/**
		 * Called by event delivery when a repaint should occur.
		 * DisplayEventConsumer I/F method.
		 *
		 * @param x1 The origin x coordinate of the repaint region
		 * @param y1 The origin y coordinate of the repaint region
		 * @param x2 The bounding x coordinate of the repaint region
		 * @param y2 The bounding y coordinate of the repaint region
		 * @param target The optional paint target
		 */
		public void handleRepaintEvent(int x1, int y1, int x2, int y2, Object target) {
			// Its ok to ignore the foreground/paintSuspended state from here.
			// repaint() will not succeed if the foreground/paintSuspended
			// status has changed in the meantime.
			Display.this.callPaint(x1, y1, x2, y2, target);
		}

		/**
		 * Called by event delivery when a screen change needs to occur.
		 * DisplayEventConsumer I/F method.
		 *
		 * @param screen The Displayable to make current in the Display
		 */
		public void handleScreenChangeEvent(Displayable screen) {
			Display.this.callScreenChange(screen);
		}

		/** 
		 * Called by event delivery to process a Form invalidation.
		 * DisplayEventConsumer I/F method.
		 */
		public void handleInvalidateEvent() {
			DisplayableLF currentCopy = Display.this.current;

			if (currentCopy != null) {
				currentCopy.uCallInvalidate();
			}
		}

		/**
		 * Called by event delivery to batch process 
		 * all pending serial callbacks. 
		 * DisplayEventConsumer I/F method.
		 */
		public void handleCallSeriallyEvent() {
			// This line is no longer necessary. The event handler will
			// already have processed any pending repaints before processing
			// the callSerially() event.
			// repaintEventProducer.serviceRepaints();
			getCallSerially();
		}

		/**
		 * Called by event delivery when MIDlet Controller (in AMS Isolate) 
		 * notifies MIdlet and its display that there is a change in its foreground
		 * status (it has been moved to foreground or background).
		 * DisplayEventConsumer I/F method.
		 *
		 * This should not be called while
		 * holding the LCDUILock.
		 *
		 * TBD: split into 2 methods: for foreground & background
		 *
		 * @param foreground A flag indicating the new foreground status.
		 *                   true if the Display should be put in
		 *                   the foreground, 
		 *                   false - in the background.
		 */
		public void handleDisplayForegroundNotifyEvent(boolean foreground) {
			
			// A copy of current LF object
			DisplayableLF currentCopy = current;

			synchronized (LCDUILock) {
				if (hasForeground == foreground) {
					return;
				}

				if (current == null) {
					/*
					 * Unlike normal displayable switching, when switching to
					 * the headless displayable, don't set wantsForeground to
					 * true. Instead, force it to be current.
					 * IMPL NOTE: this probably leaves the headless 
					 * displayable in the incorrect state.
					 */
					current = getHeadlessDisplayable().displayableLF;
					current.lSetDisplay(Display.this);
				}
				currentCopy = current;
			}

			// SYNC NOTE: for Canvas and CustomItem, dsHide/Show() will
			// call into app functions like hide/showNotify().
			// So we call it outside LCDUILock.
			if (foreground) {
				// Notify current to allocate resource and show.
				// NOTE: currentCopy should never be null. If the current
				// midlet has not set a displayable, it should be set to 
				// the headless midlet alert
				if (currentCopy != null) {
					currentCopy.uCallShow();

					// Notify Chameleon that this displayable is being shown.
					// This call will not result in any call into 
					// application code                                
					window.showDisplayable(currentCopy.lGetDisplayable(), currentCopy.lGetDisplayable().getHeight());

				}

				synchronized (LCDUILock) {

					// Update hasForeground after uCallShow to allow
					// isShown() to return false unitl now
					hasForeground = true;

					gainedForeground0(displayId);

					// update the command set after uCallShow() 
					// in which new screen commands could be created and 
					// current Item could be set.
					updateCommandSet();

					// reset the vibrator and blacklight
					vibrate0(displayId, 0);
					deviceAccess.flashBacklight(displayId, 0);

					// draw correct trusted icon
					MIDletStateHandler msHandler = MIDletStateHandler.getMidletStateHandler();
					MIDletSuite midletSuite = msHandler.getMIDletSuite();
					if (midletSuite != null) {
						drawTrustedIcon0(displayId, midletSuite.isTrusted());
					} else {
						drawTrustedIcon0(displayId, false);
					}

					// uCallShow does the rest of the settings:
					// scroll indicators, 
					// correct screen mode (full or normal)

				} // synchronized

				// SYNC NOTE: The implementation of dsPaint() could call
				// out to MIDlet's paint() function. So we have to call it
				// outside LCDUILock. dsPaint will use LCDUILock to lock
				// around its internal handling.
				callPaint(0, 0, Display.WIDTH, Display.HEIGHT, null);

				if (Constants.MEASURE_STARTUP) {
					if (current != null) {
						System.err.println("Startup Time: End at " + System.currentTimeMillis());
					}
				}
			} else {
				synchronized (Display.LCDUILock) {
					// Update hasForeground after uCallFreeze to allow
					// isShown() to return false unitl now
					hasForeground = false;
				}

				// Notify current screen to freeze its resource
				if (currentCopy != null) {

					// Notify Chameleon that the current displayable is being hidden
					// This call will not result in any call into application code
					window.hideDisplayable(currentCopy.lGetDisplayable());

					currentCopy.uCallFreeze();
				}
			}
		}
	} // END DisplayEventConsumerImpl Class

	/**
	 * Returns a Displayable to be shown for a midlet is brought 
	 * into foreground but that midlet does not set displayable to 
	 * a non-null object.
	 *
	 * @return displayable object to be displayed for a headless midlet
	 */
	private Displayable getHeadlessDisplayable() {
		return new HeadlessAlert();
	}

	/**
	 * Returns a boolean indicating whether the given displayableLF is the LF
	 * for the special headless Displayable.
	 */
	private boolean isHeadless(DisplayableLF dlf) {
		return (dlf.lGetDisplayable() instanceof HeadlessAlert);
	}

	// ************** inner class Headless Alert *****************

	/**
	 * HeadlessAlert is shown when a midlet is brought in foreground and
	 * that midlet did not setCurrent to a non-null Displayable.
	 */
	class HeadlessAlert extends Alert {

		HeadlessAlert() {
			super(null, Resource.getString(ResourceConstants.LCDUI_DISPLAY_HEADLESS), null, AlertType.INFO);
			setTimeout(Alert.FOREVER);
			addCommand(exitCommand);

			addCommand(okCommand);

			setCommandListener(new CommandListener() {
				public void commandAction(Command c, Displayable d) {
					if (c == exitCommand) {
						/**
						 * We need to destroy current midlet - 
						 * the owner of this alert: 
						 * to generate MIDLET_DESTROY_REQUEST
						 * event for a given midlet to MIDlet Controller
						 * in AMS Isolate.
						 */
						Display.this.midletControllerEventProducer
								.sendMIDletDestroyRequestEvent(Display.this.displayId);

					}

					else if (c == okCommand) {
						Display.this.midletControllerEventProducer
								.sendDisplayBackgroundRequestEvent(Display.this.displayId);
					}

				}
			});
		}
	}

	// *************** tracing support ***************

}

/**
 * ************* Class for Chameleon Image access
 */
class ChamImageTunnel implements com.sun.midp.chameleon.skins.resources.ImageTunnel {

//	public Image getRomizedImage(int imageDataArrayPtr, int imageDataArrayLength) {
//		return Image.getRomizedImage(imageDataArrayPtr, imageDataArrayLength);
//	}

	public Image getRomizedImage(String imageIdentifier) {
		try {
			return Image.createImage("/com/sun/midp/chameleon/skins/resources/images/" + imageIdentifier + ".png");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//return null;
	}
}
