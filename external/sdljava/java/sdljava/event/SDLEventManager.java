package sdljava.event;
/**
 *  sdljava - a java binding to the SDL API
 *
 *  Copyright (C) 2004  Ivan Z. Ganza
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA
 *
 *  Ivan Z. Ganza (ivan_ganza@yahoo.com)
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import sdljava.SDLException;
import sdljava.SDLMain;

/**
 * The SDLEventManager is a thread helping us to handling SDL events. 
 * <P>
 * @author Bart LEBOEUF
 * @version $Id: SDLEventManager.java,v 1.6 2005/07/13 16:33:57 ivan_ganza Exp $ 
 */
public class SDLEventManager implements Runnable {

	/**
	 * Class Instance 
	 */
	private static SDLEventManager instance;
	/**
	 * Inner repository contains listener list by event type.
	 */
	private HashMap repository;
	/**
	 * Internal daemon thread reference
	 */
	private Thread managerThread;
	/**
	 * Stop thread flag
	 */
	private volatile boolean isStopped;
	
	static {
		instance = new SDLEventManager();
	}
	
	/**
	 * Constructor
	 */
	private SDLEventManager() 
	{
		repository = new HashMap();
		isStopped = false;
	}
	
	/**
	 * Get instance of this class
	 * @return SDLEventManager return this class instance
	 */
	public static SDLEventManager getInstance() 
	{
		return instance;
	}

	/**
	 * Register a listener for a list of events
	 * @param listener The class implements SDLEventListener interface
	 * @param events A list of events or event types.
	 * @return boolean return true if registration is done.
	 */
	public boolean register(SDLEventListener listener, List events)
	{
		if (listener == null || events == null) return false;
		Iterator ite = events.iterator();
		while(ite.hasNext()) {
			Object o = ite.next();
			if (SDLEvent.class.isAssignableFrom(o.getClass()))
				register(listener,o.getClass());
			else return false;
		}
		return true;
	}
	
	/**
	 * Register a listener for an event type
	 * @param listener The class implements SDLEventListener interface
	 * @param eventType An SDL event type.
	 * @return boolean return true if registration is done.
	 */
	public boolean register(SDLEventListener listener, Class eventType)
	{
		if (listener == null) return false;
		if (!SDLEvent.class.isAssignableFrom(eventType)) return false;
		List list = (List)repository.get(eventType);
		if (list==null) list = new ArrayList(); 
		list.add(listener);		
		repository.put(eventType,list);
		return true;
	}

	/**
	 * Unregister a SDLEventListener 
	 * @param listener The class implements SDLEventListener interface
	 * @return boolean return true if unregistration is done.
	 */
	public boolean unregister(SDLEventListener listener)
	{
		Iterator ite = repository.values().iterator();
		List list;
		SDLEventListener l;
		while (ite.hasNext()) {
			list = (List) ite.next();
			while (list.contains(listener)) {
				list.remove(listener);
			}
		}		
		return true;
	}
	
	/**
	 * Get an <code>Iterator</code> of registered listeners for a particular event type.
	 * @param eventType SDLEvent type.
	 * @return Iterator return an Iterator of listeners 
	 */
	public Iterator getRegisteredListeners(Class eventType) {
		return ((List)repository.get(eventType)).iterator();
	}
	
	/**
	 * Get an <code>Iterator</code> of events type listen.
	 * @return Iterator return an Iterator of Event type. 
	 */
	public Iterator getEventListeners() {
		return repository.keySet().iterator();
	}
	
	/**
	 * Unregistered all listeners.
	 * @return boolean return true if all listeners are unregistered 
	 */
	public boolean unregisterAll() {
		repository.clear();
		return true;
	}
	
	/**
	 * Count how many event type is listened.
	 * @return int number of event type registered.
	 */
	public int countEventListeners() {
		return repository.size();
	}
	
	/**
	 * Start listening and Wait a for events.
	 */
	public void startAndWait()  {
		if (managerThread!=null && managerThread.isAlive()) return;
		managerThread = null;
		managerThread = new Thread(this);
		managerThread.setDaemon(true);
		managerThread.start();
	}

	/**
	 * Stop handling the events.
	 */
	public void stop() {
		isStopped = true;
	}

	/**
	 * Run method for our thread. If an SDLException as occured in a listener, it will not stop manager.
	 */
	public void run() {
		isStopped = false;
		do {
			try {			
				notifyEvent(SDLEvent.waitEvent(!isStopped));
			} catch(SDLException se) {
			  if (!isStopped) 
			  	System.err.println("An error has occured while listening events : "+SDLMain.getError());
			} 
		} while (!isStopped);
	}

	/**
	 * Notify all listeners registered for the event type of the new SDLEvent.
	 * @param event The new SDLEvent 
	 */
	public void notifyEvent(SDLEvent event) {
		if(event != null)
		  synchronized(repository) {
			List listeners = (List)repository.get(event.getClass());
			// if no list available, exit method and wait for a new event
			if (listeners==null) return;
			// if empty listener list exist for this event type, remove it and get out. 
			else if (listeners.size() == 0) {
				repository.remove(event.getClass());
				return;
			}				 
			// for all listeners of the event type, notify them.
			Iterator ite = listeners.iterator(); 
			while (ite.hasNext()) {
				((SDLEventListener)ite.next()).handleEvent(event);
			}
		  }
	}
}
