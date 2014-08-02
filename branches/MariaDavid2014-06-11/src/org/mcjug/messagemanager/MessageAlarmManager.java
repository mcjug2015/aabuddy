package org.mcjug.messagemanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MessageAlarmManager {
	private static final String TAG = MessageAlarmManager.class.getSimpleName();
	private static final int DEFAULT_MESSAGE_RETRIEVAL_INTERVAL = 30000;

	public static final void updateAlarm(Context context) {
		cancelAlarm(context);

		long interval = getInterval(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, getPendingIntent(context));		
	}

	public static final void cancelAlarm(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getPendingIntent(context));
	}

	private static final PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, MessageService.class);
		return PendingIntent.getService(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static final long getInterval(Context context) {
		int interval = DEFAULT_MESSAGE_RETRIEVAL_INTERVAL;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String intervalStr = prefs.getString("messageRetrievalInterval", "");
		try {
			interval = Integer.parseInt(intervalStr);
		} catch (NumberFormatException e) {
		}

		return interval;
	}
}
