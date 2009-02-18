package com.koonen.photostream.api.flickr;

import com.koonen.photostream.api.User;

public class Auth {
	private String token;
	private Perms perms;
	private User user;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Perms getPerms() {
		return perms;
	}

	public void setPerms(Perms perms) {
		this.perms = perms;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}