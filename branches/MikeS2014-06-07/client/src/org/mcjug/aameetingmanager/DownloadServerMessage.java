package org.mcjug.aameetingmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.mcjug.aameetingmanager.util.ServiceConfig;

public class DownloadServerMessage extends Service {
	static final String TAG = "DownloadServerMessage";
	
	private int result = Activity.RESULT_CANCELED;
	public static final String URL = "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "DownloaderServiceBroadcast";
	static final SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-M-dd hh:mm:ss", Locale.US);
	private Date mDate;
	
	private final IBinder mBinder = new ServiceBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "DownloadServerMessage onBind");
		return mBinder;
	}

	@Override
	  public void onCreate() {
		  super.onCreate();
		  mDate = new Date();
		  Log.v(TAG, "Service onCreate:" + sdf.format(mDate));
		  result = Activity.RESULT_OK;
		  publishResults("", "starting service", result);
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
	
	static final String jsonSource = "{\"meta\": {\"limit\": 20, \"next\": null, \"offset\": 0, \"previous\": null, \"total_count\": 1}, \"objects\": [{\"created_date\": \"2014-08-02T10:34:08.777379\", \"id\": 7, \"is_active\": true, \"message\": \"Message to test Schiang's service\", \"resource_uri\": \"/meetingfinder/api/v1/server_message/7\", \"short_message\": \"Testing Schiang's service\", \"updated_date\": \"2014-08-02T10:34:08.777410\"}]}";
	
	protected void handleIntent(final Intent intent) {
		new Thread(new Runnable() {
			public void run() {
				// String urlPath = intent.getStringExtra(URL);
				StringBuilder sb = new StringBuilder();
				InputStream stream = null;
				try {
					Log.v(TAG, "DownloadServerMessage try start on " + URL);
					URL url = new URL(URL);
					Log.v(TAG, "DownloadServerMessage URL created");
					stream = url.openConnection().getInputStream();
					Log.v(TAG, "DownloadServerMessage Connection open");
					InputStreamReader reader = new InputStreamReader(stream);
					Log.v(TAG, "DownloadServerMessage reader initiated");

					char[] buffer = new char[4*1024];
					int next = -1;
					while ((next = reader.read(buffer, 0, buffer.length)) != -1) {
						sb.append(buffer, 0, next);
					}
					Log.v(TAG, "DownloadServerMessage buffer processed");
					result = Activity.RESULT_OK;
					publishResults(sb.toString(), getServiceTicker(), result);
					Log.v(TAG, "DownloadServerMessage result published");
					setLoadedString(sb.toString());
					/*
						publishResults(jsonSource, result);
						setLoadedString(jsonSource);
					 */
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

	private int tickerNumber = 0;

	private String getServiceTicker () {
		tickerNumber++;
		Log.v(TAG, "DownloaderService Ticker " + tickerNumber);
		String currentTimeString = new SimpleDateFormat("> HH:mm", Locale.US).format(new Date());
		return ("SRV" + tickerNumber + currentTimeString);
	}
	
	private void publishResults(String loadedMessage, String serviceStatus, int result) {
		Log.v(TAG, "DownloadServerMessage publishResults");
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(ServiceConfig.LOADEDMESSAGE, loadedMessage);
		intent.putExtra(ServiceConfig.SERVICESTATUS, serviceStatus);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}

}
