package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class FindMeetingTask extends AsyncTask<Void, String, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String meetingParams;
	private SharedPreferences prefs;
	private boolean searchById = false;

	public FindMeetingTask(Context context, String meetingParams) {
		this(context, meetingParams, false);
	}
	
	public FindMeetingTask(Context context, String meetingParams, boolean searchById) {
        this.context = context;
        this.meetingParams = meetingParams;
	    this.searchById = searchById;
	    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	@Override
	protected String doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient(); 
		try {  
			int meetingUrlResourceId = (searchById) ? R.string.get_meeting_by_id_url_path : R.string.get_meetings_url_path;
			String url = HttpUtil.getUnsecureRequestUrl(context, meetingUrlResourceId) + "?" + meetingParams;
		       
			Editor editor = prefs.edit();
			editor.putString(context.getString(R.string.meetingUrl), url);
			editor.commit();
		       
			HttpGet request = new HttpGet(url);
			HttpResponse httpResponse = client.execute(request);
		    JSONArray jsonMeetings = MeetingListUtil.getMeetingList(httpResponse);
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
	protected void onPostExecute(String errorMsg) {
		super.onPostExecute(errorMsg);
		if (errorMsg != null) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();		
		}
	}
}
