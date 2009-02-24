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
	public static final BackgroundSource MY_TAGS = new BackgroundSource(
			"my_tags", "my_tags");
	public static final BackgroundSource FAVORITES = new BackgroundSource(
			"favorites", "favorites");
	public static final BackgroundSource FILE_SYSTEM_INTERNAL = new BackgroundSource(
			"telephone", "telephone");
	public static final BackgroundSource FILE_SYSTEM_EXTERNAL = new BackgroundSource(
			"sdcard", "sdcard");

	static {
		add(BackgroundSource.class, RANDOM);
		add(BackgroundSource.class, PERSONAL);
		add(BackgroundSource.class, MY_TAGS);
		add(BackgroundSource.class, FAVORITES);
		add(BackgroundSource.class, FILE_SYSTEM_INTERNAL);
		add(BackgroundSource.class, FILE_SYSTEM_EXTERNAL);
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
