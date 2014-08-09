package org.mcjug.aameetingmanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CheckBox;

public final class ServiceConfig {

	static final String TAG = "MainActivity";
	static final String cbBootName = "checkBoxBoot";
	static final String cbLoadAppName = "checkBoxAppLoad";
	static final String radioServiceRunMode = "radioServiceRunMode";
	private boolean mCheckboxBootIsChecked, mCheckboxAppLoadIsChecked;
	private boolean activeScheduleReceiver;
	
	
	public static final String LOADEDSTRING = "loadedString";
	
	public enum ServiceRunModes {
		RUN_ONCE(0), ONE_MIN(1), FIVE_MIN(5);
		
		private final int mMode;
		
		ServiceRunModes (int serviceMode) {
			mMode = serviceMode;
		}
		
		public int getServiceRunMode() {
			return mMode;
		}
	}
	
	public ServiceRunModes serviceMode;
	
	public int getServiceRunMode() {
		return serviceMode.mMode;
	}
	
	public void setServiceRunMode(ServiceRunModes serviceRunMode) {
		this.serviceMode = serviceRunMode;
	}

	public boolean isCheckboxBootChecked() {
		return mCheckboxBootIsChecked;
	}

	public void setCheckboxBootIsChecked(boolean isChecked) {
		this.mCheckboxBootIsChecked = isChecked;
	}

	public boolean isCheckboxAppLoadChecked() {
		return mCheckboxAppLoadIsChecked;
	}

	public void setCheckboxAppLoadChecked(boolean isChecked) {
		this.mCheckboxAppLoadIsChecked = isChecked;
	}

	public boolean isActiveScheduleReceiver() {
		return activeScheduleReceiver;
	}

	public void setActiveScheduleReceiver(boolean activeScheduleReceiver) {
		this.activeScheduleReceiver = activeScheduleReceiver;
	}

	public ServiceConfig(Context context) {
		loadConfig(context);
	}
	
	ServiceConfig(CheckBox checkboxBoot,CheckBox checkboxAppLoad, ServiceRunModes serviceRunMode) {
		this.mCheckboxBootIsChecked = checkboxBoot.isChecked();
		this.mCheckboxAppLoadIsChecked = checkboxAppLoad.isChecked();
		serviceMode = serviceRunMode;
	}
	
	public void saveConfig(Context context) {
		SharedPreferences.Editor editor;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
		editor.putBoolean (cbBootName, mCheckboxBootIsChecked);
		editor.putBoolean (cbLoadAppName, mCheckboxAppLoadIsChecked);
		editor.putInt(radioServiceRunMode, serviceMode.getServiceRunMode());
		editor.commit();
		Log.v(TAG, "Config saveConfig " + mCheckboxBootIsChecked + "/" + mCheckboxAppLoadIsChecked + "/" + serviceMode.name());
	}
	
	public void loadConfig(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		mCheckboxBootIsChecked= preferences.getBoolean(cbBootName, false);
		mCheckboxAppLoadIsChecked = preferences.getBoolean(cbLoadAppName, false);
		int modeValue =  preferences.getInt(radioServiceRunMode, 1);
		switch (modeValue) {
			case 0: serviceMode = ServiceRunModes.RUN_ONCE; break;	
			case 1: serviceMode = ServiceRunModes.ONE_MIN; break;
			default: serviceMode = ServiceRunModes.FIVE_MIN; break;
		}
		Log.v(TAG, "Config loadConfig " + mCheckboxBootIsChecked + "/" + mCheckboxAppLoadIsChecked + "/" + serviceMode.name());
	}
}
