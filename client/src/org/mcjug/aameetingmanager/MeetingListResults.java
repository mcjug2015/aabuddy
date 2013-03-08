package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

public class MeetingListResults {
	private int totalMeetingCount = 0;
	private List<Meeting> meetings = new ArrayList<Meeting>();

	public int getTotalMeetingCount() {
		return totalMeetingCount;
	}
	
	public void setTotalMeetingCount(int totalMeetingCount) {
		this.totalMeetingCount = totalMeetingCount;
	}
	
	public List<Meeting> getMeetings() {
		return meetings;
	}
	
	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}
	
	

}
