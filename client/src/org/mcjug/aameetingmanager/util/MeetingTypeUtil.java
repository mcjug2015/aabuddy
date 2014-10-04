package org.mcjug.aameetingmanager.util;

import org.mcjug.meetingfinder.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.Log;

public class MeetingTypeUtil {
	private static final String TAG = "MeetingTypeUtil";
	
	public static boolean getMeetingTypeShowPref (Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String key = context.getString(R.string.meetingTypePreferenceKey);
		Log.v(TAG, "getMeetingTypeShowPref getting " + key);
		try {
			return prefs.getBoolean(key, true);
		}
		catch (Exception ex) {
			Log.e(TAG, "getMeetingTypeShowPref Exception on getting boolean " + ex.getMessage());
		}
		String value = prefs.getString(key, "");
		Log.v(TAG, "getMeetingTypeShowPref value: >" + key + "<"); 
		return (value != "false");
	}
	
	public static void setMeetingTypeShowPref (Context context, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		Log.v(TAG, "setMeetingTypeShowPref setting " + context.getString(R.string.meetingTypePreferenceKey));
		editor.putBoolean(context.getString(R.string.meetingTypePreferenceKey), value);
		editor.commit();
	}
	
}
