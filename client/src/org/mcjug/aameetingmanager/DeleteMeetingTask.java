package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mcjug.aameetingmanager.util.HttpUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class DeleteMeetingTask extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private Meeting meeting;
    private DeleteMeetingListener listener;
	private ProgressDialog progressDialog;
 
	public DeleteMeetingTask(Context context, Meeting meeting, DeleteMeetingListener listener) {
		this.context = context;
		this.meeting = meeting;
		this.listener = listener;
		
		progressDialog = new ProgressDialog(context);        
		progressDialog.setTitle(context.getString(R.string.deleteProgressMsg));
		progressDialog.setMessage(context.getString(R.string.waitMsg));
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}

	@Override
	protected String doInBackground(Void... args) {
		Credentials credentials = Credentials.readFromPreferences(context);
		String errorMsg = credentials.validateCredentialsFromServer(context);
		if (errorMsg != null) {
	    	return String.format(context.getString(R.string.validateCredentialsError),errorMsg);
		}

		DefaultHttpClient client = HttpUtil.createHttpClient(); 
		try {
			String baseUrl = HttpUtil.getSecureRequestUrl(context, R.string.delete_meeting_url_path) + "?meeting_id=" + meeting.getId();
			HttpGet request = new HttpGet(baseUrl);			
	        request.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
			
	        HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
		    	errorMsg = statusLine.toString();
			}
		} catch (Exception ex) {
	    	errorMsg = ex.toString();
		} finally {
			client.getConnectionManager().shutdown();  
		}
		
		return errorMsg;
	}
	
	@Override
	protected void onPostExecute(String errorMsg) {
		try {
			progressDialog.cancel();
			
			if (errorMsg == null) {
				listener.deleteMeetingResults(meeting);
			} else {	
				Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();		
			}
		} catch (Exception e) {
		}
	}
	
	public interface DeleteMeetingListener {
		public void deleteMeetingResults(Meeting meeting);
	}
}
