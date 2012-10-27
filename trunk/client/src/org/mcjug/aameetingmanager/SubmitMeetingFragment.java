package org.mcjug.aameetingmanager;

import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class SubmitMeetingFragment extends Fragment {
	private static final String TAG = SubmitMeetingFragment.class.getSimpleName();

	private EditText addressEditText;
	private Button currentLocationButton;
	private Button validateAddressButton;
	private Button submitMeetingButton;
	private Button startTimeButton;
	private Button endTimeButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private Spinner dayOfWeekSpinner;
  
	@Override	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment		
		View view = inflater.inflate(R.layout.submit_meeting_fragment, container, false);		
		
		startTimeCalendar = Calendar.getInstance();
		clearTimeFields(startTimeCalendar);
		
		startTimeButton = (Button) view.findViewById(R.id.submitMeetingStartTimeButton); 
		startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
		startTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), startTimePickerListener, 
						startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), true);
				timePicker.show();
			} 
		}); 
		
		endTimeCalendar = Calendar.getInstance();
		endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
		clearTimeFields(endTimeCalendar);
		
		endTimeButton = (Button) view.findViewById(R.id.submitMeetingEndTimeButton); 
		endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
		endTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), endTimePickerListener, 
						endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), true);
				timePicker.show();
			} 
		}); 
		
		addressEditText = (EditText) view.findViewById(R.id.submitMeetingAddressEditText);
		
		currentLocationButton = (Button) view.findViewById(R.id.submitMeetingCurrentLocationButton); 
		currentLocationButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
			} 
		}); 		
		
		validateAddressButton = (Button) view.findViewById(R.id.submitMeetingValidateAddressButton); 
		validateAddressButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				boolean isValid = LocationUtil.validateAddress(addressEditText.getText().toString(), v.getContext());
				if (!isValid) {
					Toast.makeText(v.getContext(), "Invalid address", Toast.LENGTH_LONG).show();
				}
				// submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
				submitMeetingButton.setEnabled(true);
			} 
		}); 
		
		submitMeetingButton = (Button) view.findViewById(R.id.submitMeetingButton); 
		submitMeetingButton.setEnabled(false);
		submitMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				new SubmitMeetingTask().execute();
			} 
		}); 
		
		dayOfWeekSpinner = (Spinner) view.findViewById(R.id.submitMeetingDayOfWeekSpinner); 

		return view;
	}
		
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String address = LocationUtil.getLastKnownLocation(getActivity());
		addressEditText.setText(address);
	}

	private final TimePickerDialog.OnTimeSetListener startTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			startTimeCalendar.set(Calendar.MINUTE, minute);
			startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
			clearTimeFields(startTimeCalendar);			
			
			boolean isValid = DateTimeUtil.checkTimes(startTimeCalendar, endTimeCalendar);
			if (!isValid) {
				endTimeCalendar.setTime(startTimeCalendar.getTime());
				endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
				endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
			}
		}		
	};
	
	private final TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endTimeCalendar.set(Calendar.MINUTE, minute);
			endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
			clearTimeFields(endTimeCalendar);
			
			Context context = view.getContext();
			boolean isValid = true;
			if (startTimeCalendar.compareTo(endTimeCalendar) == 0) {
				isValid = false;
				Toast.makeText(context, context.getString(R.string.startAndEndTimesAreEqual), Toast.LENGTH_LONG).show();
			
			} else if (startTimeCalendar.compareTo(endTimeCalendar) == 1) {
				long timeDurationMins = DateTimeUtil.getTimeDurationMinutes(startTimeCalendar, endTimeCalendar);
				String msg = String.format(context.getString(R.string.meetingDuration), timeDurationMins);
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
			
			submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
		}		
	};
	
	private void clearTimeFields(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	private class SubmitMeetingTask extends AsyncTask<Void, String, String> {
		
		@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();  
			try {  
				String baseUrl = getSaveMeetingBaseUrl();
				HttpPost request = new HttpPost(baseUrl);  
				StringEntity se = new StringEntity(createSubmitMeetingJson());  
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(se);
				HttpResponse response = client.execute(request);
		        int statusCode = response.getStatusLine().getStatusCode();
		        if (statusCode != HttpStatus.SC_OK) {
		        	return "Error submitting meeting: " +  response.getStatusLine().toString();
		        }
			} catch (Exception e) {  
				return "Error submitting meeting: " + e;
			} finally {
				client.getConnectionManager().shutdown();  
			}

			return null;
		}

		private String getSaveMeetingBaseUrl() {
			Activity activity = getActivity();

			StringBuilder baseUrl = new StringBuilder();
			
			String defaultServerBase = activity.getString(R.string.meetingServerBaseUrlDefaultValue);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			String serverBaseUrl = prefs.getString(activity.getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
			
			baseUrl.append(serverBaseUrl);
			baseUrl.append(activity.getString(R.string.save_meeting_url_path));
			
			return baseUrl.toString();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
			}
		}
	}

	private String createSubmitMeetingJson() throws Exception {
		JSONObject json = new JSONObject();
		json.put("name", "");
		json.put("description", "");
		json.put("internal_type", getString(R.string.submitted));

		// Day of week is 1-7 where 1 is Monday and 7 is Sunday
		int idx = dayOfWeekSpinner.getSelectedItemPosition();
		json.put("day_of_week", (idx == 0) ? 7 : idx);
		
		json.put("start_time", DateTimeUtil.getSubmitMeetingTimeStr(startTimeCalendar));
		json.put("end_time", DateTimeUtil.getSubmitMeetingTimeStr(endTimeCalendar));

		String addressName = addressEditText.getText().toString();
		json.put("address", addressName);
	
		Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
		if (address != null) {
			json.put("lat", address.getLatitude());
			json.put("long",  address.getLongitude());
		} else {
			json.put("lat", -77.4108);
			json.put("long", 39.4142);
			
		    // Log.d(TAG, "Address is invalid: " + address);
			// throw new Exception("Address is invalid: " + address);
		}
		
		return json.toString();
	}
}
