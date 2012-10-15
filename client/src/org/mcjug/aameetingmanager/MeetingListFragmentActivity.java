package org.mcjug.aameetingmanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class MeetingListFragmentActivity extends FragmentActivity {
	private static final String TAG = MeetingListFragmentActivity.class.getSimpleName();
	private String meetingsJson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_list);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			meetingsJson = extras.getString("MEETINGS_JSON");
			Log.d(TAG, "meetingsJson = " + meetingsJson);
		}
    }

	public String getMeetingsJson() {
		return meetingsJson;
	} 
}