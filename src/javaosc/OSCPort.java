package datanetwork.javaosc;

import java.net.DatagramSocket;

/**
 * <p>Port abstraction</p>
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.<p>
 */

public abstract class OSCPort {

	DatagramSocket socket;

	int port;

	protected void finalize() throws Throwable {
		socket.close();
	}
	
	public int getPort() {
		return port;
	}
	
	public void close() {
		socket.close();
	}

}
