package org.mcjug.aameetingmanager;

import android.app.Application;

public class AAMeetingApplication extends Application {
	private String meetingListData;	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		LocationFinder locationTask = new LocationFinder(this, null);
		locationTask.requestLocation();
	}

	public String getMeetingListData() {
		return meetingListData; 
	}

	public void setMeetingListData(String meetingListData) {
		this.meetingListData = meetingListData;
	}

}
