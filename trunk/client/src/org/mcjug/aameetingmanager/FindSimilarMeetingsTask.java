package org.mcjug.aameetingmanager;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class FindSimilarMeetingsTask extends AsyncTask<Void, String, List<Meeting>> {
	private final String TAG = getClass().getSimpleName();
	private Context context;
	private String submitMeetingParams;
	private FindSimilarMeetingsListener listener;
	private ProgressDialog progressDialog;
	
	private String errorMsg =  null;
    private boolean isSuccess = true;

	public FindSimilarMeetingsTask(Context context, String submitMeetingParams, FindSimilarMeetingsListener listener) {
		this.context = context;
		this.submitMeetingParams = submitMeetingParams;
		this.listener = listener;
        progressDialog = new ProgressDialog(context);        
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.setTitle(context.getString(R.string.submitMeetingProgressMsg));
		progressDialog.setMessage(context.getString(R.string.waitMsg));
		progressDialog.show();
	}

	@Override
	protected List<Meeting> doInBackground(Void... arg0) {
		HttpClient client = HttpUtil.createHttpClient(); 
		List<Meeting> meetings = null;
		try {
			String baseUrl = HttpUtil.getUnsecureRequestUrl(context, R.string.find_similar_meetings_url_path);
			HttpPost request = new HttpPost(baseUrl);

			StringEntity se = new StringEntity(submitMeetingParams);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);

			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				meetings = MeetingListUtil.getMeetingList(context, response);
			} else {
				isSuccess = false;
		    	errorMsg = statusLine.toString();
			}
		} catch (Exception ex) {
			isSuccess = false;
	    	errorMsg = ex.toString();
		} finally {
			client.getConnectionManager().shutdown();
		}
		
		return meetings;
	}

	@Override
	protected void onPostExecute(List<Meeting> meetings) {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}		
		
		if (isSuccess) {
			if (listener != null) {
				listener.findSimilarMeetingsResults(meetings);
			}
		} else {	
			Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();		
		}
	}

	public interface FindSimilarMeetingsListener {
		public void findSimilarMeetingsResults(List<Meeting> meetings);
	}
}
