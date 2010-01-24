package datanetwork;

import datanetwork.javaosc.*;
import java.net.*;
import java.io.*;	
import java.util.Date;
import java.util.Vector;
import java.lang.reflect.Method;
import processing.core.PApplet;

/** 
 * <p>The SenseWorld DataNetwork Processing library offers a quick and easy way to retrieve or publish data from a SenseStage DataNetwork.</p>
 * 
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>OSC classes are based on Java OSC.  Copyright (c) 2002-2006, C. Ramakrishnan / Illposed Software</p>
 * 
 * <p>The DNConnection sets up the incomming and outgoing connections to the server.
 * Once connected to server, it is possible to send messages on the network using the methods provided in this class.
 * To ensure the minimum chance of errors in communication this class also recieves all the messages from the server.  
 * From here it decides which ones should go through the Processing parent sketch and which ones should not.
 * To recieve the messages in the Processing sketch, it needs to implement the dnEvent(OSCMessage msg) method.</p>

 * <p>Please look at the Processing examples provided in the example folders to see a basic implementation in a Processing sketch.</p>
 *
 * @version 002
 * @author Vincent de Belleval (v@debelleval.com)
 */

public class DNConnection {
	OSCPortIn in;
	OSCPortOut out;
	PApplet parent;
	Method[] dnEvent;
	PingResponder pingResponder;
	int incoming_port, outgoing_port;
	Vector<DNNode> client_nodes;
	Vector<Integer> subscribtion_list;
	Vector<Subscribtion> subscribtion;
	int verbo = 0;
	String name;
	String address;
	boolean isRegistered;
	boolean subscribe_all;
	
	/** 
	 * Constructs a new DNConnection attatched to the specified PApplet.  This is the only available constructor available even if some of the arguments are optional in an attempt to enforce good practices.
	 *
	 * @see #getServerPort(String addy)
	 * @param parent the Processing PAapplet to which the DNConnection is attached.
	 * @param address the IP address of the server to attempt to connect to.
	 * @param outgoing_port the port the sever at the specified address is listening to.  It is possible to query this port on the server {@link #getServerPort(String addy)} directly from the constructor.
	 * @param incoming_port the port DNConnection will listen to for incomming messages from the server.
	 * @param name the name the client will have on the network. 
	 */
	public DNConnection(PApplet parent, String address, int outgoing_port, int incoming_port, String name) {
		this.parent = parent;
		this.address = address;
		this.incoming_port = incoming_port;
		this.outgoing_port = outgoing_port;
		this.name = name;
		
		in = new OSCPortIn(this ,incoming_port);
		out = new OSCPortOut(address, outgoing_port);

		client_nodes = new Vector<DNNode>();
		subscribtion_list = new Vector<Integer>();
		subscribtion = new Vector<Subscribtion>();

		isRegistered = false;
		subscribe_all = false;
		
		dnEvent = new Method[3];
		try {
			dnEvent[0] = parent.getClass().getMethod("dnEvent", new Class[] { OSCMessage.class });
		} catch (Exception e) {}
		try {
			dnEvent[1] = parent.getClass().getMethod("dnEvent", new Class[] { String.class, String[].class });
		} catch (Exception e) {}
		try {
			dnEvent[2] = parent.getClass().getMethod("dnEvent", new Class[] { String.class, float[].class });
		} catch (Exception e) {}
		
		System.out.println("\nSenseWorldDataNetwork client connenected to server at "+address+"\nListening to port "+in.getPort()+"\nSending on port "+out.getPort()+"\n");
		
	}
	
	/**
	 * Fetches the port the SenseWorldDataNetwork server is listening to (see the SenseWorldDataNetwork documentation for more details).  
	 * Can be called directly from the DNConnection constructor.
	 *
	 * @param serverAddress the server's IP address.
	 */
	public static int getServerPort(String serverAddress) {
		int port = 57120;
		try {
		    URL server = new URL("http://"+serverAddress+"/SenseWorldDataNetwork");
		    URLConnection serverConnection = server.openConnection();
			BufferedReader serverIn = new BufferedReader(new InputStreamReader(server.openStream()));
			port = Integer.parseInt(serverIn.readLine());	
			serverIn.close();
		} catch (MalformedURLException e) { 
		} catch (IOException e) {             
			System.err.println("\nSenseWorldDataNetwork error: cannot connect with server at "+serverAddress);
		}
		if(port == 0)
			System.err.println("\nSenseWorldDataNetwork error: error fetching server's incoming port from http://"+serverAddress+"/SenseWorldDataNetwork");
		return port;
	}
	
	/** 
	 * Not to be called directly.
	 * Recieves and routes all the server messages.
	 *
	 * @param message the recieved OSCMessage.
     */
	public void oscEvent(OSCMessage message) {
		String addr = message.getAddress();	
		Object[] args = message.getArguments();
		
		if(addr.equals("/datanetwork/announce")) {
			printmsg(addr, args);
			outgoing_port = getServerPort(address);	//get the current server port.  might have changed at reboot.
			register();
		} else if(addr.equals("/datanetwork/quit") && args[0].equals(address) && args[1].equals(outgoing_port)) {
			isRegistered = false;
			printmsg(addr, args);
			pingResponder.removePingResponder();
		} else if(addr.equals("/error")) {
			if(verbo > 0) {
				System.out.print("\nSenseWorldDataNetwork server error: ");
				printmsg(addr, args);
			}
		} else if(addr.equals("/warn")) {
			if(verbo > 1) {
				System.out.print("\nSenseWorldDataNetwork server warning: ");
				printmsg(addr, args);
			}
		} else if(addr.equals("/ping")) {
			if(verbo > 4) printmsg(addr, args);
		} else if(addr.equals("/registered")) {
			out = new OSCPortOut(address, outgoing_port);
			isRegistered = true;
			dnEventInvoke(message);
			if(verbo > 3) printmsg(addr, args);
			
			//add previously expected nodes on register
			if(!client_nodes.isEmpty()) {
				DNNode[] nds = new DNNode[client_nodes.size()];
				for(int i = 0;i < client_nodes.size();i++) nds[i] = client_nodes.elementAt(i);
				addExpected(nds);
			}
		} else if(addr.equals("/unregistered")) {
			isRegistered = false;
			dnEventInvoke(message);
			if(verbo > 3) printmsg(addr, args);
		} else if(addr.equals("/subscribed/node")) {	//SUBSCRIBE NODE
			if(verbo > 3) printmsg(addr, args);
			if(subscribtion_list.indexOf(Integer.parseInt(args[2].toString())) == -1) {
				subscribtion_list.addElement(Integer.parseInt(args[2].toString()));
				subscribtion.addElement(new Subscribtion(Integer.parseInt(args[2].toString())));
			}
		} else if(addr.equals("/unsubscribed/node")) {	//UNSUBSCRIBE NODE
			if(verbo > 3) printmsg(addr, args);
			int nodeIndex = subscribtion_list.indexOf(Integer.parseInt(args[2].toString()));
			if(nodeIndex != -1) {
				subscribtion_list.removeElementAt(nodeIndex);
				subscribtion.removeElementAt(nodeIndex);
			}
	 	} else if(addr.equals("/subscribed/slot")) {	//SUBSCRIBE SLOT
			if(verbo > 3) printmsg(addr, args);
			if(subscribtion_list.indexOf(Integer.parseInt(args[2].toString())) == -1) {
				subscribtion_list.addElement(Integer.parseInt(args[2].toString()));
				subscribtion.addElement(new Subscribtion(Integer.parseInt(args[2].toString()), Integer.parseInt(args[3].toString())));
			} else subscribtion.elementAt(subscribtion_list.indexOf(Integer.parseInt(args[2].toString()))).addSubscribedSlot(Integer.parseInt(args[3].toString()));
		} else if(addr.equals("/unsubscribed/slot")) {	//UNSUBSCRIBED SLOT
			if(verbo > 3) printmsg(addr, args);
			int nodeIndex = subscribtion_list.indexOf(Integer.parseInt(args[2].toString()));
			if(nodeIndex != -1) {
				subscribtion.elementAt(nodeIndex).removeSusbscribedSlot(Integer.parseInt(args[3].toString()));
				if(subscribtion.elementAt(nodeIndex).getSize() == 0) {
					subscribtion_list.removeElementAt(nodeIndex);
					subscribtion.removeElementAt(nodeIndex);
				}
			}
		} else if(addr.equals("/removed/node")) {	//REMOVED NODE			
			if(verbo > 3) printmsg(addr, args);
		} else if(addr.equals("/info/node")) {	//INFO NODE - ADD PREVIOUSLY SUBSCRIBED NODES ON SERVER REBOOT
			if(!subscribtion.isEmpty()) {
				for(int i = 0;i< subscribtion.size();i++) {
					if(subscribtion.elementAt(i).getSize() != 0) subscribeSlot(subscribtion.elementAt(i).getSusbscribedNode(), subscribtion.elementAt(i).getSusbscribedSlots());	
					else subscribeNode(subscribtion.elementAt(i).getSusbscribedNode());
				}
			}
			if(verbo > 3) printmsg(addr, args);
			dnEventInvoke(message);
		} else if(addr.equals("/info/expected") || addr.equals("/info/slot") || addr.equals("/info/client") || addr.equals("/info/setter") || addr.equals("/data/node") || addr.equals("/data/slot") || addr.equals("/removed/node")) {
			if(verbo > 3) printmsg(addr, args);
			dnEventInvoke(message);
		} else {
			System.err.println("\nSenseWorldDataNetwork unexpected message from server: ");
			printmsg(addr, args);
		}
	}
	
	/** 
	 * Looks for which dnEvent method is implemented in the Processing sketch and passes the right arguments to the correct method.
	 * <p>dnEvent[0] will pass the whole OSCMessage.  Make sure the datanetwork.javaosc.OSCMessage is imported in the Processing sketch.<br />
	 * dnEvent[1] will pass the message's address as a string and the arguments as an array of strings.<br />
	 * dnEvent[2] will pass the message's address as a string and the arguments as an array of floats.
	 * 
	 * @param message the OSCMessage being passed to the dnEvent method.
	 */
	private void dnEventInvoke(OSCMessage message) {
		if(dnEvent[0] != null) {
			try {
				dnEvent[0].invoke(parent, new Object[] { message } );
			} catch (Exception e) {
				System.err.println("\nSenseWorldDataNetwork client error: dnEvent[0] method not present in the Processing sketch.");
				e.printStackTrace();
				dnEvent = null;
			}
		}
		if(dnEvent[1] != null) {
			try {
				dnEvent[1].invoke(parent, new Object[] { message.getAddress(), message.getArgumentsString() } );
			} catch (Exception e) {
				System.err.println("\nSenseWorldDataNetwork client error: dnEvent[1] method not present in the Processing sketch.");
				e.printStackTrace();
				dnEvent = null;
			}
		}
		if(dnEvent[2] != null) {
			try {
				dnEvent[2].invoke(parent, new Object[] { message.getAddress(), message.getArgumentsFloat() });
			} catch (Exception e) {
				System.err.println("\nSenseWorldDataNetwork client error: dnEvent[2] method not present in the Processing sketch.");
				e.printStackTrace();
				dnEvent = null;
			}
		}
	}
	
	/**
	 * Sets the level of verbosity.<br /><br /> 
	 * 0 - only sever announce and quit messages.<br />
	 * 1 - server errors.<br />
	 * 2 - server warnings.<br />
	 * 3 - client warnings.<br />
	 * 4 - server responses.<br />
	 * 5 - server ping messages.<br />
	 * @param level the level of verbosity to be set.
	 */
	public void setVerbo(int level) {
		verbo = level;
	}
	
	/** 
	 * Returns the verbosity level.
	 * @return the current verbosity level.
	 */
	public int getVerbo() {
		return verbo;
	}
	
	/**
	 * Prints the recieved OSCMessage cleanly to the console.
	 * Used for debugging.
	 * 
	 * @param addr the OSCMessage's address.
	 * @param args the OSCMessage's aguments
	 */
	private void printmsg(String addr, Object[] args) {
		System.out.print(" "+addr+" ");
		for(int i = 0;i < args.length;i++) {
			System.out.print(args[i]+", ");
		}
		System.out.println();
	}
	
	/**
	 * Returns whether or not the client has been registered on the server.
	 * @return true if the client is registered on the network.
	 */
	public boolean isRegistered() {
		return isRegistered;
	}

	
	/** CLIENT MESSAGES TO SERVER **/
	
	/** 
	 * Attempts to register the DNConnection to the network.
	 */
	public void register() {
		if(!isRegistered) {
			Object[] arg = {incoming_port, name};
			OSCMessage msg = new OSCMessage("/register", arg);
			OSCMessage rsp = new OSCMessage("/registered", arg);
			Responder responder = new Responder(in, out, msg, rsp);
			pingResponder = new PingResponder(in, out, name);
		}
	}
	
	/** 
	 * Attempts to unregister the DNConnection from the network.
 	*/
	public void unregister() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};
			OSCMessage msg = new OSCMessage("/unregister", arg);
			OSCMessage rsp = new OSCMessage("/unregistered", arg);
			Responder responder = new Responder(in, out, msg, rsp);
			pingResponder.removePingResponder();
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered.  Cannot unregister.");		
		}
	}
	
	/**
	 * Query all.
	 */ 
	public void queryAll() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/all", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Queries the server for all the expected nodes present on the network.
	 */
	public void queryExpected() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/expected", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Queries the server for all the nodes present on the network.
	 */
	public void queryNodes() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/nodes", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Queries the server for all the slots present on the network.
	 */
	public void querySlots() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/slots", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");	
		}	
	}
	
	/**
	 * Queries the server for all the clients present on the network.
	 */
	public void queryClients() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/clients", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Queries the server for all the nodes this client has set on the network.
	 */
	public void querySetters() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/setters", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Queries the server for all the subscriptions this client has.
	 */
	public void querySubscriptions() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/query/subscriptions", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query server.");		
		}
	}
	
	/**
	 * Subscribes to all nodes present on the network.
	 */
	public void subscribeAll() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/subscribe/all", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot subscribe to nodes.");		
		}
	}
	
	/**
	 * Unsubscribes to all nodes present on the network.
	 */
	public void unsubscribeAll() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/unsubscribe/all", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot unsubscribe from nodes.");		
		}
	}
	
	/**
	 * Subscribes to a specific node on the network.
	 * 
	 * @param nodeId the ID of the node to subscribe to.
	 */
	public void subscribeNode(int nodeId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId};	
			OSCMessage msg = new OSCMessage("/subscribe/node", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot subscribe to nodes.");		
		}
	}
	
	/**
	 * Unsubscribes from a specific node on the network.
	 * 
	 * @param nodeId the ID of the node to unsubscribe from.
	 */
	public void unsubscribeNode(int nodeId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId};	
			OSCMessage msg = new OSCMessage("/unsubscribe/node", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot unsubscribe from nodes.");		
		}
	}
	
	/**
	 * Subscribes to a specific slot on the network.
	 * 
	 * @param nodeId the ID of the node that contains the slot to subscribe to.
	 * @param slotId the ID of the slot to subscribe to.
	 */
	public void subscribeSlot(int nodeId, int slotId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId, slotId};	
			OSCMessage msg = new OSCMessage("/subscribe/slot", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot subscribe to slots.");		
		}
	}
	
	/**
	 * Allows to subscribe to multiple slots at once.
	 * @param nodeId the ID of the node that contains the slot to subscribe to.
	 * @param slotId the IDs of the slots to subscribe to.
	 */
	public void subscribeSlot(int nodeId, int[] slotId) {
		if(isRegistered) {
			for(int i = 0;i < slotId.length;i++) subscribeSlot(nodeId, slotId[i]);
		}
	}
	
	/**
	 * Unubscribes from a specific slot on the network.
	 * 
	 * @param nodeId the ID of the node that contains the slot to unsubscribe from.
	 * @param slotId the ID of the slot to unsubscribe from.
	 */
	public void unsubscribeSlot(int nodeId, int slotId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId, slotId};	
			OSCMessage msg = new OSCMessage("/unsubscribe/slot", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot unsubscribe from slots.");		
		}
	}
	
	/**
	 * Allows to subscribe to multiple slots at once.
	 * @param nodeId the ID of the node that contains the slot to subscribe to.
	 * @param slotId the IDs of the slots to subscribe to.
	 */
	public void unsubscribeSlot(int nodeId, int[] slotId) {
		if(isRegistered) {
			for(int i = 0;i < slotId.length;i++) unsubscribeSlot(nodeId, slotId[i]);
		}
	}
	
	/**
	 * Asks a node for the data it contains.  It does not return anything on its own. 
	 * The data will be passed to the dnEvent method wiht the "/data/node" address.
	 * 
	 * @param nodeId the ID of the node to query.
	 */
	public void getNode(int nodeId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId};	
			OSCMessage msg = new OSCMessage("/get/node", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query nodes.");		
		}	
	}
	
	/**
	 * Asks a slot for the data it contains.  It does not return anything on its own.  
	 * The data will be passed to the dnEvent method wiht the "/data/slot" address.
	 * 
	 * @param nodeId the ID of the node which contains the slot to query.
	 * @param slotId the ID of the slot to query.
	 */	
	public void getSlot(int nodeId, int slotId) {
		if(isRegistered) {
			Object[] arg = {incoming_port, name, nodeId, slotId};	
			OSCMessage msg = new OSCMessage("/get/slot", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot query slots.");		
		}	
	}

	/** 
	 * Sets the slots of a node to the array of floats being passed.
	 * The client needs to be the setter of the node in order to set its data.  
	 * The type of the node has to correspond with the right data type being passed to it.
	 *
	 * @see DNNode
	 * @param node the DNNode on which to set the data.
	 * @param data the array of floats that will fill the node's slots.
	 */
	public void setData(DNNode node, float[] data) {
		if(isRegistered && node.isExpected()) {
			if(node.type != 0) {
				System.out.println("\nDNNode error: wrong data type for node type " + node.type);
			} else {
				Vector<Object> args = new Vector<Object>();
				for(int i = 0;i < data.length; i++) {
					args.add(i, data[i]);
				}	
				args.add(0, incoming_port);
				args.add(1, name);
				args.add(2, node.nodeId);
				Object[] arg = args.toArray();	
				OSCMessage msg = new OSCMessage("/set/data", arg);
				out.send(msg);
			}
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: either the client is not registered or the node is not expected. Cannot set node "+node.nodeId);		
		}
	}
	
	/** 
	 * Sets the slots of a node to the array of Strings being passed.
	 * The client needs to be the setter of the node in order to set its data.  
	 * The type of the node has to correspond with the right data type being passed to it.
	 *
	 * @see DNNode
	 * @param node the DNNode on which to set the data.
	 * @param data the array of Strings that will fill the node's slots.
	 */
	public void setData(DNNode node, String[] data) {
		if(isRegistered && node.isExpected()) {
			if(node.type != 1) {
				System.out.println("\nDNNode error: wrong data type for node type " + node.type);
			} else {
				Vector<Object> args = new Vector<Object>();
				for(int i = 0;i < data.length; i++) {
					args.add(i, data[i]);
				}	
				args.add(0, incoming_port);
				args.add(1, name);
				args.add(2, node.nodeId);
				Object[] arg = args.toArray();	
				OSCMessage msg = new OSCMessage("/set/data", arg);
				out.send(msg);
			}
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot set nodes.");		
		}
	}
	
	/**
	 * Removes a node from the network.  Only possible if the client is the setter of the node.
	 *
	 * @see DNNode
	 * @param node the node to remove from the network.
	 */
	public void removeNode(DNNode node) {
		if(isRegistered && node.isExpected()) {
			Object[] arg = {incoming_port, name, node.nodeId};	
			OSCMessage msg = new OSCMessage("/remove/node", arg);
			out.send(msg);
		} 
		node.expected(false);		
		if(client_nodes.size() > 0) client_nodes.removeElementAt(client_nodes.indexOf(node));
	}
	
	//add DNNode[] method
	
	/**
	 * Removes all the nodes the client if a setter of from the network.  
	 */
	public void removeAll() {
		if(isRegistered) {
			Object[] arg = {incoming_port, name};	
			OSCMessage msg = new OSCMessage("/remove/all", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is unregistered. Cannot remove nodes.");		
		}
		for(int i = 0;i < client_nodes.size(); i++) {
			DNNode n = client_nodes.elementAt(i);
			n.expected(false);
		}
		if(client_nodes.size() > 0) client_nodes.removeAllElements();
	}
	
	/**
	 * Adds an expected node on the server.  
	 * Can also be called after using {@link DNNode#setType(int type)} of a node have been changed to update it on the network.  It might clear all its current data.
	 * 
	 * @param node the node to add on the network.
	 */
	public void addExpected(DNNode node) {
		if(!node.isExpected()) {			
			node.expected(true);
			client_nodes.addElement(node);
		}
		if(isRegistered) {
			Object[] arg = {incoming_port, name, node.nodeId, node.getSize(), node.label, node.type};	
			OSCMessage msg = new OSCMessage("/add/expected", arg);
			out.send(msg);
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot add nodes.");		
		}
	}
	
	/**
	 * Adds or updates an array of nodes on the network.
	 * 
	 * @see DNNode
	 * @param nodes the array of nodes to add on the network.
	 */
	public void addExpected(DNNode[] nodes) {
		for(int i = 0;i < nodes.length;i++) {
			if(!nodes[i].isExpected()) {
				client_nodes.addElement(nodes[i]);
				nodes[i].expected(true);
			}
		}
		if(isRegistered) {
			for(int i = 0;i < nodes.length;i++) {
				Object[] arg = {incoming_port, name, nodes[i].nodeId, nodes[i].getSize(), nodes[i].label, nodes[i].type};	
				OSCMessage msg = new OSCMessage("/add/expected", arg);
				out.send(msg);
			}
		} else {
			if(verbo > 2) System.err.println("\nSenseWorldDataNetwork warning: the client is not yet registered. Cannot add nodes.");		
		}
	}
		
	/**
	 * Safely closes the DNConnection.  It should be called from the stop() method in the PApplet.
	 * It will not remove the nodes the client is a setter of on the network.  If you wish to do so, call removeAll() before this method.
	 *
	 * @see DNNode
	 * @see #removeAll()
	 */
	public void close() {
		if(isRegistered) {
			unsubscribeAll();
			unregister();
		}
		in.close();
		out.close();
		System.out.println("\nSenseWorldDataNetwork connection to server on incoming port "+incoming_port+" and outgoing port "+outgoing_port+" closed.");
	}
}
