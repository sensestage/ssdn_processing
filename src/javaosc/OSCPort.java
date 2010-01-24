package datanetwork.javaosc;

import java.net.DatagramSocket;

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
