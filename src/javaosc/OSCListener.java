package datanetwork.javaosc;

import java.util.Date;

/**
 * <p>Interface for things that listen for incoming OSCMessages.</p>
 * <p>Part of <a target="_blank" href="http://sensestage.hexagram.ca">Sense/Stage</a></p>
 * <p>Based on Java OSC.
 * Copyright (C) 2003-2006, C. Ramakrishnan / Illposed Software.
 * All rights reserved.<p>
 */
public interface OSCListener {
	
	/**
	 * Accepts an incoming OSCMessage.
	 * @param message  The message to execute.
	 */
	public void acceptMessage(OSCMessage message);

}
