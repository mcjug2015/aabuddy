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


	public Meeting(String theName, String dayTime, String extraInfo,
			String building, String streetAddress, String city, String state,
			String postalCode, String specialDirections)
	{
		setName(theName);
		String descr = extraInfo;
		if ((specialDirections != null) && (!specialDirections.trim().isEmpty()))
			descr += "; " + specialDirections;
		if ((building != null) && (!building.trim().isEmpty()))
			descr += "; Location is: " + building;
		setDescription(descr);
		setDayOfWeek(dayTime.substring(0, dayTime.indexOf(" ")));
		String theStartTime = dayTime.substring(dayTime.indexOf(" ")+1);
		setStartTime(theStartTime, true);
		setStopTime(theStartTime, specialDirections);
		setAddress(streetAddress + ", " + city + ", " + state + " " + postalCode);
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
	
	public void setStopTime(String start, String specialDir) {
		stopTime = "N/A";
		int meetingLength = 1; // hours
		if ((specialDir != null) && (!specialDir.isEmpty())) {
			int meetingLengthIndex = specialDir.indexOf(MEETING_LENGTH_SUFFIX);
			if (meetingLengthIndex > 0) {
				try {
					if (specialDir.substring(meetingLengthIndex-1).startsWith(" ")) {
						meetingLength = Integer.parseInt(specialDir.substring(meetingLengthIndex-2, meetingLengthIndex-1));
					}
					else {
						meetingLength = Integer.parseInt(specialDir.substring(meetingLengthIndex-1, meetingLengthIndex));
					}
				}
				catch (NumberFormatException nfe) {
					System.out.println("Unable to parse the below special instructions for a meeting length:\n" + specialDir);
				}
			}
		}
		GregorianCalendar calendar = new GregorianCalendar();
		try {
			Date startTimeInDateFormat = simpleDateFormat.parse(start);
			calendar.setTime(startTimeInDateFormat);
			calendar.add(Calendar.HOUR, meetingLength);
			stopTime = String.format("%1$tH:%1$tM:00", calendar);
		}
		catch (Exception e) {
			System.out.println("Unable to take the start time: " + start + " and the special directions: " + specialDir + " to determine an end time");
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
