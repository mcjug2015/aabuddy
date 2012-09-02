package org.mcjug.aameetingmanager;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class FindMeetingFragment extends Fragment {
	
	private EditText addressEditText = null;
	private Button startDateButton = null;
	private Button endDateButton = null;
	private Button startTimeButton = null;
	private Button endTimeButton = null;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

		final Calendar c = Calendar.getInstance();

		startDateButton = (Button) view.findViewById(R.id.findMeetingStartDateButton);
		startDateButton.setText(DateTimeUtil.getDateStr(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
		startDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				DatePickerDialog d = new DatePickerDialog(getActivity(), startDateDialogListener, 
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				d.show();				
			}
		});
		
		c.add(Calendar.MONTH, 1);
		
		endDateButton = (Button) view.findViewById(R.id.findMeetingEndDateButton);
		endDateButton.setText(DateTimeUtil.getDateStr(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)));
		endDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				DatePickerDialog d = new DatePickerDialog(getActivity(), endDateDialogListener, 
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				d.show();
			}
		});
		
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
		
		addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);

		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String address = LocationUtil.getLastKnownLocation(this.getActivity());
		addressEditText.setText(address);
	}
	
	private final DatePickerDialog.OnDateSetListener startDateDialogListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			startDateButton.setText(DateTimeUtil.getDateStr(year, monthOfYear, dayOfMonth));
		}
	};
	
	private final TimePickerDialog.OnTimeSetListener startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startTimeButton.setText(DateTimeUtil.getTimeStr(hourOfDay, minute));
		}
	};

	private final DatePickerDialog.OnDateSetListener endDateDialogListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			endDateButton.setText(DateTimeUtil.getDateStr(year, monthOfYear, dayOfMonth));
		}
	};

	private final TimePickerDialog.OnTimeSetListener endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeButton.setText(DateTimeUtil.getTimeStr(hourOfDay, minute));
		}
	};
}
