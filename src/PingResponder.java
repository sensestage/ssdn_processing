package datanetwork;

import datanetwork.javaosc.*;
import java.util.Date;

/**
 * <p>Keeps the connection of the client to the server alive by responding to the server's ping messages.  As soon as the client registers, the server sends periodic "ping" messages to the client.
 * The client must respond with a "pong" message otherwise after a certain delay set on the server, the client will be dropped</p>
 *
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>OSC classes are based on Java OSC.
 * Copyright (c) 2002-2006, C. Ramakrishnan / Illposed Software</p>
 *
 * @version 001
 * @author Vincent de Belleval
 * @see OSCListener
 * @see OSCPortIn
 * @see OSCPortOut
 * @see DNConnection
 */

public class PingResponder implements OSCListener {
	OSCPortIn in;
	OSCPortOut out;
	OSCMessage pong;
		
	/**
	 * Constructs a PingResponder capable of listening on the client's incoming port as well as to send on its outgoing port.
	 * 
	 * @param in the DNConnection OSCPortin.
	 * @param out the DNConnection OSCPortOut.
	*/	
	public PingResponder(OSCPortIn in, OSCPortOut out) {
		this.in = in;
		this.out = out;
		
		Object[] arg = {in.getPort()};
		pong = new OSCMessage("/pong", arg);
		
		in.addListener(this);
   	}

	/**
	 * Recieves OSCMessages from the listeners on OSCPortIn.  This method is inherited from the {@link OSCListener} interface.
	 * As soon as the message's address is "/ping" it sends a messge to the server with the "/pong" reply. 
	 * 
	 * @param message the recieved OSCMessage.
	*/
   	public void acceptMessage(OSCMessage message) {
       	if(message.getAddress().equals("/ping")) {
			out.send(pong);
	 	}
	}	
	
	/**
	 * Removes the PingResponder from the OSCPortIn listeners.  Called when {@link DNConnection#unregister()} is called.
	 */
   	public void removePingResponder() {
		in.removeListener(this);
   	}

}