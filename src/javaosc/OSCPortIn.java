package datanetwork.javaosc;

import datanetwork.javaosc.utility.OSCByteArrayToJavaConverter;
import datanetwork.DNConnection;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Date;

/**
 * <p>Provides a socket connection to the server and implements {@link OSCListener} interface to allow to listen for {@link OSCMessage}</p>
 * 
 * <p>Part of the <a target="_blank" href="http://sensestage.hexagram.ca">Sense Stage</a> project.
 *
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.</p>
 *
 * @version 001
 * @author Chandrasekhar Ramakrishnan, Severin Smith, Vincent de Belleval
 */

public class OSCPortIn extends OSCPort implements Runnable {
	DNConnection dn;
	Method eventMethod;
	Thread thread;
	CopyOnWriteArrayList<OSCListener> listeners;	//this is thread safe compared to vectors
	protected OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
	
	private boolean isListening;
	
	/**
	 * Creates an OSCPortIn attached to the {@link DNConnection}.  The OSCPortIn will listen to the specified port for incoming {@link OSCMessage}.
	 *
	 * @param dn the DNConnection {@link DNConnection} OSCPortIn is attatched to.
	 * @param port the port on which to listen for OSCMessages.
	 */
	public OSCPortIn(DNConnection dn, int port) {
		this.dn = dn;
		this.port = port;
		
		listeners = new CopyOnWriteArrayList<OSCListener>();
		
		try {
			socket = new DatagramSocket(port);
			startListening();
		} catch (SocketException e) {
			System.err.println("\nSenseWorldDataNetwork OSCPortIn error: could not bind to port "+port+".");
			e.printStackTrace();
		}
				
		try {
			eventMethod = dn.getClass().getMethod("oscEvent", new Class[] { OSCMessage.class});
		} catch (Exception e) {}
	}
	
	public void run() {
		byte[] buffer = new byte[1536];
	    DatagramPacket packet = new DatagramPacket(buffer, 1536);
	    while(isListening() == true) {
	    	try {
	        	socket.receive(packet);
	            OSCPacket oscPacket = converter.convert(buffer, packet.getLength());
	            synchronized(this) {
	            	dispatchPacket(oscPacket);
	            }
	        } catch(Exception e) {
			}
		}
 	}
	
	private void dispatchPacket(OSCPacket packet) {
		dispatchPacket(packet, null);
	}
	
	private void dispatchPacket(OSCPacket packet, Date timestamp) {
		if(packet instanceof OSCBundle) dispatchBundle((OSCBundle) packet);
		else dispatchMessage((OSCMessage) packet, timestamp);
	}
	
	
	private void dispatchBundle(OSCBundle bundle) {
		Date timestamp = bundle.getTimestamp();
		OSCPacket[] packets = bundle.getPackets();
		for(OSCPacket packet : packets) {
			dispatchPacket(packet, timestamp);
		}
	}
	
	private void dispatchMessage(OSCMessage message) {
		dispatchMessage(message, null);
	}
	
	private void dispatchMessage(OSCMessage message, Date time) {
		for(OSCListener listener : listeners) {
			listener.acceptMessage(message);
		}
		
		if(eventMethod != null) {
			try {
				eventMethod.invoke(dn, new Object[] { message });
			} catch (Exception e) {
				e.printStackTrace();
				eventMethod = null;
			}
		}
	}
	
	private void startListening() {
		isListening = true;
		thread = new Thread(this);
		thread.start();
	}
	
	private void stopListening() {
		isListening = false;
	}
	
	public boolean isListening() {
		return isListening;
	}
	
	/** 
	 * Adds a listener to OSCPortIn.
	 * @param listener the listener to be added.
	 */
    public synchronized void addListener(OSCListener listener) {
        listeners.add(listener);
    }
	
	/** 
	 * Removes a listener from OSCPortIn.
	 * @param listener the listener to be removed.
	 */
	public synchronized void removeListener(OSCListener listener) {
	        listeners.remove(listener);
	}
	
	/**
	 * Stops and removes all the listeners then kills the OSCPortIn thread.
	 */
	public void close() {
		stopListening();
		for(OSCListener listener : listeners) {
			removeListener(listener);
		}
		try {
			thread.interrupt();
		} catch(SecurityException e) {
		} catch(Exception e) {
        }
		socket.close();
	}
}
