package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

public class SubmitMeetingTask extends AsyncTask<Void, String, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String submitMeetingParams;
    private String username;
    private String password;
	private SharedPreferences prefs;

	public SubmitMeetingTask(Context context, String submitMeetingParams, String username, String password) {
        this.context = context;
        this.submitMeetingParams = submitMeetingParams;
        this.username = username;
        this.password = password;
	    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	@Override
	protected String doInBackground(Void... arg0) {
		DefaultHttpClient client = new DefaultHttpClient(); 
		try {
			String baseUrl = getSaveMeetingBaseUrl();
			HttpPost request = new HttpPost(baseUrl);
			
	        String base64EncodedCredentials = Base64.encodeToString(
	                (username+":"+password).getBytes(), Base64.DEFAULT);
	        base64EncodedCredentials = base64EncodedCredentials.replace("\n", "");
	        request.addHeader("Authorization", "Basic " + base64EncodedCredentials);
			
			StringEntity se = new StringEntity(submitMeetingParams);  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) {
	        	return String.format(context.getString(R.string.submitMeetingError), response.getStatusLine().toString());
	        }
		} catch (Exception ex) {
        	return String.format(context.getString(R.string.submitMeetingError), ex);
		} finally {
			client.getConnectionManager().shutdown();  
		}

		return null;
	}
	
	private String getSaveMeetingBaseUrl() {
		String defaultServerBase = context.getString(R.string.meetingServerBaseUrlDefaultValue);			
		String serverBaseUrl = prefs.getString(context.getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
		
		StringBuilder baseUrl = new StringBuilder();
		baseUrl.append(serverBaseUrl);
		baseUrl.append(context.getString(R.string.save_meeting_url_path));
		
		return baseUrl.toString();
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (result == null) {
			Toast.makeText(context, context.getString(R.string.submitMeetingSuccess), Toast.LENGTH_LONG).show();
		} else {	
			Toast.makeText(context, result, Toast.LENGTH_LONG).show();
		}
	}

}
