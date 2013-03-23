package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class FindMeetingTask extends AsyncTask<Void, String, MeetingListResults> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String meetingParams;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private boolean appendResults = false;
	private String errorMsg =  null;
 
	public FindMeetingTask(Context context, String meetingParams) {
		this(context, meetingParams, false);
	}

	public FindMeetingTask(Context context, String meetingParams, boolean appendResults) {
		this(context, meetingParams, false, null);
	}
	
	public FindMeetingTask(Context context, String meetingParams, boolean appendResults, String progressMsg) {
		this.context = context;
		this.meetingParams = meetingParams;
		this.appendResults = appendResults;
		
		if (progressMsg != null) {
			progressDialog = new ProgressDialog(context);        
			progressDialog.setTitle(progressMsg);
			progressDialog.setMessage(context.getString(R.string.waitMsg));
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());	
	}

	@Override
	protected void onPreExecute() {
		if (progressDialog != null) {
			progressDialog.show();
		}
	}

	@Override
	protected MeetingListResults doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient(); 
		MeetingListResults meetingListResults = null;
		try {  
			int meetingUrlResourceId = R.string.get_meetings_url_path;
			String url = HttpUtil.getUnsecureRequestUrl(context, meetingUrlResourceId) + "?" + meetingParams;
		       
			Editor editor = prefs.edit();
			editor.putString(context.getString(R.string.meetingUrl), url);
			editor.commit();
		       
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				meetingListResults = MeetingListUtil.getMeetingList(context, response);
			} else {
		    	errorMsg = statusLine.toString();
			}

		} catch (Exception e) {  
	    	errorMsg = "Error in find meeting: " + e;
		} finally {
			client.getConnectionManager().shutdown();  
		}
		
		return meetingListResults;
	}
	
	@Override
	protected void onPostExecute(MeetingListResults meetingListResults) {
		try {
			if (progressDialog != null) {
				progressDialog.cancel();
			}
		} catch (Exception e) {
		}		
		
		if (errorMsg == null) {
		    AAMeetingApplication app = (AAMeetingApplication) context.getApplicationContext();
		    if (appendResults) {
		    	MeetingListResults existingResults = app.getMeetingListResults();
		    	existingResults.getMeetings().addAll(meetingListResults.getMeetings());
		    } else {	
		    	app.setMeetingListResults(meetingListResults);
		    }
		    
		    Intent intent = new Intent(context, MeetingListFragmentActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    context.startActivity(intent);		
		} else {	
			Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();		
		}
	}
}
