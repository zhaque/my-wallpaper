package com.koonen.photostream.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.koonen.photostream.R;

/**
 * 
 * @author dryganets
 * 
 */
public class UserSettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(UserPreferences.NAME);
		addPreferencesFromResource(R.xml.user_preferences);
	}

	/**
	 * Starts the PreferencesActivity for the specified user.
	 * 
	 * @param context
	 *            The application's environment.
	 */
	static void show(Context context) {
		final Intent intent = new Intent(context, UserSettingsActivity.class);
		context.startActivity(intent);
	}
}
