package org.mcjug.aameetingmanager.meeting;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

public class GetMeetingTypesTask extends AsyncTask<Void, Void, Void> {
	private final String TAG = getClass().getSimpleName();
	private Context context;
	private SparseArray<MeetingType> meetingTypes;

	public GetMeetingTypesTask(Context context, SparseArray<MeetingType>meetingTypes) {
		this.context = context;
		this.meetingTypes = meetingTypes;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient();
		try {
			String url = HttpUtil.getSecureRequestUrl(context, R.string.get_meeting_types_url_path);
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				String jsonStr = HttpUtil.getContent(response);
				setMeetingTypes(jsonStr, meetingTypes);
			} else {
				Log.d(TAG, "Exception getting user types " + statusLine.getStatusCode());
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception getting user types", e);
		} finally {
			client.getConnectionManager().shutdown();
		}

		return null;
	}	

	private void setMeetingTypes(String jsonStr, SparseArray<MeetingType> meetingTypes) throws Exception {
		if (jsonStr != null) {
			JSONObject jsonObj = new JSONObject(jsonStr);
			JSONArray meetingTypesJson = jsonObj.getJSONArray("objects");
			if (meetingTypesJson != null) {
				JSONObject meetingTypeJson;
				MeetingType meetingType;
				for (int i = 0; i < meetingTypesJson.length(); i++) {
					meetingTypeJson = meetingTypesJson.getJSONObject(i);

					meetingType = new MeetingType();
					int id = meetingTypeJson.getInt("id");
					meetingType.setId(id);
					meetingType.setName(meetingTypeJson.getString("name"));
					meetingType.setDescription(meetingTypeJson.getString("description"));
					meetingType.setResourceUri(meetingTypeJson.getString("resource_uri"));
					meetingType.setShortName(meetingTypeJson.getString("short_name"));

					meetingTypes.put(id, meetingType);
				}
			}
		}
	}
}
