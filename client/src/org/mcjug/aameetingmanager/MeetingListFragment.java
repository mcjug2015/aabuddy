package org.mcjug.aameetingmanager;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.Spinner;
import android.widget.TextView;

public class MeetingListFragment extends ListFragment {
	private static final String TAG = MeetingListFragment.class.getSimpleName();

 	private SharedPreferences prefs;
    private String[] sortOrderValues; 
    private MeetingAdapter listAdapter;
    private InfiniteScrollListener infiniteScrollListener;
    private View footerView;
    private TextView meetingListNumItemsLabel;
    private int offset = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.meeting_list_fragment, container, false);
		
		meetingListNumItemsLabel = (TextView)view.findViewById(R.id.meetingListNumItemsLabel);
	    
	    if (null != savedInstanceState) {
	    	offset = savedInstanceState.getInt("offset");
	    }
	    
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		AAMeetingApplication app = (AAMeetingApplication) getActivity().getApplicationContext();
		List<Meeting> meetings = new ArrayList<Meeting>();
		meetings.addAll(app.getMeetingListResults().getMeetings());
		
		footerView = getActivity().getLayoutInflater().inflate(R.layout.meeting_list_footer, null);
		getListView().addFooterView(footerView);	

		listAdapter = new MeetingAdapter(getActivity(), R.layout.meeting_list_row, meetings);
	    getListView().setAdapter(listAdapter);
	    
		infiniteScrollListener = new InfiniteScrollListener(getActivity(), getListView(), footerView);
		infiniteScrollListener.setOffset(offset);
		getListView().setOnScrollListener(infiniteScrollListener);
	 
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
							List<NameValuePair> meetingParams = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");
							NameValuePair param;
							for (int i = 0; i < meetingParams.size(); i++) {
								param = meetingParams.get(i);
				            	if (param.getName().equals("order_by")) {
				            		meetingParams.set(i, new BasicNameValuePair(param.getName(),  sortOrderValues[parent.getSelectedItemPosition()]));
				            	
				            	} else if (param.getName().equals("offset")) {
				            		meetingParams.set(i, new BasicNameValuePair(param.getName(), "0"));
				            	
				            	} else if (param.getName().equals("limit")) {
								    int paginationSize = view.getContext().getResources().getInteger(R.integer.paginationSize);
				            		meetingParams.set(i, new BasicNameValuePair(param.getName(), String.valueOf(paginationSize)));
				            	}
				            }

							getListView().setSelection(0);
							infiniteScrollListener.reset();
							
							String paramStr = URLEncodedUtils.format(meetingParams, "utf-8");
							FindMeetingTask findMeetingTask = new FindMeetingTask(view.getContext(), paramStr, false, getActivity().getString(R.string.sortMeetingProgressMsg));
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
			MeetingListResults meetingListResults = app.getMeetingListResults();
			listAdapter.setMeetings(meetingListResults.getMeetings());
			
			String numItemsLabel = String.format(getActivity().getString(R.string.meetingListNumItems),
					listAdapter.getCount(), meetingListResults.getTotalMeetingCount());
			meetingListNumItemsLabel.setText(numItemsLabel);
			
			infiniteScrollListener.setLoading(false);
			getListView().removeFooterView(footerView);
			
		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}
        
        ListView meetingListView = getListView();        
        meetingListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
                displayMap((Meeting) listView.getItemAtPosition(position));
                return false;
            }
        });

		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("offset", infiniteScrollListener.getOffset());
	}  
	
	@Override
	public void setRetainInstance(boolean retain) {
		// TODO Auto-generated method stub
		super.setRetainInstance(true);
	}

	private void displayMap(Meeting meeting) {
        String latitude = meeting.getLatitude();
        String longitude = meeting.getLongitude();
        if (latitude != null && latitude.length() != 0 && longitude != null && longitude.length() != 0) {
        	
        	// Display a marker with the address at the latitude and longitude
        	String intentURI = "geo:" + latitude + ","+ longitude + "?z=17&q=" + latitude + "," + longitude;
        	Uri geo = Uri.parse(intentURI);
            Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
            startActivity(geoMap);
        }
	}
}
