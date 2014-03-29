package org.mcjug.aameetingmanager;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.ResetPasswordFragmentActivity;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class AdminPrefsActivity extends SherlockPreferenceActivity {

	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Admin preferences
		addPreferencesFromResource(R.xml.adminprefs);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Preference resetPasswordPreference = findPreference(getString(R.string.resetPasswordPreferenceKey));
		Intent intent = resetPasswordPreference.getIntent();
		intent.putExtra(ResetPasswordFragmentActivity.UP_INTENT_CLASS_NAME, AdminPrefsActivity.this.getClass().getName());
		resetPasswordPreference.setIntent(intent);

		Preference changePasswordPreference = findPreference(getString(R.string.changePasswordPreferenceKey));
		Credentials credentials = Credentials.readFromPreferences(this);
		changePasswordPreference.setEnabled(credentials.isSet());
		
		
		
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		//boolean boolRecoveryDate = prefs.getBoolean(this.getApplicationContext().getString(R.string.recoveryDateAllowed), true);
		
		boolean boolRecoveryDate = DateTimeUtil.getRecoveryDateAllowed(AdminPrefsActivity.this.getApplicationContext());
		
		Preference recoveryDateAllowedCheckbox = findPreference(getString(R.string.recoveryDatePreferencesKey));
		
		if (recoveryDateAllowedCheckbox != null) {
			
			CheckBoxPreference checkBoxPreference = (CheckBoxPreference) recoveryDateAllowedCheckbox;
			checkBoxPreference.setChecked(boolRecoveryDate);
			
			recoveryDateAllowedCheckbox.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					saveRecoveryDateAllowedValue();
					return true;
				}
			});
		}
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
	
	private void saveRecoveryDateAllowedValue () {
		
		@SuppressWarnings("deprecation")
		Preference recoveryDateAllowedCheckbox = findPreference(getString(R.string.recoveryDatePreferencesKey));
		CheckBoxPreference cbRecoveryDateAllowed = (CheckBoxPreference) recoveryDateAllowedCheckbox;
		boolean boolRecoveryDate = cbRecoveryDateAllowed.isChecked();
		DateTimeUtil.setRecoveryDateAllowed(AdminPrefsActivity.this.getApplicationContext(), boolRecoveryDate);
		
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext().getApplicationContext());
		//Editor editor = prefs.edit();
		//editor.putBoolean(this.getApplicationContext().getString(R.string.recoveryDateAllowed), boolRecoveryDate);
		//editor.commit();
	}
	
}
