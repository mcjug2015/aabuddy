package org.mcjug.aameetingmanager;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeUtil {

	public static String getDateStr(int year, int monthOfYear, int dayOfMonth) {
		Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
		return String.format("%1$tm/%1$td/%1$ty ", calendar);
	}
	
	public static String getTimeStr(int hour, int minute) {
		return String.format("%2d:%2d ", hour, minute);
	}
	
	public static String getTimeStr(Calendar calendar) {
		return String.format("%1$tH:%1$tM ", calendar);
	}
}
