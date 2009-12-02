package datanetwork;

import datanetwork.javaosc.*;

/** 
 * <p>Simple class that catches the reply from the server to a specific message.  
 * Certain messages sent to the server require replies from the server to arcknolege their reception.  This class provides that functionality.
 * If the reply to the sent message has not been received within 2 seconds, the {@link #timeout()} will issue an error message.</p>
 *
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>OSC classes are based on Java OSC.
 * Copyright (c) 2002-2006, C. Ramakrishnan / Illposed Software</p>
 *
 * @version 001
 * @author Vincent de Belleval
 * @see DNConnection
 * @see OSCPortIn
 * @see OSCPortOut
 */

public class Responder implements OSCListener {
	OSCMessage msg;
   	OSCMessage rsp;
	OSCPortIn in;
	OSCPortOut out;
	
	Thread timer;
	int timeout = 3000;
	boolean responded;

	/**
	 * Constructs a Responder which adds itself as a listener on the DNConnection's OSCPortIn for a specific reply.
	 * Starts a timer thread which will call {@link #timeout()} after 2 seconds unless it reveives the correct reply.
	 *
	 * @see DNConnection
	 * @see OSCPortIn
	 * @see OSCPortOut
	 * @param in DNConnection's incoming port.
	 * @param msg message sent for which a reply is expected.
	 * @param rsp the reply message this Responder is waiting for.
	 */
	public Responder(OSCPortIn in, OSCPortOut out, OSCMessage msg, OSCMessage rsp) {
		this.in = in;
		this.out = out;
		this.msg = msg;
		this.rsp = rsp;
		
		in.addListener(this);
		responded = false;

		timer = new Thread(new Runnable() {
			public void run() {
				int count = 0;
           		while(!responded && count < 10) {
					sendMsg();
					count++;
					try {
                		Thread.sleep(timeout);
						System.out.println("DataNetwork warning: server did not recieve " + getMsgAddr() + " attempting again...");
					} catch(Exception e) {
		            }
               	}
				if(!responded) timeout();
           	}
   		});
       
       timer.setDaemon(true);
       timer.start();
   	}

 	/**
     * Interface method from OSCListener to recieve messages.
     * Once the correct reply has been received, it will attempt to interrupt the timer thread as well as to remove itself as a listener.
 	 *
     * @see OSCListener
     * @see OSCPortIn
     * @see OSCMessage
  	 * @param message OSCMessage received on a OSCPortIn listener.
	 */
   	public void acceptMessage(OSCMessage message) {	
       	if(message.getAddress().equals(rsp.getAddress()) || message.getAddress().equals("/error")) {
			try {			
				responded = true;
				timer.interrupt();
				in.removeListener(this);
			} catch(SecurityException e) {
				System.err.println("DataNetwork error: cannot shut down Responder thread");
			}
	 	}
	}
	
	private void sendMsg() {
		out.send(msg);
	}
	
	private String getMsgAddr() {
		return msg.getAddress();
	}
	
	private boolean isResponded() {
		return responded;
	}
	
   	private void timeout() {
		System.err.println("Error receiving response from server for " + msg.getAddress());
		in.removeListener(this);
   	}

}