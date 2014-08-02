package org.mcjug.aameetingmanager.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.mcjug.aameetingmanager.AAMeetingManager.MessageReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class MessageService extends IntentService {
	public static final String PARAM_IN_MSG = "imsg";
	public static final String PARAM_OUT_MSG = "omsg";

	public MessageService() {
		super("MessageService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String url = intent.getStringExtra(PARAM_IN_MSG);
		JSONObject resultJSON = getJSONObjectFromUrl(url);

		String resultTxt = null;
		try {
			JSONArray tokenList = resultJSON.getJSONArray("objects");
			JSONObject oj = tokenList.getJSONObject(0);
			resultTxt = oj.getString("message");
		} catch (Exception e) {
			Log.v("[JSON Parsering]", "JSON exception", e);
		}
    	Format formatter = new SimpleDateFormat("hh:mm:ss a");
        Log.v("MessageService", "Handling msg: " + resultTxt + "   " + formatter.format(new Date()));

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(MessageReceiver.ACTION_RESPONSE);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
		sendBroadcast(broadcastIntent);
	}

	public JSONObject getJSONObjectFromUrl(String url) {
		JSONObject result = null;

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String retSrc = EntityUtils.toString(entity); // parsing JSON
				//Log.v("[JSON Strting]", retSrc);
				result = new JSONObject(retSrc); // Convert String to JSON Object
			}
		} catch (Exception e) {
			Log.v("[GET REQUEST]", "Network exception", e);
		}

		return result;
	}
}
