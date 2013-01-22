package org.mcjug.aameetingmanager;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

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

	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "long";
	
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
			
			SimpleAdapter adapter = MeetingListUtil.getListAdapter(getActivity(), app.getMeetingListData());
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

	private void displayMap(HashMap<String, String> map) {
        String latitude = map.get(LATITUDE);
        String longitude = map.get(LONGITUDE);
        if (latitude != null && latitude.length() != 0 && longitude != null && longitude.length() != 0) {
        	
        	// Display a marker with the address at the latitude and longitude
        	String intentURI = "geo:" + latitude + ","+ longitude + "?z=17&q=" + latitude + "," + longitude;
        	Uri geo = Uri.parse(intentURI);
            Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
            startActivity(geoMap);
        }
	}
    
}
