/**
 * SenseWorldDataNetwork Processing library example.
 *
 * Part of Sense/Stage 
 * http://sensestage.hexagram.ca
 *
 * example by Vincent de Belleval (v@debelleval.com)
 *
 * Please look through the reference folder for all detailed reference on Processing the client
 *
 * This sketch is an exhaustive list of all the methods used to interact with the SenseStage DataNetwork server.
 * A DNConnection (used to manage communication with the server) and a DNode (represents a node that can be published on the server) are created in the setup() call.  
 * However, nothing is actually sent to the server at that time.  
 * Use the keyboard shortcuts to actually register, query, fetch and publish data on the SenseStage DataNetwork.
 */

import datanetwork.*;	//import the datanetwork package
import datanetwork.javaosc.*;	//required for dnEvent(OSCMessages message)

DNConnection dn;	//DNConnection instance
DNNode node;	//DNNode instance

void setup() {
	size(200, 200);

	//Create a DNConnection.  Parameters are this, IP, outgoing port, incoming port, client name.
	//getServerPort(IP) is a static method used to return the port number as published by the server.  
	//If it cannot get a connection it will connect to the specified IP on port 57120
	dn = new DNConnection(this, "192.168.0.104", dn.getServerPort("192.168.0.104"), 6009, "p5Client");

	//Set the verbosity to receive all messages but server pings.
	//0 - only sever announce and quit messages
	//1 - server errors
	//2 - server warnings
	//3 - client warnings
	//4 - server responses
	//5 - server ping messages
	dn.setVerbo(3);
			
	//Create a DNNode.  Parameters are NodeId, number of slots, type (0 = float, 1 = string), node name.
	node = new DNNode(2000, 5, 0, "p5Node");
}

void draw() {
	//not a very visual sketch... look at the Processing console.
	background(0);
}

void keyPressed() {
	//*** REGISTRATION ***//
	//register to the datanetwork
	if(key == 'r') dn.register();
	//unregister from the datanetwork
	else if(key == 'u') dn.unregister();
	
	//*** QUERIES ***//
	//query everything.  returns the info on the expected nodes, nodes present, slots, clients, setters and subscriptions.
	else if(key == 'q') dn.queryAll();
	//query the server for expected nodes.
	else if(key == 'e') dn.queryExpected();
	//query the server for present nodes.
	else if(key == 'n') dn.queryNodes();
	//query the server for present slots.
	else if(key == 's') dn.querySlots();
	//query the server for the clients present on the network.
	else if(key == 'c') dn.queryClients();
	//query the server as to which nodes this client is the setter of.
	else if(key == 't') dn.querySetters();
	//query the server for what this client is subscribed to.
	else if(key == 'b') dn.querySubscriptions();
	
	//*** SUBSCRIPTIONS ***// 
	//subscribe to all nodes present on the network.
	else if(key == 'a') dn.subscribeAll();
	//unsubscribe from all nodes the client is currently subscribed to.
	else if(key == 'z') dn.unsubscribeAll();
	//subscribe to a specific node.
	else if(key == 'f') dn.subscribeNode(401);
	//unsubscribe from a specific node.
	else if(key == 'v') dn.unsubscribeNode(401);
	//subscribe to a specific slot.  the node ID and slot ID (or an array of them) need to be passed.
	else if(key == 'o') dn.subscribeSlot(401, 1);
	//unsubscribe to a specific slot.  the node ID and slot ID (or an array of them) need to be passed.
	else if(key == 'p') dn.unsubscribeSlot(401, 1);
	
	//*** GET NODES ***//
	//ask for the data contained in a specific node.  its argument is the node's unique id.
	else if(key == 'g') dn.getNode(401);
	//ask for the data contained in a specific slot.  its argument 
	else if(key == 'h') dn.getSlot(401, 0);
	
	//*** PUBLISH NODES ***//
	//add an expected node to the network.  takes an instance of DNNode as argument.
	else if(key == 'x') dn.addExpected(node);
	//remove an expected node from the network.  takes an instance of DNNode as argument.
	else if(key == 'c') dn.removeNode(node);
	//set a node's data.  takes an instance of of a node as the first argument and the data as the second (
	else if(key == 'd') dn.setData(node, new float[] {random(10), random(10), random(10), random(10), random(10)} );
}

/**
 * this dnEvent(OSCMessage msg) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(OSCMessage msg) {
	print("OSCMessage: " + msg.getAddress());
	for(int i = 0;i < msg.getArguments().length;i++) print(" "+msg.getArgument(i));
	println();
}

/**
 * this dnEvent(String addr, String[] args) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(String addr, String[] args) {
	print("String: " + addr);
	for(int i = 0;i < args.length;i++) print(" "+args[i]);
	println();
}

/**
 * this dnEvent(String addr, float[] args) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(String addr, float[] args) {
	print("Float: " + addr);
	for(int i = 0;i < args.length;i++) print(" "+args[i]);
	println();
}
/**
 * Always close the DNConnection.  
 * Might be a good idea to remove to remove the nodes we have created and to unsubscribe from the nodes we were listening to.
 * Otherwise all the settings will be remembered by the server and applied again on startup.
 */
void stop() {
	dn.unsubscribeAll();
	dn.removeAll();	//removes all the nodes the client has created from the network,
	dn.close();	//unregisters the client and closes every port and connection.
}

