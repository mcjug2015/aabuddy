package org.mcjug.aameetingmanager;

import java.text.SimpleDateFormat;
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
import android.widget.TimePicker;

public class FindMeetingFragment extends Fragment {
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM.dd.yyyy");
	private SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
	private Button startDateButton = null;
	private Button endDateButton = null;
	private Button startTimeButton = null;
	private Button endTimeButton = null;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

		startDateButton = (Button) view.findViewById(R.id.startDateButton);
		final OnClickListener startDateButtonListener = new OnClickListener() {
			public void onClick(View v) {
				startDateButtonClicked();
			}
		};
		startDateButton.setOnClickListener(startDateButtonListener);
		
		endDateButton = (Button) view.findViewById(R.id.endDateButton);
		final OnClickListener endDateButtonListener = new OnClickListener() {
			public void onClick(View v) {
				endDateButtonClicked();
			}
		};
		endDateButton.setOnClickListener(endDateButtonListener);
		
		startTimeButton = (Button) view.findViewById(R.id.startTimeButton);
		final OnClickListener startTimeButtonListener = new OnClickListener() {
			public void onClick(View v) {
				startTimeButtonClicked();
			}
		};
		startTimeButton.setOnClickListener(startTimeButtonListener);
		
		endTimeButton = (Button) view.findViewById(R.id.endTimeButton);
		final OnClickListener endTimeButtonListener = new OnClickListener() {
			public void onClick(View v) {
				endTimeButtonClicked();
			}
		};
		endTimeButton.setOnClickListener(endTimeButtonListener);
		
		return view;
	}
	
	public void startDateButtonClicked() {
		final DatePickerDialog.OnDateSetListener startDateDialogListener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.MONTH, monthOfYear);
					c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					c.set(Calendar.YEAR, year);
					String startDate = dateFormatter.format(c.getTime());
					startDateButton.setText(startDate);
				}
			};
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog d = new DatePickerDialog(getActivity(), startDateDialogListener, year, month, day);
		d.show();
	}
	
	public void endDateButtonClicked() {
		final DatePickerDialog.OnDateSetListener endDateDialogListener =
			new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.MONTH, monthOfYear);
					c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					c.set(Calendar.YEAR, year);
					String endDate = dateFormatter.format(c.getTime());
					endDateButton.setText(endDate);
				}
			};
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog d = new DatePickerDialog(getActivity(), endDateDialogListener, year, month, day);
		d.show();
	}
	
	public void startTimeButtonClicked() {
		final TimePickerDialog.OnTimeSetListener startTimeDialogListener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, hourOfDay);
					c.set(Calendar.MINUTE, minute);
					String startTime = timeFormatter.format(c.getTime());
					startTimeButton.setText(startTime);
				}
			};
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		TimePickerDialog d = new TimePickerDialog(getActivity(), startTimeDialogListener, hour, minute, false);
		d.show();
	}
	
	public void endTimeButtonClicked() {
		final TimePickerDialog.OnTimeSetListener endTimeDialogListener =
			new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, hourOfDay);
					c.set(Calendar.MINUTE, minute);
					String endTime = timeFormatter.format(c.getTime());
					endTimeButton.setText(endTime);
				}
			};
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		TimePickerDialog d = new TimePickerDialog(getActivity(), endTimeDialogListener, hour, minute, false);
		d.show();
	}
	
}
