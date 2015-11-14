package org.mcjug.aameetingmanager.meeting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;
import org.mcjug.meetingfinder.R;

import java.net.HttpURLConnection;
import java.net.URL;

public class FindMeetingTask extends AsyncTask<Void, String, MeetingListResults> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String meetingParams;
    private SharedPreferences prefs;
    private ProgressDialog progressDialog;
    private boolean appendResults = false;
    private String errorMsg = null;

    public FindMeetingTask(Context context, String meetingParams, boolean appendResults, String progressMsg) {
        this.context = context;
        this.meetingParams = meetingParams;
        this.appendResults = appendResults;

        if (progressMsg != null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(progressMsg);
            progressDialog.setMessage(context.getString(R.string.waitMsg));
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Override
    protected void onPreExecute() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    @Override
    protected MeetingListResults doInBackground(Void... arg0) {
        MeetingListResults meetingListResults = null;
        HttpURLConnection connection = null;
        try {
            int meetingUrlResourceId = R.string.get_meetings_url_path;
            String urlStr = HttpUtil.getUnsecureRequestUrl(context, meetingUrlResourceId) + "?" + meetingParams;

            Editor editor = prefs.edit();
            editor.putString(context.getString(R.string.meetingUrl), urlStr);
            editor.commit();

            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonStr = HttpUtil.getContent(connection.getInputStream());
                meetingListResults = MeetingListUtil.getMeetingList(context, jsonStr);
            }

        } catch (Exception e) {
            errorMsg = "Error in find meeting: " + e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return meetingListResults;
    }

    @Override
    protected void onPostExecute(MeetingListResults meetingListResults) {
        try {
            if (progressDialog != null) {
                progressDialog.cancel();
            }
        } catch (Exception e) {
        }

        if (errorMsg == null) {
            AAMeetingApplication app = (AAMeetingApplication) context.getApplicationContext();
            if (appendResults) {
                MeetingListResults existingResults = app.getMeetingListResults();
                existingResults.getMeetings().addAll(meetingListResults.getMeetings());
            } else {
                app.setMeetingListResults(meetingListResults);
            }

            Intent intent = new Intent(context, MeetingListFragmentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
    }
}
