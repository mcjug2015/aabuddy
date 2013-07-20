package org.mcjug.aameetingmanager.meeting;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.R;
import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class PostMeetingNotThereTask extends AsyncTask<Void, String, String> {
    private final String TAG = getClass().getSimpleName();

    private final Context context;
	private final int meetingId;
	private final String note;
	private final ProgressDialog progressDialog;

    public PostMeetingNotThereTask(Context context, int meetingId, String note, ProgressDialog progressDialog) {
        super();
        this.context = context;
        this.meetingId = meetingId;
        this.progressDialog = progressDialog;
        this.note = note;
    }

	@Override
	protected String doInBackground(Void... arg0) {
		String errorMessage = null;

		String url = HttpUtil.getSecureRequestUrl(context, R.string.post_meeting_not_there_url_path);
		HttpClient client = HttpUtil.createHttpClient();
		try {
			HttpPost httpPost = new HttpPost(url);

			Credentials credentials = Credentials.readFromPreferences(context);
			String uniqueDeviceId = MeetingListUtil.getUniqueDeviceId(context);

			if (credentials != null) {
				httpPost.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
			}

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("meeting_id", Integer.toString(meetingId)));
			nameValuePairs.add(new BasicNameValuePair("unique_phone_id", uniqueDeviceId));
			if (!TextUtils.isEmpty(note)) {
			    nameValuePairs.add(new BasicNameValuePair("note", note));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse httpResponse = client.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				errorMessage = httpResponse.getStatusLine().toString();
				Log.d(TAG, "Response code for create user=" + statusCode + "; status line: " + errorMessage);
			}

		} catch (Exception e) {
			Log.d(TAG, "Exception creating user", e);
			errorMessage = "Unexpected error";
		} finally {
			client.getConnectionManager().shutdown();
		}


		return errorMessage;
	}


	@Override
	protected void onPostExecute(String errorMessage) {
		progressDialog.dismiss();

		if (errorMessage == null) {
			//Save meeting id to "not there" list
			AAMeetingApplication.getInstance().addToMeetingNotThereList(meetingId);
			displaySuccessMessage();
		} else {
			displayErrorMessage(errorMessage);
		}
	}

	private void displaySuccessMessage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(R.string.postMeetingNotThereSuccessDialogTitle)
			   .setMessage(R.string.postMeetingNotThereSuccessMessage)
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
                    public void onClick(DialogInterface dialog, int which) {
						//TODO:  ATL - do the disable of "Not There" button here?

						dialog.dismiss();
					}
				});

		builder.show();
	}

	private void displayErrorMessage(String errorMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		String dialogMessage = context.getString(R.string.postMeetingNotThereErrorMessage)
			+ " " + errorMessage;

		builder.setTitle(R.string.postMeetingNotThereErrorDialogTitle)
			   .setMessage(dialogMessage)
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
                    public void onClick(DialogInterface dialog, int which) {
						//TODO:  ATL - re-enable "Not There" button?

						dialog.dismiss();
					}
				});

		builder.show();
	}
}
