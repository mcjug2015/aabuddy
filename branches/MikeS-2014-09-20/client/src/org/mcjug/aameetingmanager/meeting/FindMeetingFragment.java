package org.mcjug.aameetingmanager.meeting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.DaysOfWeekMultiSpinner;
import org.mcjug.aameetingmanager.LocationFinder;
import org.mcjug.aameetingmanager.LocationFinder.LocationResult;
import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
import org.mcjug.aameetingmanager.scheduleservice.ServiceConfig;
import org.mcjug.aameetingmanager.scheduleservice.ServiceHandler;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;
import org.mcjug.aameetingmanager.util.MeetingTypeUtil;
import org.mcjug.meetingfinder.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


import android.graphics.Color;



public class FindMeetingFragment extends Fragment {
	private static final String TAG = FindMeetingFragment.class.getSimpleName();
	private static final String EMPTY_TIME = "--:--";

	private EditText nameEditText;
	private EditText addressEditText;
	private Button refreshLocationButton;
	private Button startTimeButton;
	private Button startTimeClearButton;
	private Button endTimeButton;
	private Button endTimeClearButton;
	private ImageButton selectMeetingsTypesButton;
	private Button findMeetingButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private DaysOfWeekMultiSpinner daysOfWeekSpinner;
	private Spinner distanceSpinner;

	private TimePickerDialog.OnTimeSetListener startTimeDialogListener;
	private TimePickerDialog.OnTimeSetListener endTimeDialogListener;
	private ProgressDialog locationProgress;
	private LocationResult locationResult;
	private FindMeetingTask findMeetingTask;

	private Context context;
	private boolean is24HourTime;
	private ServiceConfig serviceConfig;
	private ServiceHandler serviceHandler;
	private boolean showMeetingTypes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		serviceConfig = new ServiceConfig(getActivity(), ServiceConfig.DataSourceTypes.AA_MEETING_TYPE);
		serviceHandler = new ServiceHandler(getActivity(), ServiceConfig.DataSourceTypes.AA_MEETING_TYPE);
		showMeetingTypes = false;
		if (MeetingTypeUtil.getMeetingTypeShowPref(getActivity())) {
			showMeetingTypes = true;
			serviceHandler.startServiceOnce(serviceConfig.getURL());	
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);
		
		daysOfWeekSpinner = (DaysOfWeekMultiSpinner) view.findViewById(R.id.findMeetingDaysOfWeekSpinner);
		List<String> daysOfWeekListItems = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekLong));
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		daysOfWeekSpinner.setItems(daysOfWeekListItems, daysOfWeekListItems.get(day),
		        getString(R.string.all_days_of_week), daysOfWeekSpinnerListener);

		nameEditText = (EditText) view.findViewById(R.id.findMeetingNameEditText);

		addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);

		refreshLocationButton = (Button) view.findViewById(R.id.findMeetingRefreshLocationButton);
		refreshLocationButton.setOnClickListener(new OnClickListener() {
			@Override
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

		selectMeetingsTypesButton = (ImageButton) view.findViewById(R.id.selectMeetingType);
		if (showMeetingTypes) {
			selectMeetingsTypesButton.setOnClickListener(new OnClickListener() {
				@Override
	            public void onClick(View v) {
					createDialog();
				}
			});
		}
		else
		{
			selectMeetingsTypesButton.setVisibility(View.INVISIBLE);
		}
		
				
		findMeetingButton = (Button) view.findViewById(R.id.findMeetingFindButton);
		findMeetingButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				try {
					FragmentActivity activity = getActivity();
					String addressName = addressEditText.getText().toString();
					Address address = LocationUtil.getAddressFromLocationName(addressName, activity);
					if (address == null) {
						Toast.makeText(getActivity(), "Please enter a valid address", Toast.LENGTH_LONG).show();
					} else {
						findMeetingTask = new FindMeetingTask(activity, getFindMeetingParams(), false,
								activity.getString(R.string.findMeetingProgressMsg));
						findMeetingTask.execute();
					}
				} catch (Exception ex) {
					Log.d(TAG, "Error getting meetings: " + ex);
				}
			}
		});

		distanceSpinner = (Spinner) view.findViewById(R.id.findMeetingDistanceSpinner);
		List<String> distanceValues = Arrays.asList(getResources().getStringArray(R.array.searchDistanceValues));
		distanceSpinner.setSelection(distanceValues.indexOf("10"));
		return view;
	}


    public void createDialog() {
    	showPreselected();
    	AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
    	.setIcon(R.drawable.select_type32x32)
    	.setTitle(R.string.select_type_button)
    	.setMultiChoiceItems(serviceHandler.allTypes, serviceHandler.preselected,
    			new DialogInterface.OnMultiChoiceClickListener() {
		    		public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
		    			ListView listView = ((AlertDialog)dialog).getListView();
		    			SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		    			Log.v(TAG, "Alert Screen - user clicked Yes, set " + checkedItems);
		    		}
    	})
    	.setPositiveButton(R.string.ok,
    			new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface dialog, int whichButton) {
		    			Log.v(TAG, "user clicked Yes");
		    			ListView listView = ((AlertDialog)dialog).getListView();
		    			saveDialogResults (listView);
		    	}
    	})
    	.setNegativeButton(R.string.cancel,
    			new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {
	    			clearPreselected();
	    			Log.v(TAG, "Alert Screen - user clicked No : " + whichButton);
	    		}
    	});
    	builder.show();
    }

    
    private void showPreselected () {
    	Log.i(TAG, "allTypes length %d " + serviceHandler.allTypes.length);
    	String selectedItems = String.format(" First item %s\r\nPreselected Item(s): ", serviceHandler.allTypes[0].toString());
    	String delimiter = "";
    	for (int i=0; i < serviceHandler.preselected.length; i++) {
    		if (serviceHandler.preselected[i]) {
    			String item = serviceHandler.allTypes [i].toString();
    			selectedItems += delimiter + i + ". " + item;
    			delimiter = ", ";
    		}
    	}
    	if (delimiter == "")
    		selectedItems += "Any";
    	Log.i(TAG, selectedItems);
    }
    
    private void clearPreselected() {
    	for (int i=0; i < serviceHandler.preselected.length; i++) {
    		serviceHandler.preselected[i] = false;
    	}
    	selectMeetingsTypesButton.setBackgroundColor(0x00000000);
    }
    
    private void saveDialogResults (ListView listView) {
    	SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
    	String selectedItems = "Selected Items: Any";
		String delimiter = "";
		boolean foundCheckedItem = false;
		if (checkedItems != null) {
			if (checkedItems.size() > 0) {
				selectedItems = "Selected Item(s): ";
				for (int i=0; i < checkedItems.size(); i++) {
					if (checkedItems.valueAt(i)) {
						String item = listView.getAdapter().getItem(checkedItems.keyAt(i)).toString();
						selectedItems += delimiter + checkedItems.keyAt(i) + "." + item;
						delimiter = ", ";
						serviceHandler.preselected [checkedItems.keyAt(i)] = true;
						foundCheckedItem = true;
					}
					else
						serviceHandler.preselected [checkedItems.keyAt(i)] = false;
				}
			}
		}
		Log.i(TAG, selectedItems);
		if (foundCheckedItem)
			selectMeetingsTypesButton.setBackgroundColor(Color.GREEN);
		else
			selectMeetingsTypesButton.setBackgroundColor(Color.WHITE);
    }

    
	private void updateTimeWidgets(final boolean is24HourTime) {
		View view = getView();
		
		startTimeCalendar = Calendar.getInstance();
		startTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
		startTimeCalendar.set(Calendar.MINUTE, 0);

		startTimeButton = (Button) view.findViewById(R.id.findMeetingStartTimeButton);
		startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
		
	    startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					startTimeCalendar.set(Calendar.MINUTE, minute);
					startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
				}
		 };
		
		startTimeButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				TimePickerDialog dialog = new TimePickerDialog(getActivity(), startTimeDialogListener, startTimeCalendar
						.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), is24HourTime);
				dialog.show();
			}
		});

		startTimeClearButton = (Button) view.findViewById(R.id.findMeetingStartTimeClearButton);
		startTimeClearButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				int width = startTimeButton.getWidth();
				startTimeButton.setText(EMPTY_TIME);
				startTimeButton.setWidth(width);
			}
		});

		endTimeCalendar = Calendar.getInstance();
		endTimeCalendar.set(Calendar.HOUR_OF_DAY, 23);
		endTimeCalendar.set(Calendar.MINUTE, 59);

	    endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
				@Override
		        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					endTimeCalendar.set(Calendar.MINUTE, minute);
					endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
				}
			};
			
		endTimeButton = (Button) view.findViewById(R.id.findMeetingEndTimeButton);
		endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
		endTimeButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				TimePickerDialog dialog = new TimePickerDialog(getActivity(), endTimeDialogListener, endTimeCalendar
						.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), is24HourTime);
				dialog.show();
			}
		});

		endTimeClearButton = (Button) view.findViewById(R.id.findMeetingEndTimeClearButton);
		endTimeClearButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				int width = endTimeButton.getWidth();
				endTimeButton.setText(EMPTY_TIME);
				endTimeButton.setWidth(width);
			}
		});
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		context = getActivity();

		Location location = LocationUtil.getLastKnownLocation(context);
		String address = LocationUtil.getFullAddress(location, context);
		if (address == null || address.equals("")) {
			addressEditText.setText("Please type in address or refresh");
		} else {
			addressEditText.setText(address);
		}

		locationResult = new LocationResult() {
			@Override
			public void setLocation(Location location) {
				locationProgress.cancel();

				if (location == null) {
					location = LocationUtil.getLastKnownLocation(getActivity());
				}

				if (location == null) {
					Toast.makeText(getActivity(),
							"Not able to get current location. Please check if GPS is turned or you have a network data connection.",
							Toast.LENGTH_LONG).show();
				} else {
					String addressStr = LocationUtil.getFullAddress(location, getActivity());
					if (addressStr.trim().equals("")) {
						Toast.makeText(getActivity(),
								"Not able to get address from location. Please check for a network data connection",
								Toast.LENGTH_LONG).show();
					} else {
						addressEditText.setText(addressStr);
					}
				}
			}
		};

		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onPause () {
        super.onPause();
        Log.v(TAG, "onPause");
        serviceHandler.stopReceiver();
	}

	@Override
	public void onDestroy() {
	    Log.v(TAG, "onDestroy: broadcastReceiver unregistered");
	    serviceHandler.stopReceiver();
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		is24HourTime = DateTimeUtil.is24HourTime(context);	
	    updateTimeWidgets(is24HourTime);
        serviceHandler.startReceiver();
		super.onResume();
	}
	
	private final MultiSpinnerListener daysOfWeekSpinnerListener = new MultiSpinnerListener() {
		@Override
        public void onItemsSelected(boolean[] selected) {
		}
	};

	private String getFindMeetingParams() throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		String addressName = addressEditText.getText().toString();
		Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
		params.add(new BasicNameValuePair("lat", String.valueOf(address.getLatitude())));
		params.add(new BasicNameValuePair("long", String.valueOf(address.getLongitude())));

		String[] mileValues = getResources().getStringArray(R.array.searchDistanceValues);
		params.add(new BasicNameValuePair("distance_miles", mileValues[distanceSpinner.getSelectedItemPosition()]));

		String name = nameEditText.getText().toString().trim();
		if (!name.equals("")) {
			params.add(new BasicNameValuePair("name", name));
		}

		if (!startTimeButton.getText().equals(EMPTY_TIME)) {
			params.add(new BasicNameValuePair("start_time__gte", DateTimeUtil.getFindMeetingTimeStr(startTimeCalendar)));
		}

		if (!endTimeButton.getText().equals(EMPTY_TIME)) {
			params.add(new BasicNameValuePair("end_time__lte", DateTimeUtil.getFindMeetingTimeStr(endTimeCalendar)));
		}

		String[] daysOfWeekSelections = ((String) daysOfWeekSpinner.getSelectedItem()).split(",");
		if (daysOfWeekSelections[0].equalsIgnoreCase(getString(R.string.all_days_of_week))) {
			daysOfWeekSelections = getString(R.string.all_days_of_week_value).split(",");
		}

		List<String> daysOfWeek;
		if (daysOfWeekSelections.length == 1) {
			daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekLong));
		} else if (daysOfWeekSelections.length == 2 || daysOfWeekSelections.length == 3) {
			daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekMedium));
		} else {
			daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekShort));
		}

		for (String str : daysOfWeekSelections) {
			int idx = daysOfWeek.indexOf(str.trim());
			params.add(new BasicNameValuePair("day_of_week_in", String.valueOf(idx + 1)));
		}

		params.add(new BasicNameValuePair("order_by", getString(R.string.sortingDefault)));

		int paginationSize = getActivity().getResources().getInteger(R.integer.paginationSize);
		params.add(new BasicNameValuePair("offset", "0"));
		params.add(new BasicNameValuePair("limit", String.valueOf(paginationSize)));

		String paramStr = URLEncodedUtils.format(params, "utf-8");
		Log.d(TAG, "Find meeting request params: " + paramStr);

		return paramStr;
	}
}
