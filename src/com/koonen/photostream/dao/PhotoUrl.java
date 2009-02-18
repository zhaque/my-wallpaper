package com.koonen.photostream.dao;

/**
 * 
 * @author Glick
 *
 */
public class PhotoUrl {
	private int id;
	private String url;

	public PhotoUrl(int id, String url) {
		super();
		this.id = id;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
