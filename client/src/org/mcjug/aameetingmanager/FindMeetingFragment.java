package org.mcjug.aameetingmanager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;

import android.app.TimePickerDialog;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class FindMeetingFragment extends Fragment {
	private static final String TAG = FindMeetingFragment.class.getSimpleName();

	private EditText addressEditText;
	private Button startTimeButton;
	private Button endTimeButton;
	private Button findMeetingButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private DaysOfWeekMultiSpinner daysOfWeekSpinner;
	private Spinner distanceSpinner;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

		startTimeCalendar = Calendar.getInstance();

		startTimeButton = (Button) view.findViewById(R.id.findMeetingStartTimeButton);
		startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
		startTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(getActivity(), startTimeDialogListener, 
						startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		endTimeCalendar = Calendar.getInstance();
		endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
		
		endTimeButton = (Button) view.findViewById(R.id.findMeetingEndTimeButton);
		endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
		endTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(getActivity(), endTimeDialogListener, 
						endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		daysOfWeekSpinner = (DaysOfWeekMultiSpinner) view.findViewById(R.id.findMeetingDaysOfWeekSpinner);
		List<String> daysOfWeekListItems = Arrays.asList(getResources().getStringArray(R.array.daysOfWeek));
		daysOfWeekSpinner.setItems(daysOfWeekListItems, getString(R.string.all_days_of_week), daysOfWeekSpinnerListener);

	    addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);

	    findMeetingButton = (Button) view.findViewById(R.id.findMeetingFindButton); 
		findMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				 new FindMeetingTask().execute();
			} 
		}); 
		
	    distanceSpinner = (Spinner) view.findViewById(R.id.findMeetingDistanceSpinner); 

		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String address = LocationUtil.getLastKnownLocation(this.getActivity());
		addressEditText.setText(address);
	}
	
	private final TimePickerDialog.OnTimeSetListener startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			startTimeCalendar.set(Calendar.MINUTE, minute);
			startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
			
			if (!DateTimeUtil.checkTimes(startTimeCalendar, endTimeCalendar)) {
				Toast.makeText(view.getContext(), "Start time must be less than end time", Toast.LENGTH_LONG).show();		
			}
		}
	};

	private final TimePickerDialog.OnTimeSetListener endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endTimeCalendar.set(Calendar.MINUTE, minute);
			endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
			
			if (!DateTimeUtil.checkTimes(startTimeCalendar, endTimeCalendar)) {
				Toast.makeText(view.getContext(), "End time must be greater than start time", Toast.LENGTH_LONG).show();		
			}
		}
	};
	
	private final MultiSpinnerListener daysOfWeekSpinnerListener = new MultiSpinnerListener() {
		public void onItemsSelected(boolean[] selected) {
		}
	};
	
	private class FindMeetingTask extends AsyncTask<Void, String, String> {
		@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();  
			try {  
				String baseUrl = getActivity().getString(R.string.get_meeting_base_url);
				String url = baseUrl + "?" + getFindMeetingParams();
				HttpGet request = new HttpGet(url);
				HttpResponse httpResponse = client.execute(request);
			    String jsonResponse = getMeetingsResponse(httpResponse);
			    Log.d(TAG, "Find meeting jsonResponse: " + jsonResponse);
			} catch (Exception e) {  
				return "Error in find meeting: " + e;
			} finally {
				client.getConnectionManager().shutdown();  
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}
	
	private String getFindMeetingParams() throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
	        
		params.add(new BasicNameValuePair("name", ""));
		params.add(new BasicNameValuePair("description", ""));
		
		params.add(new BasicNameValuePair("start_time__gte", DateTimeUtil.getFindMeetingTimeStr(startTimeCalendar)));
		params.add(new BasicNameValuePair("end_time__lte", DateTimeUtil.getFindMeetingTimeStr(endTimeCalendar)));

		String[] mileValues = getResources().getStringArray(R.array.searchDistanceValues);
		params.add(new BasicNameValuePair("distance_miles", mileValues[distanceSpinner.getSelectedItemPosition()]));

		String[] daysOfWeekSelections = ((String)daysOfWeekSpinner.getSelectedItem()).split(",");
		if (daysOfWeekSelections[0].equalsIgnoreCase(getString(R.string.all_days_of_week))) {
			params.add(new BasicNameValuePair("day_of_week", getString(R.string.all_days_of_week_value)));
	    } else {
			StringBuffer daysOfWeekStr = new StringBuffer("");
			List<String> daysOfWeekAbbr = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekAbbr));
			for (String str: daysOfWeekSelections) {				
				int idx = daysOfWeekAbbr.indexOf(str.trim());
				if (idx == 0) {
					idx = 7;
				}
				daysOfWeekStr.append(idx + ",");
			}
			String str = daysOfWeekStr.substring(0, daysOfWeekStr.length() - 1);
			// params.add(new BasicNameValuePair("day_of_week", str));
	    }

		String addressName = addressEditText.getText().toString();
		params.add(new BasicNameValuePair("address", addressName));

		Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
		if (address != null) {
			params.add(new BasicNameValuePair("lat", String.valueOf(address.getLatitude())));
			params.add(new BasicNameValuePair("long", String.valueOf(address.getLongitude())));
		} else {
			params.add(new BasicNameValuePair("lat", "-77.4108"));
			params.add(new BasicNameValuePair("long", "39.4142"));

		    //Log.d(TAG, "Address is invalid: " + address);
			//throw new Exception("Address is invalid: " + address);
		}
		
		String paramStr = URLEncodedUtils.format(params, "utf-8");
	    Log.d(TAG, "Find meeting request params: " + paramStr);

		return paramStr;
	}

	private String getMeetingsResponse(HttpResponse httpResponse) throws Exception {
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
		return builder.toString();
	}
	
	private void getMeetings(String jsonResponse) throws Exception {
		if (jsonResponse != null) {
			JSONObject jsonObject = new JSONObject(jsonResponse);
		}
	}

}
