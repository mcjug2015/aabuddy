package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.meetingfinder.R;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetMeetingTypesTask extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getSimpleName();
    private Context context;

    public GetMeetingTypesTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String jsonStr = null;
        HttpURLConnection connection = null;
        try {
            String urlStr = HttpUtil.getSecureRequestUrl(context, R.string.get_meeting_types_url_path);
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                jsonStr = HttpUtil.getContent(connection.getInputStream());
            } else {
                Log.d(TAG, "Exception getting meeting types " + responseCode);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception getting meeting types", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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
