package org.mcjug.aameetingmanager.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.mcjug.aameetingmanager.AAMeetingManager;
import org.mcjug.meetingfinder.R;

import java.util.List;

//public class FindMeetingFragmentActivity extends ActionBarActivity
public class FindMeetingFragmentActivity extends AppCompatActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.find_meeting_activity);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
			}
		}
	}

}
