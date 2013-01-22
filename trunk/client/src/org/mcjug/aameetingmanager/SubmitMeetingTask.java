package org.mcjug.aameetingmanager;

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
			String baseUrl = HttpUtil.getRequestUrl(context, R.string.save_meeting_url_path);
			HttpPost request = new HttpPost(baseUrl);
			
	        request.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
			
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
