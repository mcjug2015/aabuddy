package org.mcjug.aameetingmanager.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.mcjug.meetingfinder.R;

public class MeetingListFragmentActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
 	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go back to login page
				Intent intent = new Intent(this, FindMeetingFragmentActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);				
				startActivity(intent);
				break;
		}
		return true;
	}
}