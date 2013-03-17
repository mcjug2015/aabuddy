package org.mcjug.aameetingmanager.util;

import java.util.Calendar;
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
}
