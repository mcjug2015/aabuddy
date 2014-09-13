package org.mcjug.aameetingmanager.scheduleservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class ScheduleReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION = "ScheduleReceiverBroadcast";
    static final String TAG = "ScheduleReceiver";
    private ServiceConfig config;
    public boolean isRegistered = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
            
            config = new ServiceConfig (context);
            //if (config.isCheckboxAppLoadChecked() || config.isCheckboxBootChecked() || (config.serviceMode.getServiceRunMode() > 0)) {
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

                    Log.v(TAG, "org.mcjug.schedulerservice.ScheduleReceiver fired, service started "  + 
                                    config.isCheckboxBootChecked() + "/"+ config.isCheckboxAppLoadChecked() + "/" + config.serviceMode +
                                    " repeatTime " + repeatTime );
                    isRegistered = true;
            }
            else {
                    config.setActiveScheduleReceiver(false);
                    Log.v(TAG, "ScheduleReceiver fired, not configured to init service ");
            }
            
    }
}
