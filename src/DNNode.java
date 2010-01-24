package datanetwork;

import datanetwork.javaosc.*;
import java.util.Vector;

/**
 * <p>Allows the creation of nodes that can be published on the SenseWorld DataNetwork.</p>
 *
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * 
 * <p>A node consists of a collection of slots which in turn contain data as either a String or a float.
 * It is important that your nodes have the right type for the data type being passed to it, otherwise the server returns an error.
 * It is possible to change the type of a node by calling {@link #setType(int type)} and then {@link DNConnection#addExpected(DNNode node)} but it will reset the node and its data.
 * To get the content of a node or of some of its slots use the query methods in {@link DNConnection} such as {@link DNConnection#getNode(int nodeId)} or {@link DNConnection#getSlot(int nodeId, int slotId)}. 
 *
 * @version 002
 * @author Vincent de Belleval (v@debelleval.com)
 */

public class DNNode {
	Vector<Object> slots;
	String label;
 	int port, nodeId, type;
	private boolean expected;
	/**
	 * Constructs a data node with no specified size and type. Untested!
	 * 
	 * @param nodeId unique integer that is used to refer to this node on the network.
	 * @param label name of the node on the server
	 */
	public DNNode(int nodeId, String label) {
		this.nodeId = nodeId;
		this.label = label;
		slots = new Vector<Object>();
		slots.setSize(10);
	}
	/**
	 * Constructs a SenseWorldDataNetwork node with a set number of slots and type.
	 * The number of slots cannot be changed once the node has been instanciated.
	 * 
	 * @param nodeId unique integer that is used to refer to this node on the network.
	 * @param label name of the node on the server.
	 * @param slotNum number of slots contained in the node.
	 * @param type data type the slots are expecting.  0 is {@link java.lang.Float} and 1 represents {@link java.lang.String}.
	 */
	public DNNode(int nodeId, int slotNum, int type, String label) {
		this.nodeId = nodeId;
		this.label = label;
		this.type = type;
		slots = new Vector<Object>();
		slots.setSize(slotNum);
	}
	
	public boolean isExpected() {
		return expected;
	}
	
	public void expected(boolean state) {
		expected = state;
	}
	
	/**
	 * Returns the size of the node: the number of slots it has.
	 *
	 * @return number of slots contained in the node.
	 */
	public int getSize() {
		return slots.size();
	}
}