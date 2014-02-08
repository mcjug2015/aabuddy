package org.mcjug.aameetingmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.meetingfinder.R;
import org.mcjug.aameetingmanager.meeting.Meeting;
import org.mcjug.aameetingmanager.meeting.MeetingListResults;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

public class MeetingListUtil {
    private static final String TAG = MeetingListUtil.class.getSimpleName();

    private static final String ID = "id";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String CREATOR = "creator";
	private static final String DAY_OF_WEEK = "day_of_week";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	private static final String ADDRESS = "address";
	private static final String DISTANCE = "distance";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";

	public static MeetingListResults getMeetingList(Context context, HttpResponse httpResponse) throws Exception {
		String jsonStr = HttpUtil.getContent(httpResponse);
	    Log.d(TAG, "Meeting list: " + jsonStr);

	    boolean is24HourTime = DateTimeUtil.is24HourTime(context);

	    MeetingListResults meetingListResults = new MeetingListResults();
		List<Meeting> meetings = new ArrayList<Meeting>();
	    if (jsonStr != null) {
		    JSONObject jsonObj = new JSONObject(jsonStr);

		    JSONObject metaJson = jsonObj.getJSONObject("meta");
		    meetingListResults.setTotalMeetingCount(metaJson.getInt("total_count"));

		    JSONArray meetingsJson = jsonObj.getJSONArray("objects");
		    if (meetingsJson != null) {
				JSONObject meetingJson;
				Meeting meeting;
				String startTime;
				String endTime;
				for (int i = 0; i < meetingsJson.length(); i++) {
					try {
						meetingJson = meetingsJson.getJSONObject(i);

						meeting = new Meeting();
						meeting.setId(meetingJson.getInt(ID));
						meeting.setName(meetingJson.getString(NAME));
						meeting.setDescription(meetingJson.getString(DESCRIPTION));
						meeting.setCreator(meetingJson.getString(CREATOR));

						String[] daysOfWeek = context.getResources().getStringArray(R.array.daysOfWeekLong);
						int dayOfWeek = meetingJson.getInt(DAY_OF_WEEK);
						meeting.setDayOfWeek(daysOfWeek[dayOfWeek - 1]);

						startTime = convertTime(meetingJson, START_TIME, is24HourTime);
						endTime = convertTime(meetingJson, END_TIME, is24HourTime);
						meeting.setTimeRange(startTime + " - " +  endTime);

						meeting.setAddress(meetingJson.getString(ADDRESS));
						meeting.setDistance(String.format("%.2f", Double.parseDouble(meetingJson.getString(DISTANCE))));

						meeting.setLatitude(meetingJson.getDouble(LATITUDE));
						meeting.setLongitude(meetingJson.getDouble(LONGITUDE));

						meetings.add(meeting);
					} catch (Exception e) {
					}
				}

				meetingListResults.setMeetings(meetings);
		    }
		}

		return meetingListResults;
	}

	private static String convertTime(JSONObject meetingJson, String timeKey, boolean is24HourTime) throws Exception {
		String time;
		if (is24HourTime) {
			time =  meetingJson.getString(timeKey).substring(0, 5);
		} else {
			int hour = Integer.parseInt(meetingJson.getString(timeKey).substring(0, 2));
			if (hour > 12) {
				hour = hour - 12;
				time = String.format("%02d%s", hour, meetingJson.getString(timeKey).substring(2, 5)) + " PM";
			} else {
				time = meetingJson.getString(timeKey).substring(0, 5) + " AM";
			}
		}

		return time;
	}

	public static String getUniqueDeviceId(Context context) {
		StringBuilder id = new StringBuilder();

		// 64-bit number as a hex string
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
