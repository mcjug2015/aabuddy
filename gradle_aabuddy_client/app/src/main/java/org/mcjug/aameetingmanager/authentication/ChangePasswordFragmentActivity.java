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
	}
}
