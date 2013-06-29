package org.mcjug;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Meeting {
	private static final String S = "|";	// Output file separator (can't use commas because they may be in user text)
	private static final String MEETING_LENGTH_SUFFIX = "HR MTG";
	private static final String DAYS_OF_WEEK = "SUNMONTUEWEDTHUFRISAT";
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
	private String name;
	private String description;
	private String dayOfWeek;
	private String startTime;
	private String stopTime;
	private String address;
	private String latitude;
	private String longitude;


	public Meeting(String theName, String theDayOfWeek, String theStartTime,
			String building, String theAddress) {
		setName(theName);
		if ((building != null) && (!building.trim().isEmpty()))
			setDescription("Location is: " + building);
		setDayOfWeek(theDayOfWeek);
		setStartTime(theStartTime, true);
		setStopTime(theStartTime, 1);
		setAddress(theAddress);
		setLatitude("TBD");
		setLongitude("TBD");
	}
	
	public Meeting(String theName, String theDescription,
			String theDayOfWeek, String theStartTime, String theStopTime,
			String theAddress, String theLatitude, String theLongitude)
	{
		setName(theName);
		setDescription(theDescription);
		setDayOfWeek(theDayOfWeek);
		setStartTime(theStartTime);
		setStopTime(theStopTime);
		setAddress(theAddress);
		setLatitude(theLatitude);
		setLongitude(theLongitude);
	}
	
	public String toSeparatedString() {
		String str = name + S + description + S +
			convertDayOfWeekTextToNumeric(dayOfWeek) + S +
			startTime + S +
			stopTime + S +
			address;
		return str;
	}
	
	public String convertDayOfWeekTextToNumeric(String dayTextStr) {
		boolean valid = false;
		String dayNumericStr = "N/A";
		if ((dayTextStr != null) && (dayTextStr.length() > 2)) {
			int dayValue = (DAYS_OF_WEEK.indexOf(dayTextStr.toUpperCase().substring(0, 3)) + 3) / 3;
			if (dayValue > 0) {
				valid = true;
				dayNumericStr = Integer.toString(dayValue);
			}
		}
		if (!valid) {
			System.out.println("\n\nERROR: Meeting.convertDayOfWeekTextToNumeric():");
			System.out.println("       Unable to convert " + dayTextStr + " to a numeric day of week");
			System.out.println("       for meeting: " + name);
		}
		return dayNumericStr;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String start) {
		setStartTime(start, false);
	}
	
	public void setStartTime(String start, boolean convertToMilitary) {
		if (convertToMilitary) {
			GregorianCalendar calendar = new GregorianCalendar();
			try {
				Date startTimeInDateFormat = simpleDateFormat.parse(start);
				calendar.setTime(startTimeInDateFormat);
				this.startTime = String.format("%1$tH:%1$tM:00", calendar);
			}
			catch (Exception e) {
				System.out.println("Unable to take the start time: " + start + " and convert it to military time");
			}
		}
		else {
			this.startTime = start;
		}
	}
	
	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	
	public void setStopTime(String start, int meetingLength) {
		stopTime = "N/A";
		GregorianCalendar calendar = new GregorianCalendar();
		try {
			Date startTimeInDateFormat = simpleDateFormat.parse(start);
			calendar.setTime(startTimeInDateFormat);
			calendar.add(Calendar.HOUR, meetingLength);
			stopTime = String.format("%1$tH:%1$tM:00", calendar);
		}
		catch (Exception e) {
			System.out.println("Unable to take the start time: " + start + " and add a meeting length");
		}
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getLatitude() {
		return latitude;
	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
