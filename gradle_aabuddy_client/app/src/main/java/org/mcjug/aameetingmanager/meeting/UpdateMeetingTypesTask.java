package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
        HttpURLConnection connection = null;
        try {
            String urlStr = HttpUtil.getUnsecureRequestUrl(context, R.string.update_meeting_types_url_path);

            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("meeting_id", meetingId);
            jsonObject.put("type_ids", new JSONArray(meetingTypeIds));
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(jsonObject.toString());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Exception updating meeting types " + responseCode);
            }

        } catch (Exception e) {
            Log.d(TAG, "Exception updating meeting types", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
