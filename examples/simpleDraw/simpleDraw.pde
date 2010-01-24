/**
 * SenseWorldDataNetwork Processing library example.
 *
 * Part of Sense/Stage 
 * http://sensestage.hexagram.ca
 *
 * example by Vincent de Belleval (v@debelleval.com)
 *
 * Please look through the reference folder for all detailed reference on Processing the client.
 *
 * This sketch is a simple example of how to publish and receive data on the SenseStage DataNetwork.
 * It has been designed so it can be used while running the server on your own machine.  

 * We will establish a connection with the SenseWorld DataNetwork, using an instance of DNConnection, 
 * and publish a node to it, using an instance of DNNode.
 * The node will contain the mouse position data.  We will retrieve this data form the network and use it to draw something in our sketch.
 */

import datanetwork.*;	//import the datanetwork package

DNConnection dn;	//DNConnection instance
DNNode mouse_node;	//DNNode instance

float[] mouse_position;	//array to contained the mouse position received from the server

void setup() {
	
	size(400, 400);
	smooth();
	stroke(0);
	fill(0);

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
	dn.setVerbo(5);	
	
	
	//Register the client on the network.  Can be done anytime but it needs to be called before any other messages are sent to the server.
	dn.register();	
	while(!dn.isRegistered());	//make sure this client has properly registered before doing anything else
	
	//Create a DNNode.  Parameters are NodeId, number of slots, type (0 = float, 1 = string), node name.
	//here we have a node whose id is 999, contains two slots (mouseX, mouseY) of float values.  its name is mouse_node.
	mouse_node = new DNNode(999, 2, 0, "mouse_node");
	
	//add the node to the server
	dn.addExpected(mouse_node);
	
	//lets initialise our mouse position array with values representing the origin on the screen.
	mouse_position = new float[] {0, 0};
	
	//subscribe to our own mouse_node.  notice that we subscribe to a node by passing its ID.
	dn.subscribeNode(999);
	
}

void draw() {
	
	background(255);
	
	//lets draw a circle using that mouse positions fetched from the server.
	ellipse(mouse_position[0], mouse_position[1], 15, 15);
	
}

/**
 * Setting the node's data with the mouse posisiton.
 */
void mouseMoved() {
	
	//Make sure we do not send any data until node is expected.  Not necessary, but it not a bad thing.
	if(mouse_node.isExpected()) {	
		//Send the mouse position as an array to our mouse_node.
		//make sure you pass a DNNnode instance to this method.
		dn.setData(mouse_node, new float[] {mouseX, mouseY});	
	}
}

/**
 * this dnEvent(String addr, float[] args) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(String addr, float[] args) {

	//make sure we're reading a message containing node info
	if(addr.equals("/data/node")) {
		//make sure we're reading the correct node
		if((int)(args[0]) == 999) {
			//make sure to match your array indexes properly otherwise the sketch will crash.
			//here we are reading the args[] array starting from 1 (skipping the nodeId argument) until its last element.
			//our mouse_position[] starts from 0 and its last element is at 1 so we need to substract 1 to its index for the two arrays to match.
			for(int i = 1;i < args.length;i++) mouse_position[i-1] = args[i];
		}
	}
}

/**
 * Always close the DNConnection.  
 * Might be a good idea to remove to remove the nodes we have created and to unsubscribe from the nodes we were listening to.
 * Otherwise all the settings will be remembered by the server and applied again on startup.
 */
void stop() {
	dn.unsubscribeAll();	//unsubscribe from all our current subscriptions.
	dn.removeAll();	//removes all the nodes we've created on the network,
	dn.close();	//unregisters the client and closes every port and connection.
}
