package org.mcjug.aameetingmanager.authentication;

import org.mcjug.aameetingmanager.AdminPrefsActivity;
import org.mcjug.aameetingmanager.R;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ChangePasswordFragmentActivity extends SherlockFragmentActivity {

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
