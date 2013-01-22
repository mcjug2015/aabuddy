package org.mcjug.aameetingmanager.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.R;

import android.content.Context;
import android.util.Log;
import android.widget.SimpleAdapter;

public class MeetingListUtil {
    private static final String TAG = MeetingListUtil.class.getSimpleName();
	
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String DAY_OF_WEEK = "day_of_week";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	private static final String TIME_RANGE = "time_range";
	private static final String ADDRESS = "address";
	private static final String DISTANCE = "distance";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";
	
    private static final String[] FROM = new String[] {NAME,  DAY_OF_WEEK, TIME_RANGE, ADDRESS, DISTANCE, DESCRIPTION};
    private static final int[] TO = new int[] {R.id.meetingName, R.id.meetingDay, R.id.meetingTime, R.id.meetingAddress, R.id.meetingDistance, R.id.meetingDescription};

	public static JSONArray getMeetingList(HttpResponse httpResponse) throws Exception {
		StringBuilder builder = new StringBuilder();
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} finally {
				inputStream.close();
			}
		}
		
		String jsonStr = builder.toString();		
	    Log.d(TAG, "Meeting list: " + jsonStr);
	    
	    JSONArray jsonMeetings = new JSONArray();
	    if (jsonStr != null) {
		    JSONObject jsonObj = new JSONObject(jsonStr);
		    jsonMeetings = jsonObj.getJSONArray("objects");
		}

		return jsonMeetings;
	}
	
	public static SimpleAdapter getListAdapter(Context context, String meetingsJson) throws Exception {
		List<HashMap<String, String>> list = getListItems(context, meetingsJson);
		SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.meeting_list_row, FROM, TO);
		return adapter;
	}
	
	private static List<HashMap<String, String>> getListItems(Context context, String meetingsJson) throws Exception {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		if (meetingsJson != null) {
			JSONArray meetingListJson = new JSONArray(meetingsJson);
			JSONObject meetingJson;
			for (int i = 0; i < meetingListJson.length(); i++) {
				meetingJson = meetingListJson.getJSONObject(i);
				
				HashMap<String, String> map = new HashMap<String, String>();
				
				map.put(NAME, meetingJson.getString(NAME));
				map.put(DESCRIPTION, meetingJson.getString(DESCRIPTION));
				
				String[] daysOfWeek = context.getResources().getStringArray(R.array.daysOfWeek);
				int dayOfWeek = meetingJson.getInt(DAY_OF_WEEK);
				map.put(DAY_OF_WEEK, daysOfWeek[dayOfWeek]);
					
				String startTime = meetingJson.getString(START_TIME).substring(0, 5);
				String endTime = meetingJson.getString(END_TIME).substring(0, 5);
				map.put(TIME_RANGE, startTime + " - " +  endTime);
				
				map.put(ADDRESS, meetingJson.getString(ADDRESS));
				
				double distance = Double.parseDouble(meetingJson.getString(DISTANCE));
				map.put(DISTANCE, String.format("%.2f", distance));
				
				double latitude = meetingJson.getDouble(LATITUDE);
				map.put(LATITUDE, String.format("%.3f", latitude));
				
				double longitude = meetingJson.getDouble(LONGITUDE);
				map.put(LONGITUDE, String.format("%.3f", longitude));
				
				list.add(map);
			}
		}
		return list;
	}
}
