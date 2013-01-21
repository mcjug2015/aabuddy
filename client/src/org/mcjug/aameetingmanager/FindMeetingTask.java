package org.mcjug.aameetingmanager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.HttpUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class FindMeetingTask extends AsyncTask<Void, String, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String meetingParams;
	private SharedPreferences prefs;

	public FindMeetingTask(Context context, String meetingParams) {
        this.context = context;
        this.meetingParams = meetingParams;
	    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	@Override
	protected String doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient(); 
		try {  
			String baseUrl = getGetMeetingBaseUrl();
			String url = baseUrl + "?" + meetingParams;
		       
			Editor editor = prefs.edit();
			editor.putString(context.getString(R.string.meetingUrl), url);
			editor.commit();
		       
			HttpGet request = new HttpGet(url);
			HttpResponse httpResponse = client.execute(request);
		    String jsonResponse = getMeetingsResponse(httpResponse);
		    Log.d(TAG, "Find meeting jsonResponse: " + jsonResponse);
		    if (jsonResponse == null) {
		    	return context.getString(R.string.noMeetingsFound);
		    }
		    
		    JSONObject jsonObj = new JSONObject(jsonResponse);
		    JSONArray jsonMeetings = jsonObj.getJSONArray("objects");
		    if (jsonMeetings == null || jsonMeetings.length() == 0) {
		    	return context.getString(R.string.noMeetingsFound);
		    }
		    
		    AAMeetingApplication app = (AAMeetingApplication) context.getApplicationContext();
		    app.setMeetingListData(jsonMeetings.toString());
		    
		    Intent intent = new Intent(context, MeetingListFragmentActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    context.startActivity(intent);

		} catch (Exception e) {  
			return "Error in find meeting: " + e;
		} finally {
			client.getConnectionManager().shutdown();  
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (result != null) {
			Toast.makeText(context, result, Toast.LENGTH_LONG).show();		
		}
	}
	
	private String getGetMeetingBaseUrl() {
		StringBuilder baseUrl = new StringBuilder();
		
		String defaultServerBase = context.getString(R.string.meetingServerBaseUrlDefaultValue);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String serverBaseUrl = prefs.getString(context.getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
		
		baseUrl.append(serverBaseUrl);
		baseUrl.append(context.getString(R.string.get_meeting_url_path));
		
		return baseUrl.toString();
	}
	
	private String getMeetingsResponse(HttpResponse httpResponse) throws Exception {
		StringBuilder builder = new StringBuilder();
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} finally {
				inputStream.close();
			}
		}
		return builder.toString();
	}

}
