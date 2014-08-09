package org.mcjug.aameetingmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.mcjug.aameetingmanager.util.ServiceConfig;


public class StartServiceReceiver  extends BroadcastReceiver {

	private ServiceConfig config;
	static final String TAG = "StartServiceReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		/*
		Intent service = new Intent(context, DownloadServerMessage.class);
		service.putExtra(DownloadServerMessage.URL, "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json");
		context.startService(service);
		Log.v(TAG, "StartServiceReceiver fired");
		*/
		
		
		config = new ServiceConfig (context);
		
		// Always start service - it may be a click once or a scheduled event 
		Intent downloaderService = new Intent(context, DownloadServerMessage.class);
		context.startService(downloaderService);
		
		if (config.serviceMode.getServiceRunMode() == 0) {
			Log.v(TAG, "StartServiceReceiver fired, both checkboxes are off and serviceMode == 0, cancelling alarm");
			
			AlarmManager alarmManagerService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intentStartServiceReceiver = new Intent(context, StartServiceReceiver.class);
			PendingIntent pending = PendingIntent.getBroadcast(context, 0, intentStartServiceReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManagerService.cancel(pending);
		}
		else {
			Log.v(TAG, "StartServiceReceiver fired, service restarted " + 
					config.isCheckboxBootChecked() + "/" + config.isCheckboxAppLoadChecked()  + "/" + config.serviceMode);
		}
		
		
		
	}

}
