package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class AAMeetingApplication extends Application {
	private List<Meeting> meetings;	
    
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

	public List<Meeting> getMeetings() {
		return meetings; 
	}

	public void addMeetings(List<Meeting> meetings) {
		if (this.meetings == null) {
			this.meetings = new ArrayList<Meeting>();
		}	
		this.meetings.addAll(meetings);
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
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
