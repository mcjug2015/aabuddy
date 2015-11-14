package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;
import org.mcjug.meetingfinder.R;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FindSimilarMeetingsTask extends AsyncTask<Void, String, List<Meeting>> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String submitMeetingParams;
    private FindSimilarMeetingsListener listener;
    private String errorMsg = null;

    public FindSimilarMeetingsTask(Context context, String submitMeetingParams, FindSimilarMeetingsListener listener) {
        this.context = context;
        this.submitMeetingParams = submitMeetingParams;
        this.listener = listener;
    }

    @Override
    protected List<Meeting> doInBackground(Void... arg0) {
        List<Meeting> meetings = null;
        HttpURLConnection connection = null;
        try {
            String baseUrl = HttpUtil.getUnsecureRequestUrl(context, R.string.find_similar_meetings_url_path);
            URL url = new URL(baseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            OutputStreamWriter out = new   OutputStreamWriter(connection.getOutputStream());
            out.write(submitMeetingParams);
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonStr = HttpUtil.getContent(connection.getInputStream());
                meetings = MeetingListUtil.getMeetingList(context, jsonStr).getMeetings();
            } else {
                errorMsg = "Error in Find Similar Meetings " + responseCode;
            }
        } catch (Exception ex) {
            errorMsg = ex.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return meetings;
    }

    @Override
    protected void onPostExecute(List<Meeting> meetings) {
        listener.findSimilarMeetingsResults(meetings, errorMsg);
    }

    public interface FindSimilarMeetingsListener {
        public void findSimilarMeetingsResults(List<Meeting> meetings, String errorMsg);
    }
}
