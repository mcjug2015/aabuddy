package org.mcjug.aameetingmanager;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.authentication.LogoutDialogFragment;
import org.mcjug.aameetingmanager.help.HelpFragmentActivity;
import org.mcjug.aameetingmanager.meeting.FindMeetingFragmentActivity;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.meetingfinder.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AAMeetingManager extends SherlockFragmentActivity 
	implements LogoutDialogFragment.LogoutDialogListener 
{
	private static final String TAG = AAMeetingManager.class.getSimpleName();
	
	private static final String LOGOUT_TAG = "logoutTag";

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Inflate the layout
		setContentView(R.layout.aa_meeting_manager);

		ImageView findMeetingImageView = (ImageView) findViewById(R.id.findMeetingImageView);
		findMeetingImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), FindMeetingFragmentActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		});

		ImageView submitMeetingImageView = (ImageView) findViewById(R.id.submitMeetingImageView);
		submitMeetingImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), SubmitMeetingFragmentActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

			}
		});
		
		ImageView settingsImageView = (ImageView)findViewById(R.id.settingsImageView);
		settingsImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        startActivity(new Intent(getApplicationContext(), AdminPrefsActivity.class)
                		.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		});

		ImageView helpImageView = (ImageView)findViewById(R.id.helpImageView);
		helpImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), HelpFragmentActivity.class)
        		.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		});
		
	}

	public void initLoginLogoutButton() {
		ImageView loginInImageView = (ImageView)findViewById(R.id.loginImageView);
		final TextView loginTextView = (TextView)findViewById(R.id.loginTextView);
		OnClickListener loginImageViewClickListener = null;
		
		Credentials credentials = Credentials.readFromPreferences(getApplicationContext());
	
		//toggle login/logout functionality
		if (!credentials.isSet()) {
			//show login button
			loginInImageView.setImageResource(R.drawable.login);
			loginTextView.setText(getString(R.string.login));
			loginImageViewClickListener = new OnClickListener() {
				public void onClick(View v) {
		            startActivity(new Intent(getApplicationContext(), LoginFragmentActivity.class)
		            		.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
				}
			};
			
		} else {
			//show logout button
			loginInImageView.setImageResource(R.drawable.logout);
			loginTextView.setText(getString(R.string.logout));
			
			loginImageViewClickListener = new OnClickListener() {
				public void onClick(View v) {
					LogoutDialogFragment logoutDialogFragment = 
						new LogoutDialogFragment();
					
					logoutDialogFragment.show(getSupportFragmentManager(), LOGOUT_TAG);
				}
			};
		}
		
		loginInImageView.setOnClickListener(loginImageViewClickListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		initLoginLogoutButton();
	}

	public void onLogoutDialogPositiveClick(DialogFragment dialog) {
		
		Credentials.removeFromPreferences(getApplicationContext());
		
		initLoginLogoutButton();
	}

}
