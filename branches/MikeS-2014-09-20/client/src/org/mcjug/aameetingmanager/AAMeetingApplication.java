package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import org.mcjug.aameetingmanager.meeting.GetMeetingTypesTask;
import org.mcjug.aameetingmanager.meeting.MeetingListResults;
import org.mcjug.aameetingmanager.meeting.MeetingType;

import android.app.Application;
import android.util.SparseArray;

public class AAMeetingApplication extends Application {
	private MeetingListResults meetingListResults;
	private List<Integer> meetingNotThereList = null;
	private SparseArray<MeetingType> meetingTypes = new SparseArray<MeetingType>();

	// instance 
	private static AAMeetingApplication instance = null;

	@Override
	public void onCreate() {
		super.onCreate();

		LocationFinder locationTask = new LocationFinder(this, null);
		locationTask.requestLocation();

		new GetMeetingTypesTask(this, meetingTypes).execute();

		// set instance for our static accessor
		instance = this;
	}

	public MeetingListResults getMeetingListResults() {
		return meetingListResults; 
	}

	public void setMeetingListResults(MeetingListResults meetingListResults) {
		this.meetingListResults = meetingListResults;
	}

	public SparseArray<MeetingType> getMeetingTypes() {
		return meetingTypes;
	}

	public void setMeetingTypes(SparseArray<MeetingType> meetingTypes) {
		this.meetingTypes = meetingTypes;
	}

	/**
	 * Convenient accessor to get context, so that context doesn't need to be passed around
	 */
	public static AAMeetingApplication getInstance() {
		if (instance == null)
			throw new IllegalStateException("Application not created yet!");

		return instance;
	}

	public void addToMeetingNotThereList(int meetingId) {
		if (meetingNotThereList == null) {
			meetingNotThereList = new ArrayList<Integer>();
		}

		meetingNotThereList.add(meetingId);
	}

	public List<Integer> getMeetingNotThereList() {
		return meetingNotThereList;
	}
}