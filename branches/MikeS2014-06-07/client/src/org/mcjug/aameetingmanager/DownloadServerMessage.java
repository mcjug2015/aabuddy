package org.mcjug.aameetingmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DownloadServerMessage extends Service {
	static final String TAG = "DownloadServerMessage";
	private final IBinder mBinder = new ServiceBinder();
	private int result = Activity.RESULT_CANCELED;
	public static final String URL = "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json";
	public static final String LOADEDSTRING = "";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "org.mcjug.servermessagesfinal";

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "DownloadServerMessage onBind");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "DownloadServerMessage onStartCommand");
		handleIntent (intent);
		return Service.START_NOT_STICKY;
	}

	public class ServiceBinder extends Binder {
		DownloadServerMessage getService () {
			Log.v(TAG, "DownloadServerMessage ServiceBinder getService");
			return DownloadServerMessage.this;
		}
	}
	
	private String loadedString;
	public String getLoadedString () {
		return this.loadedString;
	}

	public void setLoadedString(String loadedString) {
		this.loadedString = loadedString;
	}
	
	public int getResult () {
		return result;
	}
	

	protected void handleIntent(final Intent intent) {
		new Thread(new Runnable() {
			   public void run() {
				   String urlPath = intent.getStringExtra(URL);
					StringBuilder sb = new StringBuilder();
					InputStream stream = null;
					try {
						URL url = new URL(urlPath);
						stream = url.openConnection().getInputStream();
						InputStreamReader reader = new InputStreamReader(stream);

						char[] buffer = new char[4*1024];
						int next = -1;
						while ((next = reader.read(buffer, 0, buffer.length)) != -1) {
							sb.append(buffer, 0, next);
						}
						result = Activity.RESULT_OK;
						publishResults(sb.toString(), result);
						setLoadedString(sb.toString());
					} catch (Exception e) {
						Log.v(TAG, "DownloadServerMessage exception: " + e.getCause());
						result = Activity.RESULT_CANCELED;
						e.printStackTrace();
					} finally {
						if (stream != null) {
							try {
								stream.close();
							} catch (IOException e) {
								result = Activity.RESULT_CANCELED;
								e.printStackTrace();
							}
						}
					}
			   }                        
			}).start();
		
	}

	private void publishResults(String loadedMessage, int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(LOADEDSTRING, loadedMessage);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}

}
