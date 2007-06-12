package com.sun.midp.io;

import org.thenesis.midpath.io.backend.j2se.FileHandlerImpl;
import org.thenesis.midpath.io.backend.j2se.ServerSocketPeerImpl;
import org.thenesis.midpath.io.backend.j2se.SocketPeerImpl;

import com.sun.midp.io.j2me.file.BaseFileHandler;
import com.sun.midp.io.j2me.serversocket.ServerSocketPeer;
import com.sun.midp.io.j2me.socket.SocketPeer;

public class IOToolkit {

	private static IOToolkit toolkit = new IOToolkit();
	
	private IOToolkit() {}
	
//	public RandomAccessStream createRandomAccessStream(String name) throws IOException {
//		return new FileRandomAccessStream(name);
//	}
	
//	public FileConnection createFileConnection(String name) throws IOException {
//		return new FileConnectionImpl(name);
//	}
	
	public BaseFileHandler createBaseFileHandler() {
		return new FileHandlerImpl();
	}
	
	public SocketPeer getSocketPeer() {
		return new SocketPeerImpl();
	}
	
	public ServerSocketPeer getServerSocketPeer() {
		return new ServerSocketPeerImpl();
	}
	
	public static IOToolkit getToolkit() {
		return toolkit;
	}
	
}
