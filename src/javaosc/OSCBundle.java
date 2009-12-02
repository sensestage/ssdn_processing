package datanetwork.javaosc;

/**
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 *
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.<p>
 */

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import datanetwork.javaosc.utility.*;

public class OSCBundle extends OSCPacket {

	public static final BigInteger SECONDS_FROM_1900_to_1970 =
		new BigInteger("2208988800");
		
	protected Date timestamp;
	protected Vector<OSCPacket> packets;

	public OSCBundle() {
		this(null, new Date(System.currentTimeMillis()));
	}
	
	public OSCBundle(Date timestamp) {
		this(null, timestamp);
	}

	public OSCBundle(OSCPacket[] packets) {
		this(packets, new Date(System.currentTimeMillis()));
	}

	public OSCBundle(OSCPacket[] packets, Date timestamp) {
		super();
		if (null != packets) {
			this.packets = new Vector<OSCPacket>(packets.length);
			for (int i = 0; i < packets.length; i++) {
				this.packets.add(packets[i]);
			}
		} else
			this.packets = new Vector<OSCPacket>();
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public void addPacket(OSCPacket packet) {
		packets.add(packet);
	}
	
	public OSCPacket[] getPackets() {
		OSCPacket[] packetArray = new OSCPacket[packets.size()];
		packets.toArray(packetArray);
		return packetArray;
	}

	protected void computeTimeTagByteArray(OSCJavaToByteArrayConverter stream) {
		if ((null == timestamp) || (timestamp == new Date(System.currentTimeMillis()))) {
			stream.write((int) 0);
			stream.write((int) 1);
			return;
		}
		
		long millisecs = timestamp.getTime();
		long secsSince1970 = (long) (millisecs / 1000);
		long secs = secsSince1970 + SECONDS_FROM_1900_to_1970.longValue();
		//the next line was cribbed from jakarta commons-net's NTP TimeStamp code
		long fraction = ((millisecs % 1000) * 0x100000000L) / 1000;
		
		stream.write((int) secs);
		stream.write((int) fraction);
	}
	
	protected void computeByteArray(OSCJavaToByteArrayConverter stream) {
		stream.write("#bundle");
		computeTimeTagByteArray(stream);
		Enumeration enumerator = packets.elements();
		OSCPacket nextElement;
		byte[] packetBytes;
		while (enumerator.hasMoreElements()) {
			nextElement = (OSCPacket) enumerator.nextElement();
			packetBytes = nextElement.getByteArray();
			stream.write(packetBytes.length);
			stream.write(packetBytes);
		}
		byteArray = stream.toByteArray();
	}
	
	public String toString() {
		String out = "";
		for(OSCPacket packet : packets) {
			out += packet.toString()+"\n";
		}
		return out;
	}

}