package org.mcjug.aameetingmanager;

import java.util.Calendar;
import java.util.List;

import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
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
	private Button validateAddressButton;
	private Button startTimeButton;
	private Button endTimeButton;
	
	@Override	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment		
		View view = inflater.inflate(R.layout.submit_meeting_fragment, container, false);		
		
		Calendar calendar = Calendar.getInstance();
		final int hour = calendar.get(Calendar.HOUR);
		final int minute = calendar.get(Calendar.MINUTE);		
		
		startTimeButton = (Button) view.findViewById(R.id.startTimeButton); 
		startTimeButton.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));
		startTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), startTimePickerListener, hour, minute, true);
				timePicker.show();
			} 
		}); 
		
		endTimeButton = (Button) view.findViewById(R.id.endTimeButton); 
		endTimeButton.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));
		endTimeButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				TimePickerDialog timePicker = new TimePickerDialog(getActivity(), endTimePickerListener, hour, minute, true);
				timePicker.show();
			} 
		}); 
		
		addressEditText = (EditText) view.findViewById(R.id.addressEditText);

		validateAddressButton = (Button) view.findViewById(R.id.validateAddressButton); 
		validateAddressButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				if (!validateAddress()) {
					Toast.makeText(v.getContext(), "Invalid address", Toast.LENGTH_LONG);
				}
			} 
		}); 
		
		return view;
	}
	
	private TimePickerDialog.OnTimeSetListener startTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
			startTimeButton.setText(new StringBuilder().append(pad(selectedHour)).append(":").append(pad(selectedMinute)));
		}		
	};
	
	private TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
			endTimeButton.setText(new StringBuilder().append(pad(selectedHour)).append(":").append(pad(selectedMinute)));
		}		
	};
	
	private boolean validateAddress() {
		boolean isValid = false;
        try {
			Geocoder gc = new Geocoder(getActivity());
			String address = addressEditText.getText().toString();
			List<Address> addresses = gc.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
            	isValid = true;
            } 
		} catch (Exception e) {
		}
        return isValid;
	}
	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}
}
