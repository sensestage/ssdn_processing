package datanetwork.javaosc;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

import datanetwork.javaosc.*;

public class OSCPortOut extends OSCPort {

	protected InetAddress address;
	
	/**
	 * Attempts to connect to the server at the specified IP.  Creates a socket through which to connect to the server and sets the outgoing communication port.
	 *
	 * @param address the server's IP address as a {@link java.lang.String}.
	 * @param port the port to which send the {@link OSCMessage}.
	 */
	public OSCPortOut(String address, int port) {
		this.port = port;
		
		InetAddress tentativeAddress = null;
		
		try { 
			tentativeAddress = InetAddress.getAllByName(address)[0];
		} catch (UnknownHostException e1) {
			System.err.println("\nSenseWorldDataNetwork OSCPortOut error: could not get requested host: "+address+". Will attempt to connect to the localhost.");
			e1.printStackTrace();
			try {
				tentativeAddress = InetAddress.getLocalHost();
			} catch (UnknownHostException e2) {
				System.err.println("\nSenseWorldDataNetwork OSCPortOut error: could not get localhost.");
				e2.printStackTrace();
			}
		}
		
		this.address = tentativeAddress;
	
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println("\nSenseWorldDataNetwork OSCPortOut error: could not Create Outbound DatagramSocket");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends {@link OSCMessage} to the server.  
	 * @param message the {@link OSCMessage} to send.
	 */
	public void send(OSCMessage message) {
		byte[] byteArray = message.getByteArray();
		try {
			DatagramPacket datapacket = new DatagramPacket(byteArray, byteArray.length, address, port);
			socket.send(datapacket);
		} catch (Exception e) {
		}		
	}
	
	/** 
	 * Closes the socket used to connect to the server.
	 */
	public void close() {
		socket.close();
	}
}
