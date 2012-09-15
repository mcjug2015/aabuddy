package org.mcjug.aameetingmanager.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtil {

	public static String getDateStr(int year, int monthOfYear, int dayOfMonth) {
		Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
		return String.format("%1$tm/%1$td/%1$ty ", calendar);
	}
	
	public static String getTimeStr(Calendar calendar) {
		return String.format("%1$tH:%1$tM ", calendar);
	}
	
	public static boolean checkTimes(Calendar startTime, Calendar endTime) {
		if (startTime.compareTo(endTime) == 1) {
			return false;
		} else {
			return true;
		}
	}
}
