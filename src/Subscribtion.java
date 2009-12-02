package datanetwork;

import java.util.Vector;

public class Subscribtion {
	int nodeId;
	Vector<Integer> slotId;
	
	public Subscribtion(int nodeId) {
		this.nodeId = nodeId;
		slotId = new Vector<Integer>();
	}
	
	public Subscribtion(int nodeId, int slot) {
		this.nodeId = nodeId;
		slotId = new Vector<Integer>();
		slotId.addElement(slot);

	}
	
	public int getSize() {
		return slotId.size();
	}
	
	public int getSusbscribedNode() {
		return nodeId;
	}
	
	public int[] getSusbscribedSlots() {
		int[] ia = new int[slotId.size()];
		for(int i = 0;i < ia.length;i++) {
			ia[i] = slotId.elementAt(i);
		}
		return ia;
	}
	
	public int getSubscribedSlotIndex(int slot) {
		return slotId.indexOf(slot);
	}
	
	public void addSubscribedSlot(int slot) {
		if(slotId.indexOf(slot) == -1) slotId.addElement(slot);
	}
	
	public void removeSusbscribedSlot(int slot) {
		if(slotId.indexOf(slot) != -1) slotId.removeElementAt(slotId.indexOf(slot));
	}
}