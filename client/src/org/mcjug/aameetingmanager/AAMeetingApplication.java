package org.mcjug.aameetingmanager;

import android.app.Application;

public class AAMeetingApplication extends Application {
	private String meetingListData;
	
	public String getMeetingListData() {
		return meetingListData; 
	}

	public void setMeetingListData(String meetingListData) {
		this.meetingListData = meetingListData;
	}

}
