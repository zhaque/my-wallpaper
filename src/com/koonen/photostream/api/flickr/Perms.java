package com.koonen.photostream.api.flickr;

import com.koonen.utils.Enumeration;

public class Perms extends Enumeration {

	public static final Perms NONE = new Perms("none", "none");
	public static final Perms READ = new Perms("read", "read");
	public static final Perms WRITE = new Perms("write", "write");
	public static final Perms DELETE = new Perms("delete", "delete");

	static {
		add(Perms.class, NONE);
		add(Perms.class, READ);
		add(Perms.class, WRITE);
		add(Perms.class, DELETE);
	}
	
	public Perms(String name, String value) {
		super(name, value);
	}
	
	public static Perms valueOf(String name) {
		Perms result = null;
		result = (Perms) valueOf(Perms.class, name);
		return result;
	}
}
