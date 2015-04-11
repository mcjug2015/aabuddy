package org.mcjug.aameetingmanager.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.mcjug.aameetingmanager.AdminPrefsActivity;
import org.mcjug.meetingfinder.R;

public class ChangePasswordFragmentActivity extends ActionBarActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.change_password_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case android.R.id.home:
			// app icon in action bar clicked; go back to admin prefs
			Intent intent = new Intent(this, AdminPrefsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		
		return true;
	}
}
