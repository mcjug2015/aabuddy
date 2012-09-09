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
	private Button startTimeButton;
	private Button endTimeButton;
  
	@Override	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment		
		View view = inflater.inflate(R.layout.submit_meeting_fragment, container, false);		
		
		final Calendar c = Calendar.getInstance();
		
		startTimeButton = (Button) view.findViewById(R.id.submitMeetingStartTimeButton); 
		startTimeButton.setText(DateTimeUtil.getTimeStr(c));
		startTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), startTimePickerListener, 
						c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
				timePicker.show();
			} 
		}); 
		
		c.add(Calendar.HOUR_OF_DAY, 1);
		
		endTimeButton = (Button) view.findViewById(R.id.submitMeetingEndTimeButton); 
		endTimeButton.setText(DateTimeUtil.getTimeStr(c));
		endTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), endTimePickerListener, 
						c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
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
				if (!LocationUtil.validateAddress(addressEditText.getText().toString(), v.getContext())) {
					Toast.makeText(v.getContext(), "Invalid address", Toast.LENGTH_LONG).show();
				}
			} 
		}); 
		
		return view;
	}
		
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String address = LocationUtil.getLastKnownLocation(getActivity());
		addressEditText.setText(address);
	}

	private final TimePickerDialog.OnTimeSetListener startTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hour, int minute) {
			startTimeButton.setText(DateTimeUtil.getTimeStr(hour, minute));
		}		
	};
	
	private final TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hour, int minute) {
			endTimeButton.setText(DateTimeUtil.getTimeStr(hour, minute));
		}		
	};
}
