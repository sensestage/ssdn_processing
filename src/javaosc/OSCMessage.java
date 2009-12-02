package datanetwork.javaosc;

import datanetwork.javaosc.utility.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * <p>Defines OSCMessges and provides some methods to manipulate them.</p>
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.<p>
 */

public class OSCMessage extends OSCPacket {

	protected String address;
	protected Vector<Object> arguments;
	
	/**
	 * Create an empty OSC Message.
	 * In order to send this osc message, you need to set the address and, perhaps, some arguments.
	 */
	public OSCMessage() {
		arguments = new Vector<Object>();
	}
	
	/**
	 * Creates an OSCMessage with a specific address and arguments.  Ensures the server gets what it wants.
	 * 
 	 * @param address the OSC address the message will have.
     * @param arguments the arguments that will follow the address in the OSCMessage.
	 */
	public OSCMessage(String address, Object[] arguments) {
		this.address = address;
		if(null != arguments) {
			this.arguments = new Vector<Object>(arguments.length);
			for (int i = 0; i < arguments.length; i++) {
				this.arguments.add(arguments[i]);
			}
		} else {
			this.arguments = new Vector<Object>();
		}
	}
	
	/**
	 * Returns the OSCMessage address as a {@link java.lang.String}.
	 * @return the message's address.
     */
	public String getAddress() {
		return address.toString();
	}
	
	/**
	 * Sets the address of an OSCMessage.
	 * @param anAddress the address to set to the OSCMessage.
	 */
	public void setAddress(String anAddress) {
		address = anAddress;
	}
	
	/**
	 * Returns the first part of the OSCMessage's address.
	 * @return the first address of an OSCMessage.
	 */
	public String getTopAddress() {
		String[] topAddress = address.split("/");
		return topAddress[1];
	}
	
	/** 
	 * Returns all the OSCMessage arguments in an array.
	 * @return all the message's arguments in an array.
	 */
	public Object[] getArguments() {
		return arguments.toArray();
	}
	
	/** 
	 * Returns all the OSCMessage arguments in an array of {@link java.lang.String}.
	 * @return all the message's arguments in an {@link java.lang.String} array.
	 */	
	public String[] getArgumentsString() {
		String[] s = new String[arguments.size()];
		for(int i = 0;i < arguments.size();i++) {
			s[i] = arguments.elementAt(i).toString();
		}
		return s;
	}
	
	/** 
	 * Returns all the OSCMessage arguments in an array of {@link java.lang.Float}.
	 * @return all the message's arguments in an {@link java.lang.Float} array.
	 */	
	public float[] getArgumentsFloat() {
		String[] s = new String[arguments.size()];
		float[] f = new float[s.length];
		for(int i = 0;i < s.length;i++) {
			s[i] = arguments.elementAt(i).toString();
			try {
				f[i] = Float.parseFloat(s[i]);
			} catch(NumberFormatException e) {}	//catch strings that aren't parasable as floats.
		}
		return f;
	}
	
	/** 
	 * Returns the OSCMessage arguments at the specified index as a {@link java.lang.Object}.
	 * @param i the index of the argument to be returned.
	 * @return the message's argument at the specified index as a {@link java.lang.Object}.
	 */
	public Object getArgument(int i) {
		return arguments.get(i);
	}

	/** 
	 * Returns the OSCMessage arguments at the specified index as a {@link java.lang.String}.
	 * @param i the index of the argument to be returned.
	 * @return the message's argument at the specified index as a {@link java.lang.String}.
	 */
	public String getArgumentString(int i) {
		return arguments.get(i).toString();
	}
	
	/** 
	 * Returns the OSCMessage arguments at the specified index as a {@link java.lang.Float}.
	 * @param i the index of the argument to be returned.
	 * @return the message's argument at the specified index as a {@link java.lang.Float}.
	 */
	public float getArgumentFloat(int i) {
		return Float.parseFloat(arguments.get(i).toString());
	}
	
	/**
	 * Adds an argument to an OSCMessage.
	 * @param argument the argument to be added.
	 */
	public void addArgument(Object argument) {
		arguments.add(argument);
	}
	
	/**
	 * Adds arguments to an OSCMessage.
	 * @param arguments the arguments to be added.
	 */
	public void setArguments(Object[] arguments) {
		this.arguments.add(arguments);
	}
	
	protected void computeAddressByteArray(OSCJavaToByteArrayConverter stream) {
		stream.write(address);
	}
	
	protected void computeArgumentsByteArray(OSCJavaToByteArrayConverter stream) {
		stream.write(',');
		if (null == arguments)
			return;
		stream.writeTypes(arguments);
		Enumeration enumerator = arguments.elements();
		while (enumerator.hasMoreElements()) {
			stream.write(enumerator.nextElement());
		}
	}
	
	protected void computeByteArray(OSCJavaToByteArrayConverter stream) {
		computeAddressByteArray(stream);
		computeArgumentsByteArray(stream);
		byteArray = stream.toByteArray();
	}
}