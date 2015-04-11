package org.mcjug.aameetingmanager.authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import org.mcjug.meetingfinder.R;

public class ResetPasswordFragmentActivity extends ActionBarActivity {

	public static String UP_INTENT_CLASS_NAME = "upIntentClassName";
	
	private Class<? extends Activity> upIntentClass = LoginFragmentActivity.class;
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (getIntent().getExtras() != null) {
		
			String upIntentClassName = getIntent().getExtras().getString(UP_INTENT_CLASS_NAME);
			if (upIntentClassName != null) {
				try {
					Class<?> classObj = Class.forName(upIntentClassName);
					if (classObj != null && Activity.class.isAssignableFrom(classObj)) {
						upIntentClass = (Class<? extends Activity>)classObj;
					}
				} catch (ClassNotFoundException e) {
					//class not found.  Use default.
				}
				
			}
		}
		
    }
    
 	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go back to the caller of the activity
				Intent upIntent = new Intent(this, upIntentClass);

				NavUtils.navigateUpTo(this, upIntent);
				break;
		}
		return true;
	}
}
