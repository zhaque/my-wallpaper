package com.koonen.photostream.settings;

import com.koonen.photostream.effects.TypeEffect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

/**
 * 
 * @author dryganets
 * 
 */
public class UserPreferences {
	static final String NAME = "user-settings";

	public static final String NETWORK_ENABLED_KEY = "photostream.enable-network";
	public static final String NETWORK_NAME_KEY = "photostream.networkName";

	public static final String NETWORK_USER_NAME_KEY = "photostream.networkUsername";
	// public static final String NETWORK_PASSWORD_KEY =
	// "photostream.networkPassword";

	public static final String ROTATION_ENABLED_KEY = "photostream.enable-rotation";

	public static final String ROTATION_SCHEDULE_KEY = "photostream.rotation-schedule";
	public static final String ROTATION_SOURCE_KEY = "photostream.rotation_background_source";
	public static final String ROTATION_NOTIFICATION_KEY = "photostream.rotation_background_notification";
	public static final String ROTATION_MY_TAGS_KEY = "photostream.my_tags";

	public static final String EFFECTS_KEY = "photostream.effects";
	public static final String GROUP_NAME_KEY = "photostream.groupName";

	public static final String IMAGES_PER_REQUEST = "photostream.images_per_request";
	public static final String CROP_WALLPAPER = "photostream.enable-crop";

	private Context context;
	SharedPreferences preferences;

	public UserPreferences(Context context) {
		this.context = context;
		preferences = this.context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);
	}

	public boolean isNetworkEnabled() {
		return preferences.getBoolean(NETWORK_ENABLED_KEY, false);
	}

	// public String getPassword() {
	// return preferences.getString(NETWORK_PASSWORD_KEY, "");
	// }

	public String getUserName() {
		return preferences.getString(NETWORK_USER_NAME_KEY, "");
	}

	public Network getNetwork() {
		return Network.valueOf(preferences
				.getString(NETWORK_NAME_KEY, "flickr"));
	}

	public boolean isRotationEnabed() {
		return preferences.getBoolean(ROTATION_ENABLED_KEY, false);
	}

	public long getRotationSchedule() {
		return Long
				.parseLong(preferences.getString(ROTATION_SCHEDULE_KEY, "0"));
	}

	public BackgroundSource getRotationBackgroundSource() {
		return BackgroundSource.valueOf(preferences.getString(
				ROTATION_SOURCE_KEY, ""));
	}

	public boolean isRotationNotificationEnabled() {
		return preferences.getBoolean(ROTATION_NOTIFICATION_KEY, false);
	}

	public TypeEffect getTypeEffect() {
		return TypeEffect.valueOf(preferences.getString(EFFECTS_KEY, "random"));
	}

	public int getImagesPerRequest() {
		return Integer.parseInt(preferences.getString(IMAGES_PER_REQUEST, "6"));
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		preferences.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		preferences.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public String getEmail() {
		return "22dsse@gmail.com";
	}

	public String getGroup() {
		return preferences.getString(GROUP_NAME_KEY, "344828@N24");
	}

	public String getMyTags() {
		return preferences.getString(ROTATION_MY_TAGS_KEY, "");
	}

	public boolean isCropWallpaper() {
		return preferences.getBoolean(CROP_WALLPAPER, false);
	}
}
