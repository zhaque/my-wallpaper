package com.koonen.photostream.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

import com.koonen.photostream.R;
import com.koonen.utils.StatisticUtils;

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

		EditTextPreference networkUserName = (EditTextPreference) findPreference(UserPreferences.NETWORK_USER_NAME_KEY);

		initRotationSourcesList(networkUserName.getText());

		//final OnPreferenceChangeListener networkUserNameListener = networkUserName
				//.getOnPreferenceChangeListener();
		networkUserName
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object value) {
						String text = (String) value;
						initRotationSourcesList(text);
						return true;
					}
				});
	}

	private void initRotationSourcesList(String networkName) {
		ListPreference rotationSourcePreference = (ListPreference) findPreference(UserPreferences.ROTATION_SOURCE_KEY);
		if (networkName == null || networkName.trim() == "") {

			rotationSourcePreference
					.setEntries(R.array.photo_source_type_safe_names);
			rotationSourcePreference
					.setEntryValues(R.array.photo_source_type_safe_values);
		} else {
			rotationSourcePreference
					.setEntries(R.array.photo_source_type_names);
			rotationSourcePreference
					.setEntryValues(R.array.photo_source_type_values);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		StatisticUtils.onStartSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		StatisticUtils.onEndSession();
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
