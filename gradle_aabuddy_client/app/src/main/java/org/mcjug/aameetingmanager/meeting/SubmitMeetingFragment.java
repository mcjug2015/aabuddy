package org.mcjug.aameetingmanager.meeting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.AAMeetingManager;
import org.mcjug.aameetingmanager.LocationFinder;
import org.mcjug.aameetingmanager.LocationFinder.LocationResult;
import org.mcjug.aameetingmanager.MultiSpinner;
import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.meeting.FindSimilarMeetingsTask.FindSimilarMeetingsListener;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingTask.SubmitMeetingListener;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;
import org.mcjug.meetingfinder.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmitMeetingFragment extends Fragment {
	private static final String TAG = SubmitMeetingFragment.class.getSimpleName();

	private EditText nameEditText;
	private EditText descriptionEditText;
	private EditText addressEditText;
	private Button refreshLocationButton;
	private Button submitMeetingButton;
	private Button startTimeButton;
	private Button endTimeButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private Spinner dayOfWeekSpinner;
	private MultiSpinner meetingTypesSpinner;
 
	private boolean isTimeValid = true;
	private boolean is24HourTime;
	
	private TimePickerDialog.OnTimeSetListener startTimePickerListener;
	private TimePickerDialog.OnTimeSetListener endTimePickerListener;
	private ProgressDialog locationProgress;
	private ProgressDialog submitProgressDialog;
	private LocationResult locationResult;
	private Credentials submitCredentials;
	private String submitMeetingParams;
	private Context context;
	private Map<String, Integer> meetingTypeIds = new HashMap<String, Integer>();
	
	private static final int BEFORE_SUBMIT_LOGIN_ACTIVITY 	= 1;
	private static final int AFTER_SUBMIT_LOGIN_ACTIVITY 	= 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Credentials credentials = Credentials.readFromPreferences(getActivity());
		
		if (!credentials.isSet()) {
			//if no user name and password, go to login screen
			
			Intent loginIntent =  new Intent(getActivity(), LoginFragmentActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivityForResult(loginIntent, BEFORE_SUBMIT_LOGIN_ACTIVITY);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
			case BEFORE_SUBMIT_LOGIN_ACTIVITY:
				if (resultCode != Activity.RESULT_OK) {
					getActivity().finish();
					//go back to home
					startActivity(new Intent(getActivity().getApplicationContext(), AAMeetingManager.class)
											.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				}
			break;
			
			case AFTER_SUBMIT_LOGIN_ACTIVITY:
				if (resultCode != Activity.RESULT_OK) {
					getActivity().finish();
					//go back to home
					startActivity(new Intent(getActivity().getApplicationContext(), AAMeetingManager.class)
											.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				} else {
					Credentials credentials = Credentials.readFromPreferences(getActivity());
					submitMeeting(credentials);
				}
			break;
		}
	}

	@Override	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment		
		View view = inflater.inflate(R.layout.submit_meeting_fragment, container, false);		
		
		nameEditText = (EditText) view.findViewById(R.id.submitMeetingNameEditText);
		nameEditText.addTextChangedListener(new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				checkMeetingFieldsValid();
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
		});
		
		descriptionEditText = (EditText) view.findViewById(R.id.submitMeetingDescriptionEditText);
		descriptionEditText.addTextChangedListener(new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				checkMeetingFieldsValid();
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
		});
		
		addressEditText = (EditText) view.findViewById(R.id.submitMeetingAddressEditText);
		
		refreshLocationButton = (Button) view.findViewById(R.id.submitMeetingRefreshLocationButton);
		refreshLocationButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					Context context = getActivity();
					locationProgress = ProgressDialog.show(context, context.getString(R.string.getLocationMsg),
							context.getString(R.string.waitMsg));
					LocationFinder locationTask = new LocationFinder(getActivity(), locationResult);
					locationTask.requestLocation();
				} catch (Exception ex) {
					Log.d(TAG, "Error current location " + ex);
				}
			}
		});
		
		submitMeetingButton = (Button) view.findViewById(R.id.submitMeetingButton); 
		submitMeetingButton.setEnabled(false);
		submitMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				if (nameEditText.getText().toString().trim().length() == 0) {
					Toast.makeText(getActivity(), R.string.nameRequiredMsg, Toast.LENGTH_LONG).show();					
					return;
				}

				try {
					String addressName = addressEditText.getText().toString().trim();
					Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
					if (address == null) {
						Toast.makeText(getActivity(), "Please enter a valid address", Toast.LENGTH_LONG).show();
						return;
					}
				} catch (Exception ex) {
				}

		        // hide keyboard
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);

				Credentials credentials = Credentials.readFromPreferences(getActivity());
				if (!credentials.isSet()) {
					//if no user name and password, go to login screen
					Intent loginIntent =  new Intent(getActivity().getApplicationContext(), LoginFragmentActivity.class);
					loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					getActivity().startActivityForResult(loginIntent, AFTER_SUBMIT_LOGIN_ACTIVITY);

				} else {
					submitMeeting(credentials);
				}
			}

		}); 
		
		dayOfWeekSpinner = (Spinner) view.findViewById(R.id.submitMeetingDayOfWeekSpinner);
		dayOfWeekSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				checkMeetingFieldsValid();
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		dayOfWeekSpinner.setSelection(dayOfWeek - 1);
		
		List<MeetingType> meetingTypes = AAMeetingApplication.getInstance().getMeetingTypes();
		final List<String> meetingTypesToDisplay = new ArrayList<String>();
		meetingTypeIds.clear();		
		for (int i = 0; i < meetingTypes.size(); i++) {
			MeetingType meetingType = meetingTypes.get(i);
			meetingTypesToDisplay.add(meetingType.getName());
			meetingTypeIds.put(meetingType.getShortName().trim(), Integer.valueOf(meetingType.getId()));
		}

		meetingTypesSpinner = (MultiSpinner) view.findViewById(R.id.submitMeetingTypesSpinner);
		meetingTypesSpinner.setItems(meetingTypesToDisplay, getString(R.string.none), getString(R.string.none), null,
				new MultiSpinnerListener() {
					@Override
					public void onItemsSelected(boolean[] selected) {
					}
		});
		return view;
	}
	
	private void updateTimeWidgets(final boolean is24HourTime) {
		View view = getView();
		
		startTimeCalendar = Calendar.getInstance();
		clearTimeFields(startTimeCalendar);
		int minutes = DateTimeUtil.roundMinutes(startTimeCalendar.get(Calendar.MINUTE));
		startTimeCalendar.set(Calendar.MINUTE, minutes);
		
		startTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				startTimeCalendar.set(Calendar.MINUTE, minute);
				startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
				clearTimeFields(startTimeCalendar);			

				endTimeCalendar.setTime(startTimeCalendar.getTime());
				endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
				endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));

				isTimeValid = true;
				submitMeetingButton.setEnabled(isTimeValid);	
			}		
		};

		startTimeButton = (Button) view.findViewById(R.id.submitMeetingStartTimeButton); 
		startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
		startTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), startTimePickerListener, 
						startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), is24HourTime);
				timePicker.show();
			} 
		}); 
		
		endTimeCalendar = Calendar.getInstance();
		endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
		clearTimeFields(endTimeCalendar);
		endTimeCalendar.set(Calendar.MINUTE, minutes);
		
		endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				endTimeCalendar.set(Calendar.MINUTE, minute);
				endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
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

				checkMeetingFieldsValid();
			}
		};		    
		
		endTimeButton = (Button) view.findViewById(R.id.submitMeetingEndTimeButton); 
		endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
		endTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), endTimePickerListener, 
						endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), is24HourTime);
				timePicker.show();
			} 
		});
	}
	
	private void submitMeeting(Credentials credentials) {
		FragmentActivity activity = getActivity();
		try {
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
			submitMeetingParams = createSubmitMeetingJson();
			submitCredentials = credentials;
			
			submitProgressDialog = ProgressDialog.show(activity, activity.getString(R.string.submitMeetingProgressMsg), 
						activity.getString(R.string.waitMsg));

			new FindSimilarMeetingsTask(activity, submitMeetingParams, findSimilarMeetingsListener).execute();					
			
			submitMeetingButton.setEnabled(false);
			nameEditText.requestFocus();
			
		} catch (Exception ex) {
        	String msg = String.format(activity.getString(R.string.submitMeetingError), ex);
			Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
		}
	} 

	private boolean checkMeetingFieldsValid() {
		submitMeetingButton.setEnabled(isTimeValid);
		return isTimeValid;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		context = getActivity();
		is24HourTime = DateTimeUtil.is24HourTime(context);
		updateTimeWidgets(is24HourTime);
		
		Location location = LocationUtil.getLastKnownLocation(context);
		String address = LocationUtil.getFullAddress(location, context);
		if (address == null || address.equals("")) {
			addressEditText.setText("Please type in address or refresh");
		} else {
			addressEditText.setText(address);
		}
		
		locationResult =  new LocationResult() {
			@Override
			public void setLocation(Location location) {
				locationProgress.cancel();
				
				if (location == null) {
					location = LocationUtil.getLastKnownLocation(getActivity());
				}

				if (location == null) {
					Toast.makeText(getActivity(), getString(R.string.locationNotFound), Toast.LENGTH_LONG).show();
				} else {
					String address = LocationUtil.getFullAddress(location, getActivity());
					if (address.trim().equals("")) {
						Toast.makeText(getActivity(), getString(R.string.addressNotFound), Toast.LENGTH_LONG).show();
					} else {
						addressEditText.setText(address);
					}
				}
			}
		};
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		is24HourTime = DateTimeUtil.is24HourTime(context);
	    updateTimeWidgets(is24HourTime);
		super.onResume();
	}	
	
	private void clearTimeFields(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	private String createSubmitMeetingJson() throws Exception {
		JSONObject json = new JSONObject();
		
		String name = nameEditText.getText().toString().trim();
		json.put("name", name);

		String description = descriptionEditText.getText().toString().trim();
		json.put("description", description);
		
		json.put("internal_type", getString(R.string.submitted));

		// Day of week is 1-7 where 1 is Sunday and 7 is Saturday
		int idx = dayOfWeekSpinner.getSelectedItemPosition() + 1;
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
		     Log.d(TAG, "Address is invalid: " + address);
			 throw new Exception("Address is invalid: " + address);
		}

		String[] meetingTypeSelections = ((String) meetingTypesSpinner.getSelectedItem()).split(",");
		if (!meetingTypeSelections[0].equals(getString(R.string.none))) {
			List<Integer> meetingTypeIdList = new ArrayList<Integer>();
			for (String str: meetingTypeSelections) {
				Integer id = meetingTypeIds.get(str.trim());
				meetingTypeIdList.add(id);
			}
			json.put("type_ids", new JSONArray(meetingTypeIdList));
		}
		
		return json.toString();
	}
	
	public void removeLoginInfo() {
		Credentials.removeFromPreferences(getActivity());
	}
	
	private FindSimilarMeetingsListener findSimilarMeetingsListener = new FindSimilarMeetingsListener() {
		public void findSimilarMeetingsResults(List<Meeting> similarMeetings, String errorMsg) {
			final Context context = getActivity();
			if (errorMsg != null) {
				submitProgressDialog.cancel();
				Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
				return;
			}
			
			try {
				if (similarMeetings.size() > 0) {
					submitProgressDialog.cancel();

					LayoutInflater layoutInflater = LayoutInflater.from(context);
					View view = layoutInflater.inflate(R.layout.similar_meetings_dialog, null);		    
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(getString(R.string.similarMeetingsList));
					builder.setView(view);
					
					ListView listView = (ListView)view.findViewById(R.id.similarMeetingList);
					MeetingAdapter adapter = new MeetingAdapter(getActivity(), R.layout.meeting_list_row, similarMeetings);
					listView.setAdapter(adapter);
					
					builder.setPositiveButton(getString(R.string.submit_button), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							submitProgressDialog = ProgressDialog.show(context, getString(R.string.submitMeetingProgressMsg), getString(R.string.waitMsg));
							new SubmitMeetingTask(context, submitMeetingParams, submitCredentials, submitMeetingListener).execute();
						}
					});

					builder.setNegativeButton(getString(R.string.cancel_submit_button), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						}
					});

					builder.create().show();
				} else {
					new SubmitMeetingTask(context, submitMeetingParams, submitCredentials, submitMeetingListener).execute();					
				}
			} catch (Exception ex) {
				Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();				
			}
		}
	};
	
	private SubmitMeetingListener submitMeetingListener = new SubmitMeetingListener() {
		public void submitMeetingResults(Meeting meeting, String errorMsg) {
			submitProgressDialog.cancel();
			
			final Context context = getActivity();
			if (errorMsg != null) {
				Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
				return;
			}
			
			try {
				if (meeting != null) {
					LayoutInflater layoutInflater = LayoutInflater.from(context);
					View view = layoutInflater.inflate(R.layout.submit_meeting_dialog, null);		    
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(getString(R.string.meetingAdded));
					builder.setView(view);
					
					List<Meeting> meetings = new ArrayList<Meeting>();
					meetings.add(meeting);
					
					MeetingAdapter adapter = new MeetingAdapter(getActivity(), R.layout.meeting_list_row, meetings);
					ListView listView = (ListView)view.findViewById(R.id.submitMeetingList);
					listView.setAdapter(adapter);
					
					builder.setPositiveButton(getString(R.string.addAnotherMeetingButton), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							nameEditText.setText("");
							descriptionEditText.setText("");
							getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						}
					});

					builder.setNegativeButton(getString(R.string.goToMainScreenButton), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							
							Intent intent = new Intent(getActivity(), AAMeetingManager.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});

					builder.create().show();
				} else {
					getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				}
				
			} catch (Exception ex) {
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
			}			
		}
	};	
	
}
