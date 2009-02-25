package com.koonen.utils;

import android.content.res.Resources;

import com.koonen.photostream.R;

/**
 * 
 * @author glick
 * 
 */
public class GroupUtils {

	public static String getGroupName(Resources resources, String group) {
		String[] groupNames = resources.getStringArray(R.array.group_names);
		String[] groupValues = resources.getStringArray(R.array.group_values);

		for (int i = 0; i < groupValues.length; i++) {
			if (groupValues[i].equals(group)) {
				return groupNames[i];
			}
		}

		return null;
	}

}
