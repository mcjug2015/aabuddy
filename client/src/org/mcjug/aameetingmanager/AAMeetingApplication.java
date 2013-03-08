package org.mcjug.aameetingmanager;

import android.app.Application;

public class AAMeetingApplication extends Application {
	private MeetingListResults meetingListResults;	
    
	//instance 
    private static AAMeetingApplication instance = null;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		LocationFinder locationTask = new LocationFinder(this, null);
		locationTask.requestLocation();
		
	    //set instance for our static accessor
        instance = this;
	}

	public MeetingListResults getMeetingListResults() {
		return meetingListResults; 
	}

	public void setMeetingListResults(MeetingListResults meetingListResults) {
		this.meetingListResults = meetingListResults;
	}
	
    /**
     * Convenient accessor to get context, so that context doesn't need to be passed around
     */
    public static AAMeetingApplication getInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");

        return instance;
    }
    
}
