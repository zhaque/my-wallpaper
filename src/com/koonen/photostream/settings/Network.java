package com.koonen.photostream.settings;

import com.koonen.utils.Enumeration;

/**
 * 
 * @author dryganets
 * 
 */
public class Network extends Enumeration {
	public static final Network FLICKR = new Network("Flickr", "flickr");

	static {
		add(Network.class, FLICKR);
	}

	public static Network valueOf(String name) {
		Network result = null;
		result = (Network) valueOf(Network.class, name);
		return result;
	}

	public Network(String name, String value) {
		super(name, value);
	}

}