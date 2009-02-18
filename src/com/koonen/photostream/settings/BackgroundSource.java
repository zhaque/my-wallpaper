package com.koonen.photostream.settings;

import com.koonen.utils.Enumeration;

/**
 * 
 * @author dryganets
 * 
 */
public class BackgroundSource extends Enumeration {

	public static final BackgroundSource RANDOM = new BackgroundSource(
			"random", "random");
	public static final BackgroundSource PERSONAL = new BackgroundSource(
			"personal", "personal");
	public static final BackgroundSource FAVORITES = new BackgroundSource(
			"favorites", "favorites");

	static {
		add(BackgroundSource.class, RANDOM);
		add(BackgroundSource.class, PERSONAL);
		add(BackgroundSource.class, FAVORITES);
	}

	public static BackgroundSource valueOf(String name) {
		BackgroundSource result = null;
		result = (BackgroundSource) valueOf(BackgroundSource.class, name);
		return result;
	}

	public BackgroundSource(String name, String value) {
		super(name, value);
	}

}
