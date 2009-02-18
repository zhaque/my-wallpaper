package com.koonen.utils;

import android.app.Activity;
import android.content.res.Configuration;

/**
 * 
 * @author dryganets
 * 
 */
public class ConfigurationReader {
	private Activity activity;

	public ConfigurationReader(Activity activity) {
		super();
		this.activity = activity;
	}

	public Configuration getConfiguration() {
		Configuration config = activity.getResources().getConfiguration();
		return config;
	}

}
