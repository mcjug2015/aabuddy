package org.mcjug.aameetingmanager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.mcjug.aameetingmanager.util.HttpUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SubmitMeetingTask extends AsyncTask<Void, String, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String submitMeetingParams;
	private Credentials credentials;

	public SubmitMeetingTask(Context context, String submitMeetingParams, Credentials credentials) {
        this.context = context;
        this.submitMeetingParams = submitMeetingParams;
        this.credentials = credentials;
	}

	@Override
	protected String doInBackground(Void... arg0) {
		String errorMessage = credentials.validateCredentialsFromServer(context);
		if (errorMessage != null) {
			return String.format(context.getString(R.string.validateCredentialsError), errorMessage);
		}

		DefaultHttpClient client = HttpUtil.createHttpClient(); 
		try {
			String baseUrl = HttpUtil.getSecureRequestUrl(context, R.string.save_meeting_url_path);
			HttpPost request = new HttpPost(baseUrl);
			
	        request.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
			
			StringEntity se = new StringEntity(submitMeetingParams);  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			
			HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode == HttpStatus.SC_OK) {
	    		try {
  	    			String paramStr = "meeting_id=" + getMeetingId(response);
	    			FindMeetingTask findMeetingTask = new FindMeetingTask(context, paramStr, true);
	    			findMeetingTask.execute();
	    		} catch (Exception ex) {
		        	return String.format(context.getString(R.string.submitMeetingError), ex);
				}
	        } else {	
	        	return String.format(context.getString(R.string.submitMeetingError), response.getStatusLine().toString());
	        }
		} catch (Exception ex) {
        	return String.format(context.getString(R.string.submitMeetingError), ex);
		} finally {
			client.getConnectionManager().shutdown();  
		}
		return null;
	}
	
	private String getMeetingId(HttpResponse response) throws Exception {
		String id = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			StringBuilder builder = new StringBuilder();
			InputStream inputStream = entity.getContent();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				id = builder.toString();
			} finally {
				inputStream.close();
			}
		}		
		return id;
	}
	
	@Override
	protected void onPostExecute(String errorMsg) {
		super.onPostExecute(errorMsg);
		if (errorMsg != null) {	
			Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
		}
	}
}
