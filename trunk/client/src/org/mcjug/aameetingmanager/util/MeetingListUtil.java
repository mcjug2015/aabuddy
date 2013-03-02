package org.mcjug.aameetingmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.Meeting;
import org.mcjug.aameetingmanager.R;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

public class MeetingListUtil {
    private static final String TAG = MeetingListUtil.class.getSimpleName();
	
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String DAY_OF_WEEK = "day_of_week";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	private static final String ADDRESS = "address";
	private static final String DISTANCE = "distance";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";
	
	public static List<Meeting> getMeetingList(Context context, HttpResponse httpResponse) throws Exception {
		String jsonStr = HttpUtil.getContent(httpResponse);		
	    Log.d(TAG, "Meeting list: " + jsonStr);
	    
		List<Meeting> meetings = new ArrayList<Meeting>();
	    if (jsonStr != null) {
		    JSONObject jsonObj = new JSONObject(jsonStr);
		    JSONArray meetingsJson = jsonObj.getJSONArray("objects");
		    
		    if (meetingsJson != null) {
				JSONObject meetingJson;
				Meeting meeting;
				for (int i = 0; i < meetingsJson.length(); i++) {
					meetingJson = meetingsJson.getJSONObject(i);
					
					meeting = new Meeting();
					meeting.setName(meetingJson.getString(NAME));
					meeting.setDescription(meetingJson.getString(DESCRIPTION));
				
					String[] daysOfWeek = context.getResources().getStringArray(R.array.daysOfWeekLong);
					int dayOfWeek = meetingJson.getInt(DAY_OF_WEEK);
					meeting.setDayOfWeek(daysOfWeek[dayOfWeek - 1]);
						
					String startTime = meetingJson.getString(START_TIME).substring(0, 5);
					String endTime = meetingJson.getString(END_TIME).substring(0, 5);
					meeting.setTimeRange(startTime + " - " +  endTime);
					
					meeting.setAddress(meetingJson.getString(ADDRESS));
					meeting.setDistance(String.format("%.2f", Double.parseDouble(meetingJson.getString(DISTANCE))));
					
					meeting.setLatitude(String.format("%.2f", meetingJson.getDouble(LATITUDE)));
					meeting.setLongitude(String.format("%.2f", meetingJson.getDouble(LONGITUDE)));
					
					meetings.add(meeting);
				}
		    }	
		}
		
		return meetings;
	}
	
	public static String getUniqueDeviceId(Context context) {
		StringBuilder id = new StringBuilder();
		
		// 64-bit number as a hex string (15 characters)
		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID); 
		if (androidId != null) {
			id.append(androidId);
		}
		
		// 15 digits
		String deviceId = "35" + // we make this look like a valid IMEI
				Build.BOARD.length() %10 + Build.BRAND.length() %10 +
				Build.CPU_ABI.length() %10 + Build.DEVICE.length() %10 +
				Build.DISPLAY.length() %10 + Build.HOST.length() %10 +
				Build.ID.length() %10 + Build.MANUFACTURER.length() %10 +
				Build.MODEL.length() %10 + Build.PRODUCT.length() %10 +
				Build.TAGS.length() %10 + Build.TYPE.length() %10 +
				Build.USER.length() %10 ;
		
		id.append(deviceId);
		
	    Log.d(TAG, "getUniqueDeviceId: " + id);

		return id.toString();
	}
}
