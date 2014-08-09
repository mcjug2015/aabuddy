package org.mcjug.aameetingmanager;

import java.util.Calendar;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.PendingIntent;

import org.mcjug.aameetingmanager.util.ServiceConfig;

public class ScheduleReceiver extends BroadcastReceiver {
	
	static final String TAG = "ScheduleReceiver";
	public static final String NOTIFICATION = "ScheduleReceiverBroadcast";
	private ServiceConfig config;
	// restart service every 30 seconds
	
	@Override
	public void onReceive(Context context, Intent intent) {
		config = new ServiceConfig (context);
/*		
		AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, StartServiceReceiver.class);

		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		Calendar boot_time = Calendar.getInstance();
		// start 60 seconds after boot completed
		boot_time.add(Calendar.SECOND, 60);

		// fetch every REPEAT_TIME seconds. InexactRepeating allows Android to optimize the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, boot_time.getTimeInMillis(), REPEAT_TIME, pending);

		Log.v(TAG, "ScheduleReceiver fired");
	*/
		
		if (config.serviceMode.getServiceRunMode() > 0) {
			
			config.setActiveScheduleReceiver(true);
			Intent intentStartServiceReceiver = new Intent(context, StartServiceReceiver.class);

			PendingIntent pending = PendingIntent.getBroadcast(context, 0, intentStartServiceReceiver, PendingIntent.FLAG_CANCEL_CURRENT);

			Calendar boot_time = Calendar.getInstance();
			// start 60 seconds after boot completed
			boot_time.add(Calendar.SECOND, 60);
			long repeatTime = 1000 * 60 * config.serviceMode.getServiceRunMode();

			AlarmManager alarmManagerService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			// fetch every REPEAT_TIME seconds. InexactRepeating allows Android to optimize the energy consumption
			alarmManagerService.setInexactRepeating(AlarmManager.RTC_WAKEUP, boot_time.getTimeInMillis(), repeatTime, pending);

			Log.v(TAG, "ScheduleReceiver fired, service started "  + 
					config.isCheckboxBootChecked() + "/"+ config.isCheckboxAppLoadChecked() + "/" + config.serviceMode);
			
		}
		else {
			config.setActiveScheduleReceiver(false);
			Log.v(TAG, "ScheduleReceiver fired, not configured to init service ");
		}

		
	}

}
