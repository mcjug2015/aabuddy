package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.content.Context;
import android.os.AsyncTask;

public class FindSimilarMeetingsTask extends AsyncTask<Void, String, JSONObject> {
	private final String TAG = getClass().getSimpleName();
	private Context context;
	private String submitMeetingParams;
	private FindSimilarMeetingsListener listener;

	public FindSimilarMeetingsTask(Context context, String submitMeetingParams, FindSimilarMeetingsListener listener) {
		this.context = context;
		this.submitMeetingParams = submitMeetingParams;
		this.listener = listener;
	}

	@Override
	protected JSONObject doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient(); 
		JSONObject jsonResponse = new JSONObject();
		try {
			String baseUrl = HttpUtil.getUnsecureRequestUrl(context, R.string.find_similar_meetings_url_path);
			HttpPost request = new HttpPost(baseUrl);

			StringEntity se = new StringEntity(submitMeetingParams);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);

			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			    JSONArray jsonMeetings = MeetingListUtil.getMeetingList(response);
				jsonResponse.put("success", true);
				jsonResponse.put("meetings", jsonMeetings);
			} else {
				jsonResponse = getErrorResponse(statusLine.toString());
			}
		} catch (Exception ex) {
			jsonResponse = getErrorResponse(ex.toString());
		} finally {
			client.getConnectionManager().shutdown();
		}
		
		return jsonResponse;
	}

	private JSONObject getErrorResponse(String errorMsg) {
		JSONObject jsonResponse = new JSONObject();
		try {
			jsonResponse.put("success", false);
			jsonResponse.put("errorMsg", String.format(context.getString(R.string.submitMeetingError), errorMsg));
		} catch (JSONException e) {
		}
		return jsonResponse;
	}
	
	@Override
	protected void onPostExecute(JSONObject results) {
		if (listener != null) {
			listener.findSimilarMeetingsResults(results);
		}
	}

	public interface FindSimilarMeetingsListener {
		public void findSimilarMeetingsResults(JSONObject results);
	}
}
