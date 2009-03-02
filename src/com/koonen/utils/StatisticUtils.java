package com.koonen.utils;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.koonen.photostream.ActivityConstants;

/**
 * 
 * @author dryganets
 * 
 */
public class StatisticUtils {
	public static void onStartSession(Context context) {
		FlurryAgent.onStartSession(context, ActivityConstants.FLURRY_KEY);
	}

	public static void onEndSession() {
		FlurryAgent.onEndSession();
	}

}
