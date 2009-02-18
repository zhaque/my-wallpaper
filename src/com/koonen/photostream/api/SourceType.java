package com.koonen.photostream.api;

import com.koonen.utils.Enumeration;

public class SourceType extends Enumeration {

	public static final SourceType FLICKR = new SourceType("flickr", "F");

	static {
		add(SourceType.class, FLICKR);
	}
	
	public SourceType(String name, String value) {
		super(name, value);
	}

	public static SourceType valueOf(String name) {
		SourceType result = null;
		result = (SourceType) valueOf(SourceType.class, name);
		return result;
	}
}
