package org.mcjug.aameetingmanager.scheduleservice;

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

public class DownloaderService extends Service {

        static final String TAG = "DownloaderService";
        private int result = Activity.RESULT_CANCELED;
        public static final String INTENT_NOTIFICATION = "DownloaderServiceBroadcast";


        static final SimpleDateFormat sdf = new SimpleDateFormat(" > yyyy-M-dd hh:mm:ss <", Locale.US);
        private Date mDate;

        @Override
        public void onCreate() {
                super.onCreate();
                mDate = new Date();
                Log.v(TAG, "DownloaderService onCreate:" + sdf.format(mDate));
                result = Activity.RESULT_OK;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
                Log.v(TAG, "DownloaderService onStartCommand");
                handleIntent (intent);
                return Service.START_NOT_STICKY;
        }


        /***Bind section: use it if the service is going to be bound****/
        private final IBinder mBinder = new ServiceBinder();
        @Override
        public IBinder onBind(Intent intent) {
                Log.v(TAG, "DownloaderService onBind");
                return mBinder;
        }
        public class ServiceBinder extends Binder {
                public DownloaderService getService () {
                        Log.v(TAG, "DownloaderService ServiceBinder getService");
                        return DownloaderService.this;
                }
        }
        /***************** End of Bind section *********************/

        public int getResult () {
        	return result;
        }

        private void publishResults(String loadedMessage, int result) {
        	Log.v(TAG, "DownloaderService publishResults loadedMessage: " + loadedMessage);
        	Intent intent = new Intent(INTENT_NOTIFICATION);
        	intent.putExtra(ServiceConfig.LOADEDSTRING, loadedMessage);
        	intent.putExtra(ServiceConfig.RESULT, result);
        	sendBroadcast(intent);
        }
        
        protected void handleIntent(final Intent intent) {
                new Thread(new Runnable() {
                        public void run() {

                                if (intent.getExtras() != null) {
                                        String targetUrl = intent.getStringExtra("URL");
                                        Log.v(TAG, "DownloaderService handleIntent get URL " + targetUrl);
                                        result = readXMLdata(targetUrl);   
                                }
                        }
                }).start();
        }

        private int readXMLdata(String targetUrl) {

                int readingResult = Activity.RESULT_CANCELED;
                StringBuilder sb = new StringBuilder();
                InputStream stream = null;
                try {
                        URL url = new URL(targetUrl);
                        stream = url.openConnection().getInputStream();
                        InputStreamReader reader = new InputStreamReader(stream);

                        char[] buffer = new char[ServiceConfig.bufferSize];
                        int next = -1;
                        while ((next = reader.read(buffer, 0, buffer.length)) != -1) {
                                sb.append(buffer, 0, next);
                        }
                        readingResult = Activity.RESULT_OK;
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        if (stream != null) {
                                try {
                                        stream.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }
                        }
                }
                publishResults(sb.toString(), result);
                return readingResult;
        }



}
