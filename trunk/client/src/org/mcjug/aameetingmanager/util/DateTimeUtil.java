package org.mcjug.aameetingmanager.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;

public class DateTimeUtil {

	public static String getTimeStr(Calendar calendar) {
		return String.format("%1$tH:%1$tM ", calendar);
	}

	public static String getFindMeetingTimeStr(Calendar calendar) {
		return String.format("%1$tH%1$tM00", calendar);
	}

	public static String getSubmitMeetingTimeStr(Calendar calendar) {
		return String.format("%1$tH:%1$tM:00", calendar);
	}

	public static long getTimeDurationMinutes(Calendar startTime,
			Calendar endTime) {
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

	
	
	///////////////////////////////////////////////////
	// Methods to add a meeting to the phone's calendar

	private Calendar makeCalendarDate(int dayOfWeek, Date timeOfDay) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(timeOfDay);
		int targetHours = calendar.get(Calendar.HOUR_OF_DAY);
		int targetMinutes = calendar.get(Calendar.MINUTE);

		// Normalize start times to a round quarter an hour
		if (targetMinutes < 15)
			targetMinutes = 0;
		else if (targetMinutes < 30)
			targetMinutes = 15;
		else if (targetMinutes < 45)
			targetMinutes = 30;
		else
			targetMinutes = 45;

		calendar.setTime(new Date());

		int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		// if today is Monday(2) and dayOfWeek is Tuesday(3), then dayOfWeek -
		// currentDayOfWeek is 1
		// if today is Tuesday(3) and dayOfWeek is Monday(2), then dayOfWeek -
		// currentDayOfWeek is -1
		int biasToWeekDay = dayOfWeek - currentDayOfWeek;
		if (biasToWeekDay < 0)
			biasToWeekDay += 7;

		calendar.add(Calendar.DAY_OF_YEAR, biasToWeekDay);
		calendar.set(Calendar.HOUR_OF_DAY, targetHours);
		calendar.set(Calendar.MINUTE, targetMinutes);
		calendar.set(Calendar.MILLISECOND, 0);

		return (calendar);
	}

	public void addToCalendar(Context context, String title, String description, String address,
			int startDayOfWeek, Date startTimeOfDay, int endDayOfWeek, Date endTimeOfDay) {
		
		Calendar startTime = makeCalendarDate(startDayOfWeek, startTimeOfDay);
		Calendar endTime = makeCalendarDate(endDayOfWeek, endTimeOfDay); 
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 14) {
			intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
	        intent.putExtra("beginTime", startTime.getTimeInMillis());
	        intent.putExtra("allDay", false);
	        intent.putExtra("rrule", "FREQ=WEEKLY");
	        intent.putExtra("endTime", endTime.getTimeInMillis());
	        intent.putExtra("title", "A Test Event from android app");
		}
		else {
			intent = new Intent(Intent.ACTION_INSERT,
					CalendarContract.Events.CONTENT_URI);
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
					startTime.getTimeInMillis());
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
					endTime.getTimeInMillis());
			// Add the calendar event details
			intent.putExtra(CalendarContract.Events.TITLE, title);
			intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
			intent.putExtra(CalendarContract.Events.EVENT_LOCATION, address);
		}
		context.startActivity(intent);
	}

	private void insertNewEventIntoCalendar() {
		// Create calendar date for the test day of week
		// testDayOfWeek can be 1,2,3,4,5,6 or 7
		/*  BELOW IS TEST CODE
		int testDayOfWeek = 7;
		Date testDate = new Date();
		String[] strDays = new String[] { "", "Sun", "Mon", "Tue", "Wed",
				"Thu", "Fri", "Sat" };

		Date testDate = (new GregorianCalendar(2014, 1, 28, 10, 55)).getTime();

		addToCalendar("Test for " + strDays[testDayOfWeek],
				"Test event added to the calendar at " + testDate,
				"1700 Rockville Pike, MD, 20850", testDayOfWeek, testDate);
		*/
	}

}
