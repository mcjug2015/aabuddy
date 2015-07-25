package org.mcjug.aameetingmanager;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.meeting.GetMeetingTypesTask;
import org.mcjug.aameetingmanager.meeting.MeetingListResults;
import org.mcjug.aameetingmanager.meeting.MeetingType;
import org.mcjug.meetingfinder.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Testing first git commit.
//Test marias first git commit.
//David's first comment
//Mike was here :)
//Victor's other comment.
public class AAMeetingApplication extends Application {
	private static final String TAG = AAMeetingApplication.class.getSimpleName();	
	private MeetingListResults meetingListResults;
	private List<Integer> meetingNotThereList = null;
	private List<MeetingType> meetingTypes = new ArrayList<MeetingType>();

	private static final String meetingTypesDefault = 
		"{\"meta\": {\"limit\": 20, \"next\": null, \"offset\": 0, \"previous\": null, \"total_count\": 14}, \"objects\": [{\"description\": \"Initial speakers kick off the group's discussion of matters brought up by the speakers.\", \"id\": 3, \"name\": \"Speaker/Discussion\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/3\", \"short_name\": \"SD\"}, {\"description\": \"Everyone is Welcome. The group has voted to allow all comers to attend the meeting, although usually only Alcoholics speak.\", \"id\": 1, \"name\": \"Open\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/1\", \"short_name\": \"O\"}, {\"description\": \"The group has voted attendance is limited to alcoholics only.\", \"id\": 2, \"name\": \"Closed\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/2\", \"short_name\": \"C\"}, {\"description\": \"A Big Book discussion meeting.\", \"id\": 4, \"name\": \"Big Book\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/4\", \"short_name\": \"BB\"}, {\"description\": \"This type of meeting focuses on specific material from the Big Book and the book Twelve Steps and Twelve Traditions (12 & 12).\", \"id\": 5, \"name\": \"Big Book Step Study\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/5\", \"short_name\": \"BS\"}, {\"description\": \"A chairperson is designated for a meeting; the chairperson speaks briefly about their experience, strength and hope. Following this the chairperson may suggest a topic for sharing by other attendees.\", \"id\": 6, \"name\": \"Discussion\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/6\", \"short_name\": \"D\"}, {\"description\": \"These are chairperson led meetings focusing on the monthly publication of the Grapevine.\", \"id\": 7, \"name\": \"Grapevine\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/7\", \"short_name\": \"GV\"}, {\"description\": \"A chairperson leads the meeting in a discussion of material drawn from the AA publication Living Sober.\", \"id\": 8, \"name\": \"Living Sober\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/8\", \"short_name\": \"LS\"}, {\"description\": \"The Promises of Alcoholics Anonymous are on pages 82 and 83 of the Big Book. These are the focus of a chairperson led discussion.\", \"id\": 9, \"name\": \"Promises\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/9\", \"short_name\": \"P\"}, {\"description\": \"A recent addition to the AA library, Reflections provides daily material for meditation and prayer. Meetings of this type focus on this daily practice.\", \"id\": 10, \"name\": \"Reflections\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/10\", \"short_name\": \"R\"}, {\"description\": \"Using the book Twelve Steps and Twelve Traditions (12 & 12) the meeting focuses on the Steps.\", \"id\": 11, \"name\": \"Step\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/11\", \"short_name\": \"S\"}, {\"description\": \"The meeting focuses on both the 12 Steps and the 12 Traditions. The formats of these meetings vary. Please investigate on your own.\", \"id\": 12, \"name\": \"Step / Tradition\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/12\", \"short_name\": \"ST\"}, {\"description\": \"The second half of the 12 and 12 are the focus of these meetings.\", \"id\": 13, \"name\": \"Tradition\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/13\", \"short_name\": \"T\"}, {\"description\": \"Any of the above meetings may, by group conscience, be designated as a Beginner meeting. These meetings are usually led by more experienced group members with the primary focus on introducing new members to AA.\", \"id\": 14, \"name\": \"Beginner\", \"resource_uri\": \"/meetingfinder/api/v1/meeting_type/14\", \"short_name\": \"BG\"}]}";
	
	// instance 
	private static AAMeetingApplication instance = null;

	@Override
	public void onCreate() {
		super.onCreate();

		LocationFinder locationTask = new LocationFinder(this, null);
		locationTask.requestLocation();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String meetingTypesJsonStr = prefs.getString(getString(R.string.meetingTypesKey), null);
		if (meetingTypesJsonStr == null) {
			Editor editor = prefs.edit();
			editor.putString(getString(R.string.meetingTypesKey), meetingTypesDefault);			
			editor.commit();	
		} 
		
		try {
			setMeetingTypes();
		} catch (Exception e) {
			Log.d(TAG, "Exception setting meeting types", e);
		}
		
		new GetMeetingTypesTask(this).execute();
		
		// set instance for our static accessor
		instance = this;
	}

	public MeetingListResults getMeetingListResults() {
		return meetingListResults; 
	}

	public void setMeetingListResults(MeetingListResults meetingListResults) {
		this.meetingListResults = meetingListResults;
	}

	public List<MeetingType> getMeetingTypes() {
		return meetingTypes;
	}
	
	public void setMeetingTypes() throws Exception {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String meetingTypesJsonStr = prefs.getString(getString(R.string.meetingTypesKey), null);
		
		JSONObject jsonObj = new JSONObject(meetingTypesJsonStr);
		JSONArray meetingTypesJson = jsonObj.getJSONArray("objects");
		if (meetingTypesJson != null) {
			List<MeetingType> types = new ArrayList<MeetingType>();
			
			JSONObject meetingTypeJson;
			MeetingType meetingType;
			for (int i = 0; i < meetingTypesJson.length(); i++) {
				meetingTypeJson = meetingTypesJson.getJSONObject(i);

				meetingType = new MeetingType();
				meetingType.setId(meetingTypeJson.getInt("id"));
				meetingType.setName(meetingTypeJson.getString("name"));
				meetingType.setDescription(meetingTypeJson.getString("description"));
				meetingType.setResourceUri(meetingTypeJson.getString("resource_uri"));
				meetingType.setShortName(meetingTypeJson.getString("short_name"));

				types.add(meetingType);
			}

			Collections.sort(types, new Comparator<MeetingType>() {
				@Override
				public int compare(MeetingType type, MeetingType type1) {
					return type.getName().compareTo(type1.getName());
				}
			});
			
			meetingTypes = types;
		}	
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
