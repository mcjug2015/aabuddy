package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import java.util.List;

public class UpdateMeetingTypesTask extends AsyncTask<Void, Void, Void> {
	private final String TAG = getClass().getSimpleName();
	private Context context;
	private int meetingId;
	private List<Integer> meetingTypeIds;

	public UpdateMeetingTypesTask(Context context, int meetingId, List<Integer> meetingTypeIds) {
		this.context = context;
		this.meetingId = meetingId;
		this.meetingTypeIds = meetingTypeIds;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient();
		try {
			String url = HttpUtil.getUnsecureRequestUrl(context, R.string.update_meeting_types_url_path);
			HttpPost request = new HttpPost(url);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("meeting_id", meetingId);
			jsonObject.put("type_ids", new JSONArray(meetingTypeIds));

			StringEntity stringEntity = new StringEntity(jsonObject.toString());
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(stringEntity);

			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				Log.d(TAG, "Exception updating meeting types " + statusLine.getStatusCode());
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception updating meeting types", e);
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}
}
