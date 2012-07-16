package com.enjin.officialplugin;

public class UnknownPacketException extends Exception {
	private static final long serialVersionUID = -3978137019615080819L;

	
	public UnknownPacketException(int read) {
		super("Unknown packet recieved: " + read);
	}
}
