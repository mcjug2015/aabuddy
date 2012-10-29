package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MeetingListFragment extends ListFragment {
	private static final String TAG = MeetingListFragment.class.getSimpleName();

	private static final String NAME = "name";
	private static final String DAY_OF_WEEK = "day_of_week";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	private static final String ADDRESS = "address";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.meeting_list_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        String[] from = new String[] {NAME,  DAY_OF_WEEK, START_TIME, END_TIME, ADDRESS, LATITUDE, LONGITUDE};
        int[] to = new int[] {R.id.meetingName, R.id.day, R.id.startTime, R.id.endTime,  R.id.address, R.id.latitude, R.id.longitude};
        
		try {
	        MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();
			List<HashMap<String, String>> list = getListItems(activity.getMeetingsJson());
	        SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.meeting_list_row, from, to);
	        setListAdapter(adapter);
		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}
	}

	protected List<HashMap<String, String>> getListItems(String meetingsJson) throws Exception {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		if (meetingsJson != null) {
			Activity activity = getActivity();
			JSONArray meetingListJson = new JSONArray(meetingsJson);
			JSONObject meetingJson;
			for (int i = 0; i < meetingListJson.length(); i++) {
				meetingJson = meetingListJson.getJSONObject(i);
				
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(NAME, meetingJson.getString(NAME));
				
				String[] daysOfWeek = activity.getResources().getStringArray(R.array.daysOfWeek);
				int dayOfWeek = meetingJson.getInt(DAY_OF_WEEK);
				if (dayOfWeek == 7) {
					map.put(DAY_OF_WEEK, daysOfWeek[0]);
				} else {
					map.put(DAY_OF_WEEK, daysOfWeek[dayOfWeek]);
				}
				
				map.put(START_TIME, meetingJson.getString(START_TIME));
				map.put(END_TIME, meetingJson.getString(END_TIME));
				map.put(ADDRESS, meetingJson.getString(ADDRESS));
				
				double latitude = meetingJson.getDouble(LATITUDE);
				map.put(LATITUDE, String.format("%.3f", latitude));
				
				double longitude = meetingJson.getDouble(LONGITUDE);
				map.put(LONGITUDE, String.format("%.3f", longitude));
				
				list.add(map);
			}
		}
		return list;
	}
	   
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
	}
}
