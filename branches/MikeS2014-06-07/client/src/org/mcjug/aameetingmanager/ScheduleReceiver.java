package org.mcjug.aameetingmanager;


import java.util.Calendar;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.PendingIntent;

public class ScheduleReceiver extends BroadcastReceiver {
	
	static final String TAG = "ScheduleReceiver";
	
	// restart service every 30 seconds
	private static final long REPEAT_TIME = 1000 * 60 * 3;

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, StartServiceReceiver.class);

		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		Calendar boot_time = Calendar.getInstance();
		// start 60 seconds after boot completed
		boot_time.add(Calendar.SECOND, 60);

		// fetch every REPEAT_TIME seconds. InexactRepeating allows Android to optimize the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, boot_time.getTimeInMillis(), REPEAT_TIME, pending);

		Log.v(TAG, "ScheduleReceiver fired");
		
	}

}
