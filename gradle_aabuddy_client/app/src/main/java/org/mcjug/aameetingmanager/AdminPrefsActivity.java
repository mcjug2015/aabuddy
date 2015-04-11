package org.mcjug.aameetingmanager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.ResetPasswordFragmentActivity;
import org.mcjug.meetingfinder.R;

public class AdminPrefsActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Admin preferences
        addPreferencesFromResource(R.xml.adminprefs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Preference resetPasswordPreference = findPreference(getString(R.string.resetPasswordPreferenceKey));
        Intent intent = resetPasswordPreference.getIntent();
        intent.putExtra(ResetPasswordFragmentActivity.UP_INTENT_CLASS_NAME, AdminPrefsActivity.this.getClass().getName());
        resetPasswordPreference.setIntent(intent);

        Preference changePasswordPreference = findPreference(getString(R.string.changePasswordPreferenceKey));
        Credentials credentials = Credentials.readFromPreferences(this);
        changePasswordPreference.setEnabled(credentials.isSet());
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
