package com.sun.midp.io;

import org.thenesis.midpath.io.MemoryFileHandler;

import com.sun.midp.io.j2me.file.BaseFileHandler;
import com.sun.midp.io.j2me.serversocket.ServerSocketPeer;
import com.sun.midp.io.j2me.socket.SocketPeer;
import com.sun.midp.main.Configuration;

public class IOToolkit {

	private static IOToolkit toolkit = new IOToolkit();
	private String backendName;

	private IOToolkit() {
		backendName = Configuration.getPropertyDefault("com.sun.midp.io.backend", "null");
	}

	public BaseFileHandler createBaseFileHandler() {
		if (backendName.equalsIgnoreCase("J2SE")) {
			return new org.thenesis.midpath.io.backend.j2se.FileHandlerImpl();
		} else if (backendName.equalsIgnoreCase("CLDC")) {
			return new org.thenesis.midpath.io.backend.cldc.FileHandlerImpl();
		} else {
			return new MemoryFileHandler();
		}
	}

	public SocketPeer getSocketPeer() {
		if (backendName.equalsIgnoreCase("J2SE")) {
			return new org.thenesis.midpath.io.backend.j2se.SocketPeerImpl();
		} else if (backendName.equalsIgnoreCase("CLDC")) {
			return new org.thenesis.midpath.io.backend.cldc.SocketPeerImpl();
		} else {
			return null;
		}
	}

	public ServerSocketPeer getServerSocketPeer() {
		if (backendName.equalsIgnoreCase("J2SE")) {
			return new org.thenesis.midpath.io.backend.j2se.ServerSocketPeerImpl();
		} else if (backendName.equalsIgnoreCase("CLDC")) {
			return null;
		} else {
			return null;
		}
	}

	public static IOToolkit getToolkit() {
		return toolkit;
	}

}
