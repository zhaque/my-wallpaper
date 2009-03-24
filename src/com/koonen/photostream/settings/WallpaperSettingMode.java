package com.koonen.photostream.settings;

/**
 * 
 * @author glick
 * 
 */
public enum WallpaperSettingMode {

	ORIGINAL_MODE("original"), STRETCH_MODE("stretch"), AUTO_MODE("auto");

	private final String name;

	private WallpaperSettingMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
