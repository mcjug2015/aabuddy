package org.mcjug.aameetingmanager.scheduleservice;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceReceiver  extends BroadcastReceiver {

        static final String TAG = "StartServiceReceiver";
        private ServiceConfig config;
        
        @Override
        public void onReceive(Context context, Intent intent) {
                config = new ServiceConfig (context);
                
                // Always start service - it may be a click once or a scheduled event 
                Intent downloaderService = new Intent(context, DownloaderService.class);
                downloaderService.putExtra("URL", config.getURL());
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
