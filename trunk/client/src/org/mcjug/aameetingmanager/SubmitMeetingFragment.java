package org.mcjug.aameetingmanager;

import java.util.Calendar;

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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class SubmitMeetingFragment extends Fragment {
	private static final String TAG = SubmitMeetingFragment.class.getSimpleName();

	private final static int MENU_ID_LOGOUT = Menu.FIRST + 1;
	private final static int MENU_ID_LOGIN  = Menu.FIRST + 2;
	
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
  
	private boolean isLocationValid = false;
	private boolean isTimeValid = true;
	
	private SharedPreferences prefs;
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
		addressEditText.addTextChangedListener(new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				isLocationValid = false;
				validateAddressButton.setEnabled(true);	
				submitMeetingButton.setEnabled(false);
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			public void afterTextChanged(Editable s) {
				
			}
		});		
		
		currentLocationButton = (Button) view.findViewById(R.id.submitMeetingCurrentLocationButton); 
	    currentLocationButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				try {
					Context context = getActivity();
					progress = ProgressDialog.show(context, context.getString(R.string.getLocationMsg), context.getString(R.string.waitMsg));
					LocationFinder locationTask = new LocationFinder(getActivity(), locationResult);
					locationTask.requestLocation();
					
					isLocationValid = false;
					validateAddressButton.setEnabled(true);	
					submitMeetingButton.setEnabled(false);
				} catch (Exception ex) {
				    Log.d(TAG, "Error getting location: " + ex);
				}
			} 
		});
	    
		validateAddressButton = (Button) view.findViewById(R.id.submitMeetingValidateAddressButton); 
		validateAddressButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				isLocationValid = LocationUtil.validateAddress(addressEditText.getText().toString(), v.getContext());
				if (!isLocationValid) {
					Toast.makeText(v.getContext(), "Invalid address", Toast.LENGTH_LONG).show();
				}
				
				validateAddressButton.setEnabled(!isLocationValid);				
				submitMeetingButton.setEnabled(isLocationValid && isTimeValid);
			} 
		}); 
		
		submitMeetingButton = (Button) view.findViewById(R.id.submitMeetingButton); 
		submitMeetingButton.setEnabled(false);
		submitMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				
				//hide keyboard
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
				
				Credentials credentials = Credentials.readFromPreferences(getActivity());
				
				if (!credentials.isSet()) {
					//if no username and password, go to login screen
					
					//TODO: may want to change to just swap out the fragment

					Bundle args = new Bundle();
					//TODO:  pass in meeting info
					
					Intent loginIntent =  new Intent(getActivity().getApplicationContext(), LoginFragmentActivity.class);
					loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					getActivity().startActivity(loginIntent);
											
					return;
				}
				
				try {
					String submitMeetingParams = createSubmitMeetingJson();
					new SubmitMeetingTask(getActivity(), submitMeetingParams, credentials).execute();
				} catch (Exception ex) {
		        	String msg = String.format(getActivity().getString(R.string.submitMeetingError), ex);
					Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				}
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
					Toast.makeText(getActivity(), getActivity().getString(R.string.locationNotFound), Toast.LENGTH_LONG).show();
				} else {
					String address = LocationUtil.getAddress(location, getActivity());
					if (address.trim().equals("")) {
						Toast.makeText(getActivity(), getActivity().getString(R.string.addressNotFound), Toast.LENGTH_LONG).show();
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
			
			isTimeValid = true;
			submitMeetingButton.setEnabled(isLocationValid && isTimeValid);			
		}		
	};
	
	private final TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endTimeCalendar.set(Calendar.MINUTE, minute);
			endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
			clearTimeFields(endTimeCalendar);
			
			Context context = view.getContext();
			isTimeValid = true;
			if (startTimeCalendar.compareTo(endTimeCalendar) == 0) {
				isTimeValid = false;
				Toast.makeText(context, getString(R.string.startAndEndTimesAreEqual), Toast.LENGTH_LONG).show();
			
			} else if (startTimeCalendar.compareTo(endTimeCalendar) == 1) {
				long timeDurationMins = DateTimeUtil.getTimeDurationMinutes(startTimeCalendar, endTimeCalendar);
				String msg = String.format(getString(R.string.meetingDuration), timeDurationMins);
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
			
			submitMeetingButton.setEnabled(isLocationValid && isTimeValid);
		}		
	};
	
	private void clearTimeFields(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	private String createSubmitMeetingJson() throws Exception {
		JSONObject json = new JSONObject();
		
		String name = nameEditText.getText().toString().trim();
		if (!name.equals("")) {
			json.put("name", name);
		}

		String description = descriptionEditText.getText().toString().trim();
		if (!description.equals("")) {
			json.put("description", description);
		}
		
		json.put("internal_type", getString(R.string.submitted));

		// Day of week is 1-7 where 1 is Sunday and 7 is Saturday
		int idx = dayOfWeekSpinner.getSelectedItemPosition();
		json.put("day_of_week", idx);
		
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
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		Credentials credentials = Credentials.readFromPreferences(getActivity());

		menu.removeItem(MENU_ID_LOGOUT);
		menu.removeItem(MENU_ID_LOGIN);

		if (!credentials.isSet()) {
			int groupId = 0;
			menu.add(groupId, MENU_ID_LOGIN, Menu.NONE, getString(R.string.loginMenuText));
		} else {
			int groupId = 0;
			menu.add(groupId, MENU_ID_LOGOUT, Menu.NONE, getString(R.string.logoutMenuText));
		}

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
	            
	        case MENU_ID_LOGOUT:
	        	removeLoginInfo();
	        	return true;
	        	
	        case MENU_ID_LOGIN:
	            startActivity(new Intent(getActivity().getApplicationContext(), LoginFragmentActivity.class)
                		.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	        	return true;
	        
        }

		
		return false;
	}
	
	public void removeLoginInfo() {
		Credentials.removeFromPreferences(getActivity());
	}
}
