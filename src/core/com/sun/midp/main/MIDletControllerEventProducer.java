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

package com.sun.midp.main;

import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;


/**
 * This class provides methods to send events of types 
 * handled by MIDletControllerEventConsumer I/F implementors. 
 * This class completely hide event construction & sending in its methods.
 *
 * This class is intended to be used by MIDletStateHandler & MIDletPeer 
 * classes in Allication Isolate. 
 * So in some of its sendXXXEvent()methods we can change int IDs to 
 * MIDletPeer references. 
 * 
 * Generic comments for all XXXEventProducers:
 *
 * For each supported event type there is a separate sendXXXEvent() method, 
 * that gets all needed parameters to construct an event of an approprate class.
 * The method also performs event sending itself.
 *
 * If a given event type merges a set of logically different subtypes, 
 * this class shall provide separate methods for these subtypes.
 *
 * It is assumed that only one object instance of this class (per isolate)
 * is created at (isolate) startup. 
 * All MIDP runtime subsystems that need to send events of supported types, 
 * must get a reference to an already created istance of this class. 
 * Typically, this instance should be passed as a constructor parameter.
 *
 * For security reasons constructor is not public. 
 * Use createXXXProducer(...) method, 
 * protected by security, to create and object instance of this class
 * from a different package.
 *
 * Class is NOT final to allow debug/profile/test/automation subsystems
 * to change, substitute, complement default "event sending" functionality :
 * Ex. 
 * class LogXXXEventProducer 
 *      extends XXXEventProducer {
 *  ...
 *  void sendXXXEvent(parameters) {
 *      LOG("Event of type XXX is about to be sent ...")
 *      super.sendXXXEvent(parameters);
 *      LOG("Event of type XXX has been sent successfully !")
 *  }
 *  ...
 * }
 */
public class MIDletControllerEventProducer {
    
    /** Cached reference to the MIDP event queue. */
    protected EventQueue eventQueue;
    /** Cached reference to AMS isolate ID. */
    protected int amsIsolateId; 
    /** Cached reference to current isolate ID. */
    protected int currentIsolateId; 

    /** Preallocate start error event to work in case of out of memory */
    NativeEvent startErrorEvent;
    /** Preallocate MIDlet created event to work in case of out of memory */
    NativeEvent midletCreatedEvent;
    /** Preallocate MIDlet active event to work in case of out of memory */
    NativeEvent midletActiveEvent;
    /** Preallocate MIDlet paused event to work in case of out of memory */
    NativeEvent midletPausedEvent;
    /** Preallocate MIDlet destroyed event to work in case of out of memory */
    NativeEvent midletDestroyedEvent;

    /**
     * Construct a new MIDletControllerEventProducer.
     *
     * @param  token security token that controls instance creation.
     * @param  theEventQueue An event queue where new events will be posted.
     * @param  theAmsIsolateId AMS Isolate Id
     * @param  theCurrentIsolateId Current Isolate Id
     */
    public MIDletControllerEventProducer(
        SecurityToken token,
        EventQueue theEventQueue, 
        int theAmsIsolateId, 
        int theCurrentIsolateId) {
            
        token.checkIfPermissionAllowed(Permissions.MIDP);
        eventQueue = theEventQueue;
        amsIsolateId = theAmsIsolateId;
        currentIsolateId = theCurrentIsolateId;

        /* Cache all of the notification events. */
        startErrorEvent =
            new NativeEvent(EventTypes.MIDLET_START_ERROR_EVENT);
        midletCreatedEvent = 
            new NativeEvent(EventTypes.MIDLET_CREATED_NOTIFICATION);
        midletActiveEvent = 
            new NativeEvent(EventTypes.MIDLET_ACTIVE_NOTIFICATION);
        midletPausedEvent = 
            new NativeEvent(EventTypes.MIDLET_PAUSED_NOTIFICATION);
        midletDestroyedEvent =
            new NativeEvent(EventTypes.MIDLET_DESTROYED_NOTIFICATION);
    }

    /*
     * MIDlet Startup Events:
     *
     * MIDLET_START_ERROR
     * MIDLET_CREATED_NOTIFICATION
     */    
    /**
     * Notifies AMS that MIDlet creation failed
     * NEW: earlier it has been explicitely generated by 
     * void static AppIsolateMIDletSuiteLoader.main(...)
     *
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param error start error code
     */
    public void sendMIDletStartErrorEvent(
        int midletExternalAppId,
        String midletSuiteId, 
        String midletClassName,
        int error) {

        synchronized (startErrorEvent) {
            // use pre-created event to work in case of hadling out of memory
            startErrorEvent.intParam1 = midletExternalAppId;
            startErrorEvent.stringParam1 = midletSuiteId;
            startErrorEvent.stringParam2 = midletClassName;
            startErrorEvent.intParam2 = error;

            eventQueue.sendNativeEventToIsolate(startErrorEvent, amsIsolateId);
        }
    }
    /**
     * Called to send a MIDlet created notification to the AMS isolate.
     *
     * @param midletExternalAppId ID of given by an external application
     *                            manager
     * @param midletDisplayId ID of the sending Display
     * @param midletSuiteId ID of the MIDlet suite
     * @param midletClassName Class name of the MIDlet
     * @param midletDisplayName name to show the user
     */
    public void sendMIDletCreateNotifyEvent(
        int midletExternalAppId,
        int midletDisplayId, 
        String midletSuiteId,
        String midletClassName, 
        String midletDisplayName) {

        synchronized (midletCreatedEvent) {
            midletCreatedEvent.intParam1 = currentIsolateId;
            midletCreatedEvent.intParam2 = midletExternalAppId;
            midletCreatedEvent.intParam4 = midletDisplayId;
        
            midletCreatedEvent.stringParam1 = midletSuiteId;
            midletCreatedEvent.stringParam2 = midletClassName;
            midletCreatedEvent.stringParam3 = midletDisplayName;

            eventQueue.sendNativeEventToIsolate(midletCreatedEvent,
                                                amsIsolateId);
        }
    }
    
    /*
     * MIDlet State Management (Lifecycle) Events:
     *
     * MIDLET_ACTIVE_NOTIFICATION
     * MIDLET_PAUSE_NOTIFICATION
     * MIDLET_DESTROY_NOTIFICATION
     *
     * MIDLET_DESTROY_REQUEST
     *
     * ACTIVATE_ALL - produced by native code
     * PAUSE_ALL -produced by native code
     * SHUTDOWN/DESTROY_ALL - produced by native code
     *
     * FATAL_ERROR_NOTIFICATION - produced by native code
     *
     */    
    /**
     * Called to send a MIDlet active notification to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletActiveNotifyEvent(int midletDisplayId) {
        synchronized (midletActiveEvent) {
            midletActiveEvent.intParam1 = currentIsolateId;
            midletActiveEvent.intParam4 = midletDisplayId;
        
            eventQueue.sendNativeEventToIsolate(midletActiveEvent,
                                                amsIsolateId);
        }
    }
    /**
     * Called to send a MIDlet paused notification to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletPauseNotifyEvent(int midletDisplayId) {
        synchronized (midletPausedEvent) {
            midletPausedEvent.intParam1 = currentIsolateId;
            midletPausedEvent.intParam4 = midletDisplayId;

            eventQueue.sendNativeEventToIsolate(midletPausedEvent,
                                                amsIsolateId);
        }
    }
    /**
     * Called to send a MIDlet destroyed notification to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletDestroyNotifyEvent(int midletDisplayId) {
        synchronized (midletDestroyedEvent) {
            midletDestroyedEvent.intParam1 = currentIsolateId;
            midletDestroyedEvent.intParam4 = midletDisplayId;

            eventQueue.sendNativeEventToIsolate(midletDestroyedEvent,
                                                amsIsolateId);
        }
    }
    /**
     * Called to send a MIDlet destroy request to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletDestroyRequestEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.MIDLET_DESTROY_REQUEST_EVENT);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
        
    /*
     * Foreground MIDlet Management Events:
     *
     * SELECT_FOREGROUND - produced by native code
     * FOREGROUND_TRANSFER
     * SET_FOREGROUND_BY_NAME_REQUEST
     *
     */    
    /**
     * Called to send a foreground MIDlet transfer event to the AMS isolate.
     * Former: NEW method, originally sent from CHAPI
     *
     * @param originMIDletSuiteId ID of MIDlet from which 
     *        to take forefround ownership away, 
     * @param originMIDletClassName Name of MIDlet from which 
     *        to take forefround ownership away
     * @param targetMIDletSuiteId ID of MIDlet 
     *        to give forefround ownership to, 
     * @param targetMIDletClassName Name of MIDlet 
     *        to give forefround ownership to
     */
    public void sendMIDletForegroundTransferEvent(
        String originMIDletSuiteId,
        String originMIDletClassName,
        String targetMIDletSuiteId,
        String targetMIDletClassName) {
        NativeEvent event = 
            new NativeEvent(EventTypes.FOREGROUND_TRANSFER_EVENT);

        event.stringParam1 = originMIDletSuiteId;
        event.stringParam2 = originMIDletClassName;
        event.stringParam3 = targetMIDletSuiteId;
        event.stringParam4 = targetMIDletClassName;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    /**
     * Called to send a request to AMS isolate for a MIDlet be in
     * the foreground.
     *
     * @param suiteId MIDlet's suite ID
     * @param className MIDlet's class name
     */
    public void sendSetForegroundByNameRequestEvent(String suiteId, 
            String className) {

        NativeEvent event =
            new NativeEvent(EventTypes.SET_FOREGROUND_BY_NAME_REQUEST);

        event.stringParam1 = suiteId;
        event.stringParam2 = className;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    
    
    /*
     * Foreground Display Management Events:
     *
     * FOREGROUND_REQUEST
     * BACKGROUND_REQUEST
     *
     */    
    /**
     * Called to send a foreground request event to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     * @param isAlert true if the current displayable is an Alert
     */
    public void sendDisplayForegroundRequestEvent(int midletDisplayId,
            boolean isAlert) {
        NativeEvent event =
            new NativeEvent(EventTypes.FOREGROUND_REQUEST_EVENT);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;

        if (isAlert) {
            event.intParam2 = 1;
        } else {
            event.intParam2 = 0;
        }

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    /**
     * Called to send a background request event to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayBackgroundRequestEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.BACKGROUND_REQUEST_EVENT);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    } 

    /*
     * Display Preemption Management Events:
     *
     * PREEMPT
     *
     */    
    /**
     * Called to start preempting and end preempting.
     * Probably: will need more parameters, ex. MIDlet ID
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayPreemptStartEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.PREEMPT_EVENT);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;
        event.intParam2 = -1; /* start = true */

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    /**
     * Called to start preempting and end preempting.
     * Probably: will need more parameters, ex. MIDlet ID
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendDisplayPreemptStopEvent(int midletDisplayId) {
        NativeEvent event =
            new NativeEvent(EventTypes.PREEMPT_EVENT);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;
        event.intParam2 = 0; /* start = false */

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }

    /**
     * Called to send a MIDlet resume request to the AMS isolate.
     *
     * @param midletDisplayId ID of the sending Display
     */
    public void sendMIDletResumeRequest(int midletDisplayId) {
        NativeEvent event = 
            new NativeEvent(EventTypes.MIDLET_RESUME_REQUEST);

        event.intParam1 = currentIsolateId;
        event.intParam4 = midletDisplayId;

        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
}

