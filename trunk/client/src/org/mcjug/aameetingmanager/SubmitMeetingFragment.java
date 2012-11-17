package org.mcjug.aameetingmanager;

import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.LocationFinder.LocationResult;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

	private EditText nameEditText;
	private EditText descriptionEditText;
	private EditText addressEditText;
	private Button currentLocationButton;
	private Button validateAddressButton;
	private Button submitMeetingButton;
	private Button startTimeButton;
	private Button endTimeButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private Spinner dayOfWeekSpinner;
  
	SharedPreferences prefs;
	private ProgressDialog progress;
	private LocationResult locationResult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

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
		
		nameEditText = (EditText) view.findViewById(R.id.submitMeetingNameEditText);
		descriptionEditText = (EditText) view.findViewById(R.id.submitMeetingDescriptionEditText);
		addressEditText = (EditText) view.findViewById(R.id.submitMeetingAddressEditText);
		
		currentLocationButton = (Button) view.findViewById(R.id.submitMeetingCurrentLocationButton); 
	    currentLocationButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				try {
					progress = ProgressDialog.show(getActivity(), "Getting location", "Please wait...");
					LocationFinder locationTask = new LocationFinder(getActivity(), locationResult);
					locationTask.requestLocation();
				} catch (Exception ex) {
				    Log.d(TAG, "Error getting meetings: " + ex);
				}
			} 
		});
	    
		validateAddressButton = (Button) view.findViewById(R.id.submitMeetingValidateAddressButton); 
		validateAddressButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				boolean isValid = LocationUtil.validateAddress(addressEditText.getText().toString(), v.getContext());
				if (!isValid) {
					Toast.makeText(v.getContext(), "Invalid address", Toast.LENGTH_LONG).show();
				}
				submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
			} 
		}); 
		
		submitMeetingButton = (Button) view.findViewById(R.id.submitMeetingButton); 
		submitMeetingButton.setEnabled(true);
		submitMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
				
				String username = prefs.getString(getString(R.string.usernamePreferenceName), "");
				String password = prefs.getString(getString(R.string.passwordPreferenceName), "");

				
				if (username.equals("") && password.equals("")) {
					//if no username and password, go to login screen
					
					//TODO: may want to change to just swap out the fragment

					Bundle args = new Bundle();
					//TODO:  pass in meeting info
					
					Intent loginIntent =  new Intent(getActivity().getApplicationContext(), LoginFragmentActivity.class);
					loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					getActivity().startActivity(loginIntent, args);
											
					return;
				}
				
				new SubmitMeetingTask(username, password).execute();
			} 
		}); 
		
		dayOfWeekSpinner = (Spinner) view.findViewById(R.id.submitMeetingDayOfWeekSpinner); 

		return view;
	}
		
	public void onActivityCreated(Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

		locationResult =  new LocationResult() {
			@Override
			public void setLocation(Location location) {
				progress.cancel();
				
				if (location == null) {
					location = LocationUtil.getLastKnownLocation(getActivity());
				}

				if (location == null) {
					Toast.makeText(getActivity(), "Not able to get current location. Please check if GPS is turned or you have a network data connection.", Toast.LENGTH_LONG).show();
				} else {
					String address = LocationUtil.getAddress(location, getActivity());
					if (address.trim().equals("")) {
						Toast.makeText(getActivity(), "Not able to get address from location. Please check for a network data connection", Toast.LENGTH_LONG).show();
					} else {
						addressEditText.setText(address);
					}
				}
			}
		};
		
		super.onActivityCreated(savedInstanceState);
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
				Toast.makeText(context, getString(R.string.startAndEndTimesAreEqual), Toast.LENGTH_LONG).show();
			
			} else if (startTimeCalendar.compareTo(endTimeCalendar) == 1) {
				long timeDurationMins = DateTimeUtil.getTimeDurationMinutes(startTimeCalendar, endTimeCalendar);
				String msg = String.format(getString(R.string.meetingDuration), timeDurationMins);
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
		
		private String username;
		private String password;
		
		public SubmitMeetingTask(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			DefaultHttpClient client = new DefaultHttpClient(); 
			try {
				String username = prefs.getString(getString(R.string.meetingServerBaseUrlPreferenceName), "");
				String password = prefs.getString(getString(R.string.meetingServerBaseUrlPreferenceName), "");

				client.getCredentialsProvider().setCredentials(
		                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
		                    new UsernamePasswordCredentials(username, password));
				
				String baseUrl = getSaveMeetingBaseUrl();
				HttpPost request = new HttpPost(baseUrl);
				StringEntity se = new StringEntity(createSubmitMeetingJson(username, password));  
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
			StringBuilder baseUrl = new StringBuilder();
			
			String defaultServerBase = getString(R.string.meetingServerBaseUrlDefaultValue);			
			String serverBaseUrl = prefs.getString(getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
			
			baseUrl.append(serverBaseUrl);
			baseUrl.append(getString(R.string.save_meeting_url_path));
			
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

	private String createSubmitMeetingJson(String username, String password) throws Exception {
		JSONObject json = new JSONObject();
		
		//TODO:  add username and password to request
		
		String name = nameEditText.getText().toString().trim();
		if (!name.equals("")) {
			json.put("name", name);
		}

		String description = descriptionEditText.getText().toString().trim();
		if (!description.equals("")) {
			json.put("description", description);
		}
		
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
		    // Log.d(TAG, "Address is invalid: " + address);
			// throw new Exception("Address is invalid: " + address);
		}
		
		return json.toString();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
        switch (item.getItemId()) {

	        // Admin prefs
	        case R.id.adminPrefs:
	            startActivity(new Intent(getActivity().getApplicationContext(), AdminPrefsActivity.class)
	                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	            return true;
        }

		
		return false;
	}	
}
