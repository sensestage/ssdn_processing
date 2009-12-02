package datanetwork.javaosc;

import datanetwork.javaosc.utility.*;

/**
 * <p>OSCMessage abstraction.</p>
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.<p>
 */

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