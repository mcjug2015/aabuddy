package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteMeetingTask extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private Meeting meeting;
    private DeleteMeetingListener listener;

    public DeleteMeetingTask(Context context, Meeting meeting, DeleteMeetingListener listener) {
        this.context = context;
        this.meeting = meeting;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... args) {
        Credentials credentials = Credentials.readFromPreferences(context);
        String errorMsg = credentials.validateCredentialsFromServer(context);
        if (errorMsg != null) {
            return String.format(context.getString(R.string.validateCredentialsError), errorMsg);
        }

        HttpURLConnection connection = null;
        try {
            String baseUrl = HttpUtil.getSecureRequestUrl(context, R.string.delete_meeting_url_path) + "?meeting_id=" + meeting.getId();
            URL url = new URL(baseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                errorMsg = "Error deleting meeting: " + responseCode;
            }

        } catch (Exception ex) {
            errorMsg = ex.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return errorMsg;
    }

    @Override
    protected void onPostExecute(String errorMsg) {
        try {
            if (errorMsg != null) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            } else if (listener != null) {
                listener.deleteMeetingResults(meeting);
            }
        } catch (Exception e) {
        }
    }

    public interface DeleteMeetingListener {
        public void deleteMeetingResults(Meeting meeting);
    }
}
