package org.mcjug.aameetingmanager.scheduleservice;


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
	static final String serviceSourceType = "serviceSourceType";
	private boolean mCheckboxBootIsChecked, mCheckboxAppLoadIsChecked;
	private boolean activeScheduleReceiver;
	private static final String URL_Simple_Message = "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json";
	private static final String URL_AA_Meeting = "https://mcasg.org/meetingfinder/get_meeting_by_id/?meeting_id=221727";
	private static final String URL_AA_Meeting_Type = "https://mcasg.org/meetingfinder/api/v1/meeting_type/?format=json";
	private String URL;
	public static int bufferSize = 4*1024;
	public static final String LOADEDSTRING = "loadedString";
	public static final String RESULT = "result";


	/************************************************/

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

	/************************************************/

	public enum DataSourceTypes { 
		AA_MEETING (1), AA_MEETING_TYPE (2), SIMPLE_MESSAGE (0); 

		private final int mDataSourceType;

		DataSourceTypes (int dataSourceType) {
			mDataSourceType = dataSourceType;
		}

		public int getDataSourceType() {
			return mDataSourceType;
		}
	}

	private DataSourceTypes dataSourceType; 

	public DataSourceTypes getHandlerDataSourceType() {
		return dataSourceType;
	}

	public int getHandlerDataSourceTypeInt() {
		return dataSourceType.getDataSourceType();
	}

	public void setHandlerDataSourceType(DataSourceTypes serviceDataSourceType) {
		this.dataSourceType = serviceDataSourceType;
	}

	public void setHandlerDataSourceType(int dataSourceType) {
		switch (dataSourceType) {
		case 0: this.dataSourceType =  DataSourceTypes.SIMPLE_MESSAGE;                  break;
		case 1: this.dataSourceType = DataSourceTypes.AA_MEETING ;                              break;
		default: this.dataSourceType = DataSourceTypes.AA_MEETING_TYPE ;                break;
		}
	}

	/************************************************/

	public String getURL() {
		return this.URL;
	}

	public void setURL(String url) {
		this.URL = url;
	}


	/************************************************/

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

	ServiceConfig(CheckBox checkboxBoot,CheckBox checkboxAppLoad, ServiceRunModes serviceRunMode, DataSourceTypes serviceSourceType) {
		this.mCheckboxBootIsChecked = checkboxBoot.isChecked();
		this.mCheckboxAppLoadIsChecked = checkboxAppLoad.isChecked();
		this.serviceMode = serviceRunMode;
		this.dataSourceType = serviceSourceType;
	}

	public void saveConfig(Context context) {
		SharedPreferences.Editor editor;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
		editor.putBoolean (cbBootName, mCheckboxBootIsChecked);
		editor.putBoolean (cbLoadAppName, mCheckboxAppLoadIsChecked);
		editor.putInt(radioServiceRunMode, serviceMode.getServiceRunMode());
		editor.putInt(serviceSourceType, dataSourceType.getDataSourceType());
		editor.commit();
		Log.v(TAG, "Config saveConfig " + mCheckboxBootIsChecked + "/" + mCheckboxAppLoadIsChecked + 
				"/" + serviceMode.name() + " /" + dataSourceType);

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
		int sourceType = preferences.getInt(serviceSourceType, 0);
		switch (sourceType) {
		case 0: this.dataSourceType =  DataSourceTypes.SIMPLE_MESSAGE;
		URL = URL_Simple_Message; break;
		case 1: this.dataSourceType = DataSourceTypes.AA_MEETING ;
		URL = URL_AA_Meeting; break;
		default: this.dataSourceType = DataSourceTypes.AA_MEETING_TYPE ;
		URL = URL_AA_Meeting_Type;  break;
		}               
		Log.v(TAG, "Config saveConfig " + mCheckboxBootIsChecked + "/" + mCheckboxAppLoadIsChecked + 
				"/" + serviceMode.name() + " /" + dataSourceType);
	}
}