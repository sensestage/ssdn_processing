package datanetwork.javaosc;

import datanetwork.javaosc.utility.*;

public abstract class OSCPacket {

	protected boolean isByteArrayComputed;
	protected byte[] byteArray;

	protected void computeByteArray() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		computeByteArray(stream);
	}
	
	protected abstract void computeByteArray(OSCJavaToByteArrayConverter stream);

	public byte[] getByteArray() {
		if (!isByteArrayComputed) computeByteArray();
		return byteArray;
	}
}