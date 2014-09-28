package org.mcjug.aameetingmanager.meeting;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class GetMeetingTypesTask extends AsyncTask<Void, Void, String> {
	private final String TAG = getClass().getSimpleName();
	private Context context;

	public GetMeetingTypesTask(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient();
		String jsonStr = null;
		try {
			String url = HttpUtil.getSecureRequestUrl(context, R.string.get_meeting_types_url_path);
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				jsonStr = HttpUtil.getContent(response);
			} else {
				Log.d(TAG, "Exception getting meeting types " + statusLine.getStatusCode());
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception getting meeting types", e);
		} finally {
			client.getConnectionManager().shutdown();
		}

		return jsonStr;
	}

	@Override
	protected void onPostExecute(String meetingTypesJsonStr) {
		if (meetingTypesJsonStr != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString(context.getString(R.string.meetingTypesKey), meetingTypesJsonStr);
			editor.commit();
			
			try {
				AAMeetingApplication application = AAMeetingApplication.getInstance();
				application.setMeetingTypes();
			} catch (Exception e) {
				Log.d(TAG, "Exception setting meeting types", e);
			}
		}	
	}
	
}
