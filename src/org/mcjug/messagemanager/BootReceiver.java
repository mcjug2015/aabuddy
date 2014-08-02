package org.mcjug.messagemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private final String TAG = getClass().getSimpleName();

	public void onReceive(Context context, Intent callingIntent) {
		Log.d(TAG, "BootReceiver ************************");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean invokeMessageServiceOnceOnBoot = prefs.getBoolean("invokeMessageServiceOnceOnBoot", false);
		if (invokeMessageServiceOnceOnBoot) {
			Intent intent = new Intent(context, MessageService.class);
			context.startService(intent);

		} else {
			MessageAlarmManager.updateAlarm(context);
		}
	}
}
