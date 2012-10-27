package org.mcjug.aameetingmanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AdminPrefsActivity extends PreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Admin preferences
		addPreferencesFromResource(R.xml.adminprefs);
	}
}

