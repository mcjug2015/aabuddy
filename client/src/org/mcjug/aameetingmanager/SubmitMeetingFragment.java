package org.mcjug.aameetingmanager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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

import android.app.TimePickerDialog;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
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
			
			boolean isValid = DateTimeUtil.checkTimes(startTimeCalendar, endTimeCalendar);
			if (!isValid) {
				Toast.makeText(view.getContext(), "Start time must be less than end time", Toast.LENGTH_LONG).show();		
			}
			submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
		}		
	};
	
	private final TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endTimeCalendar.set(Calendar.MINUTE, minute);
			endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
			
			boolean isValid = DateTimeUtil.checkTimes(startTimeCalendar, endTimeCalendar);
			if (!isValid) {
				Toast.makeText(view.getContext(), "End time must be greater than start time", Toast.LENGTH_LONG).show();		
			}
			submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
		}		
	};
	
	private class SubmitMeetingTask extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();  
			try {  
				String baseUrl = getActivity().getString(R.string.meeting_base_url);
				HttpPost request = new HttpPost(baseUrl);  
				StringEntity se = new StringEntity(createSubmitMeetingJson());  
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				request.setEntity(se);
				HttpResponse response = client.execute(request);
		        int statusCode = response.getStatusLine().getStatusCode();
		        if (statusCode != HttpStatus.SC_CREATED) {
		        	return "Error submitting meeting: " +  response.getStatusLine().toString();
		        }
			} catch (Exception e) {  
				return "Error submitting meeting: " + e;
			} finally {
				client.getConnectionManager().shutdown();  
			}

			return null;
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
		
		List<String> daysOfWeekListItems = Arrays.asList(getResources().getStringArray(R.array.daysOfWeek));
		String dayOfWeek = ((String)dayOfWeekSpinner.getSelectedItem()).trim();
		int idx = daysOfWeekListItems.indexOf(dayOfWeek);
		if (idx == 0) {
			idx = 7;
		}
		json.put("day_of_week", idx);
		
		String addressName = addressEditText.getText().toString();
		json.put("address", addressName);
	
		Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
		if (address != null) {
			json.put("lat", address.getLatitude());
			json.put("long",  address.getLongitude());
		} else {
			json.put("lat", -77.4108);
			json.put("long", 39.4142);
		}
		
		json.put("start_time", DateTimeUtil.getTimeStr(startTimeCalendar));
		json.put("end_time", DateTimeUtil.getTimeStr(endTimeCalendar));
		
		return json.toString();
	}

}
