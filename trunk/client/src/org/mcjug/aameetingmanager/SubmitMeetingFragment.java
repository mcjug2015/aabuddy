package org.mcjug.aameetingmanager;

import java.util.Calendar;

import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class SubmitMeetingFragment extends Fragment {
	private EditText addressEditText;
	private Button currentLocationButton;
	private Button validateAddressButton;
	private Button submitMeetingButton;
	private Button startTimeButton;
	private Button endTimeButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
  
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
				submitMeetingButton.setEnabled(submitMeetingButton.isEnabled() && isValid);
			} 
		}); 
		
		submitMeetingButton = (Button) view.findViewById(R.id.submitMeetingButton); 
		submitMeetingButton.setEnabled(false);
		
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
}
