package datanetwork.javaosc;

import java.util.Date;

public interface OSCListener {
	
	/**
	 * Accepts an incoming OSCMessage.
	 * @param message  The message to execute.
	 */
	public void acceptMessage(OSCMessage message);

}
