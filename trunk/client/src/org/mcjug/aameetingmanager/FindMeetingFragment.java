package org.mcjug.aameetingmanager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
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


public class FindMeetingFragment extends Fragment {
	
	private EditText addressEditText = null;
	private Button startTimeButton = null;
	private Button endTimeButton = null;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

		final Calendar c = Calendar.getInstance();

		startTimeButton = (Button) view.findViewById(R.id.findMeetingStartTimeButton);
		startTimeButton.setText(DateTimeUtil.getTimeStr(c));
		startTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				TimePickerDialog d = new TimePickerDialog(getActivity(), startTimeDialogListener, 
						c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		c.add(Calendar.HOUR_OF_DAY, 1);
		
		endTimeButton = (Button) view.findViewById(R.id.findMeetingEndTimeButton);
		endTimeButton.setText(DateTimeUtil.getTimeStr(c));
		endTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(getActivity(), endTimeDialogListener, 
						c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		MultiSpinner multiSpinner = (DaysOfWeekMultiSpinner) view.findViewById(R.id.findMeetingDaysOfWeekSpinner);
		String[] daysOfWeek = getResources().getStringArray(R.array.daysOfWeek);
		List<String> items = Arrays.asList(daysOfWeek);
	    multiSpinner.setItems(items, "All", daysOfWeekSpinnerListener);

	    addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);

		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String address = LocationUtil.getLastKnownLocation(this.getActivity());
		addressEditText.setText(address);
	}
	
	private final TimePickerDialog.OnTimeSetListener startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startTimeButton.setText(DateTimeUtil.getTimeStr(hourOfDay, minute));
		}
	};

	private final TimePickerDialog.OnTimeSetListener endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeButton.setText(DateTimeUtil.getTimeStr(hourOfDay, minute));
		}
	};
	
	private final MultiSpinnerListener daysOfWeekSpinnerListener = new MultiSpinnerListener() {
		public void onItemsSelected(boolean[] selected) {
		}
	};
}
