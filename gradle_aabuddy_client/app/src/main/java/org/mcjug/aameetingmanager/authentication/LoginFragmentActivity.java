package org.mcjug.aameetingmanager.authentication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

//import org.mcjug.aameetingmanager.AAMeetingManager;
import org.mcjug.meetingfinder.R;

public class LoginFragmentActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
		setContentView(R.layout.login);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

}
