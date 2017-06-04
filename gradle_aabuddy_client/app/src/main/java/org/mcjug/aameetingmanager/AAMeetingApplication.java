package org.mcjug.aameetingmanager;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mcjug.aameetingmanager.meeting.GetMeetingTypesTask;
import org.mcjug.aameetingmanager.meeting.MeetingListResults;
import org.mcjug.aameetingmanager.meeting.MeetingType;
import org.mcjug.meetingfinder.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AAMeetingApplication extends Application {
    private static final String TAG = AAMeetingApplication.class.getSimpleName();
    private MeetingListResults meetingListResults;
    private List<Integer> meetingNotThereList = null;
    private List<MeetingType> meetingTypes = new ArrayList<MeetingType>();

    // instance
    private static AAMeetingApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        LocationFinder locationTask = new LocationFinder(this, null);
        locationTask.requestLocation();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String meetingTypesJsonStr = prefs.getString(getString(R.string.meetingTypesKey), null);
        if (meetingTypesJsonStr == null) {
            Editor editor = prefs.edit();
            editor.putString(getString(R.string.meetingTypesKey), getMeetingTypesDefault());
            editor.commit();
        }

        try {
            setMeetingTypes();
        } catch (Exception e) {
            Log.d(TAG, "Exception setting meeting types", e);
        }

        new GetMeetingTypesTask(this).execute();

        // set instance for our static accessor
        instance = this;
    }

    private String getMeetingTypesDefault() {
        StringBuilder meetingTypesDefault = new StringBuilder();
        try {
            InputStream in = getResources().openRawResource(R.raw.meetingtypes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                meetingTypesDefault.append(line);
            }
            reader.close();
        } catch (Exception ex) {
        }
        return meetingTypesDefault.toString();
    }

    public MeetingListResults getMeetingListResults() {
        return meetingListResults;
    }

    public void setMeetingListResults(MeetingListResults meetingListResults) {
        this.meetingListResults = meetingListResults;
    }

    public List<MeetingType> getMeetingTypes() {
        return meetingTypes;
    }

    public void setMeetingTypes() throws Exception {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String meetingTypesJsonStr = prefs.getString(getString(R.string.meetingTypesKey), null);

        JSONObject jsonObj = new JSONObject(meetingTypesJsonStr);
        JSONArray meetingTypesJson = jsonObj.getJSONArray("objects");
        if (meetingTypesJson != null) {
            List<MeetingType> types = new ArrayList<MeetingType>();

            JSONObject meetingTypeJson;
            MeetingType meetingType;
            for (int i = 0; i < meetingTypesJson.length(); i++) {
                meetingTypeJson = meetingTypesJson.getJSONObject(i);

                meetingType = new MeetingType();
                meetingType.setId(meetingTypeJson.getInt("id"));
                meetingType.setName(meetingTypeJson.getString("name"));
                meetingType.setDescription(meetingTypeJson.getString("description"));
                meetingType.setResourceUri(meetingTypeJson.getString("resource_uri"));
                meetingType.setShortName(meetingTypeJson.getString("short_name"));

                types.add(meetingType);
            }

            Collections.sort(types, new Comparator<MeetingType>() {
                @Override
                public int compare(MeetingType type, MeetingType type1) {
                    return type.getName().compareTo(type1.getName());
                }
            });

            meetingTypes = types;
        }
    }

    /**
     * Convenient accessor to get context, so that context doesn't need to be passed around
     */
    public static AAMeetingApplication getInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");

        return instance;
    }

    public void addToMeetingNotThereList(int meetingId) {
        if (meetingNotThereList == null) {
            meetingNotThereList = new ArrayList<Integer>();
        }

        meetingNotThereList.add(meetingId);
    }

    public List<Integer> getMeetingNotThereList() {
        return meetingNotThereList;
    }

}
