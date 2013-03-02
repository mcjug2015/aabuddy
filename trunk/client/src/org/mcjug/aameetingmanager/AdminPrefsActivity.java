package org.mcjug.aameetingmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class AdminPrefsActivity extends SherlockPreferenceActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Admin preferences
		addPreferencesFromResource(R.xml.adminprefs);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		ListView listView = getListView();
		
		Button resetPasswordButton = new Button(this);
		resetPasswordButton.setText(R.string.resetPasswordButton);
		resetPasswordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AdminPrefsActivity.this, ResetPasswordFragmentActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				
				intent.putExtra(ResetPasswordFragmentActivity.UP_INTENT_CLASS_NAME, AdminPrefsActivity.this.getClass().getName());
				
				startActivity(intent);
            }
        });		
        listView.addFooterView(resetPasswordButton);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, AAMeetingManager.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
		}
		return true;
	}
}

