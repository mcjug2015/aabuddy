package org.mcjug.aameetingmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceReceiver  extends BroadcastReceiver {

	static final String TAG = "StartServiceReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, DownloadServerMessage.class);
		service.putExtra(DownloadServerMessage.URL, "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json");
		context.startService(service);
		Log.v(TAG, "StartServiceReceiver fired");
	}

}
