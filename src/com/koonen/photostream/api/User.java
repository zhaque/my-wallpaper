package com.koonen.photostream.api;

/**
 * 
 * @author dryganets
 * 
 */
public class User {
	String id;

	public User(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
