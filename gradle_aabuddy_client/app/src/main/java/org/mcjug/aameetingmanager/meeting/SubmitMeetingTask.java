package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.os.AsyncTask;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;
import org.mcjug.meetingfinder.R;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubmitMeetingTask extends AsyncTask<Void, String, Meeting> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String submitMeetingParams;
    private Credentials credentials;
    private SubmitMeetingListener listener;
    private String errorMsg = null;

    public SubmitMeetingTask(Context context, String submitMeetingParams, Credentials credentials, SubmitMeetingListener listener) {
        this.context = context;
        this.submitMeetingParams = submitMeetingParams;
        this.credentials = credentials;
        this.listener = listener;
    }

    @Override
    protected Meeting doInBackground(Void... arg0) {
        String errorMessage = credentials.validateCredentialsFromServer(context);
        if (errorMessage != null) {
            errorMsg = String.format(context.getString(R.string.validateCredentialsError), errorMessage);
            return null;
        }

        Meeting meeting = null;
        HttpURLConnection connection = null;
        try {
            String baseUrl = HttpUtil.getSecureRequestUrl(context, R.string.save_meeting_url_path);

            URL url = new URL(baseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(submitMeetingParams);
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonStr = HttpUtil.getContent(connection.getInputStream());
                meeting = MeetingListUtil.getMeetingList(context, jsonStr).getMeetings().get(0);
            } else {
                errorMsg = "Error in Submit Meetings " + responseCode;
            }

        } catch (Exception ex) {
            errorMsg = ex.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return meeting;
    }

    @Override
    protected void onPostExecute(Meeting meeting) {
        listener.submitMeetingResults(meeting, errorMsg);
    }

    public interface SubmitMeetingListener {
        public void submitMeetingResults(Meeting meeting, String errorMsg);
    }
}
