package org.mcjug.aameetingmanager.scheduleservice;


import java.util.List;

import org.mcjug.aameetingmanager.jsonobjects.ServerMessage;
import org.mcjug.aameetingmanager.scheduleservice.ServiceConfig.DataSourceTypes;
import org.mcjug.meetingfinder.R;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ServiceHandler {

        static final String TAG = "ServiceHandler";
        static final String defaultBroadcastMessage = "?";
        private String receivedBroadcastMessage = defaultBroadcastMessage;
        public ScheduleReceiver scheduleReceiver;
        private Context context;
        public static int SERVICE_NOTIFICATION = 1;
        private int previous_result = Activity.RESULT_CANCELED;
        private String jsonSource;
        private ServerMessage serverMessage;
        private DataSourceTypes selectedServiceType;
        private boolean localReceiverRegistered = false;
        private boolean frontEndIsActive = true;
        
        public void setSelectedServiceType(DataSourceTypes selectedServiceType) {
                this.selectedServiceType = selectedServiceType;
        }

        /************  SimpleMessage handlers *************/
        private ServerMessage processServerMessage (String messageJson) {
                if (messageJson.length() > 0) {
                        Log.v(TAG, "processing " + messageJson);
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        Gson gson = gsonBuilder.create();
                        ServerMessage sm = gson.fromJson(messageJson, ServerMessage.class);
                        return (sm);                    
                }
                return null;
        }
        
        public ServerMessage getServerMessage() {
                return serverMessage;
        }
        
        
        /************  Service handlers *************/
        public ServiceHandler (Context context) {
                this.context = context;
        }

        public ServiceHandler (Context context, ServiceConfig.DataSourceTypes sourceType) {
                selectedServiceType = sourceType;
                this.context = context;
        }
        
        public void startScheduler () {
                Log.v(TAG, "initiateScheduleReceiver");
                scheduleReceiver = new ScheduleReceiver();
                context.registerReceiver (scheduleReceiver, new IntentFilter(ScheduleReceiver.NOTIFICATION));
                Intent intent = new Intent(ScheduleReceiver.NOTIFICATION);
                context.sendBroadcast(intent);
        }
        
        public void stopScheduler () {
        	if (scheduleReceiver != null) {
        		if (scheduleReceiver.isRegistered) {
        			Log.v(TAG, "unRegisterReceiver ScheduleReceiver");
        			if (scheduleReceiver != null)
        				context.unregisterReceiver(scheduleReceiver);   
        		}
        		else
        			Log.v(TAG, "unRegisterReceiver ScheduleReceiver is not possible");
        	}
        }
        
        public String getReceivedBroadcastMessage() {
        	Log.v(TAG, "Handler's getReceivedBroadcastMessage: " + receivedBroadcastMessage);
        	return receivedBroadcastMessage;
        }

        public void startReceiver () {
        	if (localReceiverRegistered) {
        		Log.v(TAG, "Handler's startReceiver exception: local Receiver registered");
        	}
        	else {
        		context.registerReceiver(localReceiver, new IntentFilter(DownloaderService.INTENT_NOTIFICATION));
                localReceiverRegistered = true;
                Log.v(TAG, "Handler's startReceiver register local Receiver");
        	}
        }
        
        public void stopReceiver () {
        	if (localReceiverRegistered) {
        		context.unregisterReceiver(localReceiver);
            	localReceiverRegistered = false;
            	Log.v(TAG, "Handler's stopReceiver unregister local Receiver");
        	}
        	else {
        		Log.v(TAG, "Handler's stopReceiver exception: non-registered receiver");
        	}
        }
        
        private BroadcastReceiver localReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                        Log.v(TAG, "Handler's Receiver onReceive: Broadcast intent detected " + intent.getAction());

                        if (intent.hasExtra(ServiceConfig.LOADEDSTRING)) {
                                jsonSource = intent.getExtras().getString(ServiceConfig.LOADEDSTRING);
                                
                                if (selectedServiceType == ServiceConfig.DataSourceTypes.SIMPLE_MESSAGE) {
                                        serverMessage = processServerMessage(jsonSource);
                                        receivedBroadcastMessage = serverMessage.firstShortMessage();   
                                }
                                        
                                if (previous_result == Activity.RESULT_CANCELED) {
                                        int current_result = intent.getExtras().getInt(ServiceConfig.RESULT);
                                        if (current_result != previous_result) {
                                                previous_result = current_result; 
                                                showNotification();
                                        }
                                        else {
                                                Log.v(TAG, "Handler's Receiver onReceive result == previous");  
                                        }
                                }
                                else {
                                        Log.v(TAG, "Handler's Receiver onReceive previous != RESULT_CANCELED");
                                }
                        }
                        else {
                                receivedBroadcastMessage = "LOADEDSTRING not found";
                        }
                        Log.v(TAG, "Handler's Receiver onReceive broadcastResult: " + receivedBroadcastMessage);
                }
        };
        

        public void startServiceOnce (String url) {
                Intent downloaderService = new Intent(context, DownloaderService.class);
                downloaderService.putExtra("URL", url);
                context.startService(downloaderService);
        }
        
        /*** Show a notification when this service has something to say and the front end is not in foreground ***/
        private NotificationManager mNotificationManager = null;

        @SuppressLint("NewApi")
        private void showNotification() {
                if (mNotificationManager == null) {
                        
                	if (isFrontEndActive()) {
                	    Log.v(TAG, "showNotification: frontEnd is active, notification suspended");
                	}
                	else {
                        Log.v(TAG, "ServiceHandler showNotification ");
                        Intent intent = new Intent(context, org.mcjug.aameetingmanager.AAMeetingManager.class);
                                
                        // Creates an explicit intent for an Activity to launch the activity when the user selects this notification
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                                        Notification.FLAG_AUTO_CANCEL);
        
                        Notification notification;
        
                        // Notification.Builder is available since API level 11, for earlier use 
                        // notification = new Notification(R.drawable.actionsplayerpauseicon64x64, text, System.currentTimeMillis());
        
                        notification = new Notification.Builder(context)
                                                                .setContentIntent(pendingIntent)
                                                                .setContentTitle("New Message Downloaded")
                                                                .setContentText("Click Here to review it")
                                                                .setSmallIcon(R.drawable.actionsplayerpauseicon16px)
                                                                .addAction(R.drawable.ic_launcher, context.getString(R.string.app_name), pendingIntent)
                                                                .build();
        
                        // hide the notification after its selected
                        notification.flags |= (Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE);
        
                        mNotificationManager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                        // Send the notification.
                        mNotificationManager.notify(SERVICE_NOTIFICATION, notification);
                	}
                }
        }

        public void cancelNotification () {
                if (mNotificationManager != null) {
                          // Notify user that the service is destroyed
                          mNotificationManager.cancel(R.string.service_stop);                   
                }
        }
        
        private boolean isFrontEndActive() {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                Log.v(TAG, "isFrontEndActive: topActivity " + topActivity.getPackageName() + " context " + context.getPackageName());
                if (!topActivity.getPackageName().equals(context.getPackageName())) {
                    return false;
                }
            }
            else
            	Log.v(TAG, "isFrontEndActive: tasks list isEmpty");
            return true;
        }
        

}