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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class MeetingListFragment extends ListFragment {
	private static final String TAG = MeetingListFragment.class.getSimpleName();

	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String DAY_OF_WEEK = "day_of_week";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	private static final String TIME_RANGE = "time_range";
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

        String[] from = new String[] {NAME,  DAY_OF_WEEK, TIME_RANGE, ADDRESS, DESCRIPTION};
        int[] to = new int[] {R.id.meetingName, R.id.meetingDay, R.id.meetingTime, R.id.meetingAddress, R.id.meetingDescription};
        
		try {
	        MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();
			List<HashMap<String, String>> list = getListItems(activity.getMeetingsJson());
			
			SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.meeting_list_row, from, to);
			setListAdapter(adapter);
			
			Spinner sortOrder = (Spinner)getView().findViewById(R.id.meetingListSortOrder);
			ArrayAdapter<CharSequence> sortOrderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sortOrder, R.layout.spinner_item);
			sortOrderAdapter.setDropDownViewResource(R.layout.spinner_item);
			sortOrder.setAdapter(sortOrderAdapter);
		
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
				map.put(DESCRIPTION, meetingJson.getString(DESCRIPTION));
				
				String[] daysOfWeek = activity.getResources().getStringArray(R.array.daysOfWeek);
				int dayOfWeek = meetingJson.getInt(DAY_OF_WEEK);
				if (dayOfWeek == 7) {
					map.put(DAY_OF_WEEK, daysOfWeek[0]);
				} else {
					map.put(DAY_OF_WEEK, daysOfWeek[dayOfWeek]);
				}
				
				String startTime = meetingJson.getString(START_TIME).substring(0, 5);
				String endTime = meetingJson.getString(END_TIME).substring(0, 5);
				map.put(TIME_RANGE, startTime + " - " +  endTime);
				
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
