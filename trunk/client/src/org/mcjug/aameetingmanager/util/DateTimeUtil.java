package org.mcjug.aameetingmanager.util;

import java.util.Calendar;
import java.util.Date;

import org.mcjug.aameetingmanager.meeting.Meeting;
import org.mcjug.meetingfinder.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

public class DateTimeUtil {

	public static boolean is24HourTime(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String timeFormat = prefs.getString(context.getString(R.string.timeFormatKey), "12");
		if (timeFormat.equals("24")) {
			return true;
		} else {
			return false;
		}
	}

	public static String getTimeStr(Calendar calendar, boolean is24HourTime) {
		if (is24HourTime) {
			return String.format("%1$tH:%1$tM ", calendar);
		} else {
			return String.format("%1$tI:%1$tM %Tp ", calendar);
		}
	}

	public static String getFindMeetingTimeStr(Calendar calendar) {
		return String.format("%1$tH%1$tM00", calendar);
	}

	public static String getSubmitMeetingTimeStr(Calendar calendar) {
		return String.format("%1$tH:%1$tM:00", calendar);
	}

	public static long getTimeDurationMinutes(Calendar startTime, Calendar endTime) {
		long startMillis = startTime.getTimeInMillis();
		long endMillis = endTime.getTimeInMillis();
		if (startMillis > endMillis) {
			endMillis += (24 * 60 * 60 * 1000);
		}

		long diffMinutes = (endMillis - startMillis) / (60 * 1000);
		return diffMinutes;
	}

	public static int roundMinutes(int currentMinutes) {
		return (currentMinutes + 5) / 10 * 10;
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	public static void addToCalendar(Context context, Meeting meeting) {

		Calendar startTime = makeCalendarDate(meeting.getStartTime(), meeting.getDayOfWeekIdx());
		Calendar endTime = makeCalendarDate(meeting.getEndTime(), meeting.getDayOfWeekIdx());

		Intent intent;
		if (Build.VERSION.SDK_INT < 14) {
			intent = new Intent(Intent.ACTION_EDIT);
			intent.putExtra("beginTime", startTime.getTimeInMillis());
			intent.putExtra("endTime", endTime.getTimeInMillis());
		} else {
			intent = new Intent(Intent.ACTION_INSERT);
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis());
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
			intent.setData(Events.CONTENT_URI);
		}

		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra(Events.ALL_DAY, false);

		String[] daysOfWeekCalendar = context.getResources().getStringArray(R.array.daysOfWeekCalendar);
		String dayOfWeekStr = daysOfWeekCalendar[meeting.getDayOfWeekIdx() - 1];
		intent.putExtra(Events.RRULE, "FREQ=WEEKLY;COUNT=52;WKST=SU;BYDAY="+ dayOfWeekStr);

		// Add the calendar event details
		intent.putExtra(Events.TITLE, meeting.getName());
		intent.putExtra(Events.DESCRIPTION, meeting.getDescription());
		intent.putExtra(Events.EVENT_LOCATION, meeting.getAddress());

		intent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);

		context.startActivity(intent);
	}

	private static Calendar makeCalendarDate(Date meetingTime, int dayOfWeekIdx) {
		// Set the meeting time (start or end time)
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meetingTime);
		int targetHours = calendar.get(Calendar.HOUR_OF_DAY);
		int targetMinutes = calendar.get(Calendar.MINUTE);
		
		// Normalize start times to a round quarter an hour
//		if (targetMinutes < 15)
//			targetMinutes = 0;
//		else if (targetMinutes < 30)
//			targetMinutes = 15;
//		else if (targetMinutes < 45)
//			targetMinutes = 30;
//		else
//			targetMinutes = 45;
	
		calendar.setTime(new Date());
		
		int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		// if today is Monday(2) and dayOfWeek is Tuesday(3), then dayOfWeek -
		// currentDayOfWeek is 1
		// if today is Tuesday(3) and dayOfWeek is Monday(2), then dayOfWeek -
		// currentDayOfWeek is -1
		int biasToWeekDay = dayOfWeekIdx - currentDayOfWeek;
		if (biasToWeekDay < 0)
			biasToWeekDay += 7;

		calendar.add(Calendar.DAY_OF_YEAR, biasToWeekDay);
		calendar.set(Calendar.HOUR_OF_DAY, targetHours);
		calendar.set(Calendar.MINUTE, targetMinutes);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar;
	}
	
	public static String getOrdinalFor(int value) {
		int hundredRemainder = value % 100;
		int tenRemainder = value % 10;
		if(hundredRemainder - tenRemainder == 10) {
			return "th";
		}

		switch (tenRemainder) {
			case 1:  return "st";
			case 2:  return "nd";
			case 3:  return "rd";
			default: return "th";
		}
	}

	public static void resetRecoveryDate (Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = prefs.edit();
		editor.remove(context.getString(R.string.recoveryDateValue));
		editor.commit();
	}
	
	public static Calendar getRecoveryDate (Context context) {
		Calendar recoveryDate = null;
		// recoveryDate.setTime(new Date(Long.MAX_VALUE));
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long longRecoveryDate = prefs.getLong(context.getString(R.string.recoveryDateValue), 0);
		if (longRecoveryDate > 0) {
			recoveryDate = Calendar.getInstance();
			recoveryDate.setTime(new Date(longRecoveryDate));
		}
		return (recoveryDate);
	}
	
	public static void setRecoveryDate (Context context, Calendar calendar){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = prefs.edit();
		long longRecoveryDate = calendar.getTimeInMillis();
		editor.putLong(context.getString(R.string.recoveryDateValue), longRecoveryDate);
		editor.commit();
	}
	
	public static boolean getRecoveryDateAllowed (Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean boolRecoveryDateAllowed = prefs.getBoolean(context.getString(R.string.recoveryDateAllowed), true);
		return (boolRecoveryDateAllowed);
	}
	
	public static void setRecoveryDateAllowed (Context context, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(context.getString(R.string.recoveryDateAllowed), value);
		if (!value)
			editor.remove(context.getString(R.string.recoveryDateValue));
		editor.commit();
	}
	
}
