/**
 * SenseWorldDataNetwork Processing library example.
 *
 * Part of Sense/Stage 
 * http://sensestage.hexagram.ca
 *
 * example by Vincent de Belleval (v@debelleval.com)
 *
 * Please look through the reference folder for all specifics about the client
 */

import datanetwork.*;	//import the datanetwork pacakage
import datanetwork.javaosc.OSCMessage;	//required for dnEvent(OSCMessages message)

DNConnection dn;	//DNConnection
DNNode node;	//DNNode

void setup() {
	size(200, 200);

	//Create a DNConnection.  Parameters are IP, outgoing port, incoming port, client name.
	dn = new DNConnection(this, "192.168.1.102", dn.getServerPort("192.168.1.102"), 6009, "p5Client");
	dn.setVerbo(4);	//set the verbosity to receive all messages but server pings.
	dn.register();	//register the client on the network.  Can be done anytime.
}

void draw() {
	background(0);
}

/**
 * Use your keyboard to send messages to the server.
 */
void keyPressed() {
	if(key == 'q') {
		dn.queryNodes();	//check which nodes are on the network.
	} else if(key == '1') {
		dn.getNode(100);	//get the data from node '100' (create it on the network first).
	} else if(key == 's') { 
		dn.subscribeNode(100);	//subscribe to all nodes on the network.
	} else if(key == 'u') {
		dn.unsubscribeNode(100);	//unsubscribe from all nodes on the network.
	}
}

/**
 * this dnEvent(OSCMessages message) method recieves whole OSCMessages.  You need to import datanetwork.javaosc.* in order for it to work.
 * the OSCMessage class provides a few methods to parse messages.  Please refer to the javadocs in the reference folder.
 */
void dnEvent(OSCMessage message) {
	println("OSCMessage: " + message.getAddress());
}

/**
 * this dnEvent(String addr, floatp[] args) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(String addr, float[] args) {
	println("Float: '" + addr + "' with " + args.length + " arguments as floats");
}

/**
 * this dnEvent(String addr, floatp[] args) method receives the message's address as a String and all of its arguments in a String array.
 */
void dnEvent(String addr, String[] args) {
	println("String: '" + addr + "' with " + args.length + " arguments as Strings");
}

/**
 * Always close the DNConnection.  
 * It's optional to remove all the nodes created by the client but the sever currently will not recognize the client on reboot.
 */
void stop() {
	dn.removeAll();	//removes all the nodes the client has created from the network,
	dn.close();	//unregisters the client and closes every port and connection.
}

