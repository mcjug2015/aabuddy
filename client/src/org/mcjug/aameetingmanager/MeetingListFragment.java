package org.mcjug.aameetingmanager;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
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
	private static final String DISTANCE = "distance";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";
	
    private static final String[] FROM = new String[] {NAME,  DAY_OF_WEEK, TIME_RANGE, ADDRESS, DISTANCE, DESCRIPTION};
    private static final int[] TO = new int[] {R.id.meetingName, R.id.meetingDay, R.id.meetingTime, R.id.meetingAddress, R.id.meetingDistance, R.id.meetingDescription};

	private SharedPreferences prefs;
    private String[] sortOrderValues;    
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.meeting_list_fragment, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

 		try {
 	        MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();
 	        prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			
 	        sortOrderValues = getResources().getStringArray(R.array.sortOrderValues);
			Spinner sortOrder = (Spinner)getView().findViewById(R.id.meetingListSortOrder);
			sortOrder.setOnItemSelectedListener(new OnItemSelectedListener() {
				private boolean initialSelection = true;

				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (initialSelection) {	
						initialSelection = false;
					} else {	
						try {
							String meetingUrl = prefs.getString(getString(R.string.meetingUrl), "");
							List<NameValuePair> values = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");

							int itemPosition = parent.getSelectedItemPosition();
							values.set(values.size() - 1, new BasicNameValuePair("order_by", sortOrderValues[itemPosition]));

							String paramStr = URLEncodedUtils.format(values, "utf-8");
							FindMeetingTask findMeetingTask = new FindMeetingTask(getActivity(), paramStr);
							findMeetingTask.execute();
						} catch (Exception ex) {
							Log.d(TAG, "Error getting meetings: " + ex);
						}
					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

 		} catch (Exception e) {
 			Log.d(TAG, "Error setting meeting list");
 		}
	}

	@Override
	public void onResume() {
        try {
			MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();			
			AAMeetingApplication app = (AAMeetingApplication) activity.getApplicationContext();	
			
			List<HashMap<String, String>> list = getListItems(app.getMeetingListData());
			SimpleAdapter adapter = new SimpleAdapter(activity, list, R.layout.meeting_list_row, FROM, TO);
			setListAdapter(adapter);
		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}
        
        ListView meetingListView = getListView();
        meetingListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
            	HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
                displayMap(map);
                return false;
            }
        });

		super.onResume();
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
	   
	private void displayMap(HashMap<String, String> map) {
        String latitude = map.get(LATITUDE);
        String longitude = map.get(LONGITUDE);
        if (latitude != null && latitude.length() != 0 && longitude != null && longitude.length() != 0) {
        	// Display a marker with the address at the latitude and longitude
        	String intentURI = "geo:" + latitude + ","+ longitude + "?z=17&q=" + latitude + "," + longitude;
        	/*
        	// Display a marker with the address from the server and other meeting information
    		String intentURI = "geo:0,0?z=17&q=" + latitude + "," + longitude
					+ "(" + map.get(ADDRESS) + " " + map.get(NAME) + " "
					+ map.get(DAY_OF_WEEK) + " " + map.get(START_TIME) + " - "
					+ map.get(END_TIME) + ")";
    		*/
        	
        	Uri geo = Uri.parse(intentURI);
            Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
            startActivity(geoMap);
        }
	}
    
}
