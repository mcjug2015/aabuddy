package org.mcjug.aameetingmanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.authentication.LogoutDialogFragment;
import org.mcjug.aameetingmanager.help.HelpFragmentActivity;
import org.mcjug.aameetingmanager.meeting.FindMeetingFragmentActivity;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
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
		
		initRecoveryText();
		/*
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);
		
		if (DateTimeUtil.getRecoveryDateAllowed (getApplicationContext())){
			Calendar recoveryDate = DateTimeUtil.getRecoveryDate(getApplicationContext());
			if (recoveryDate != null) {
				Calendar today = Calendar.getInstance();
				long diffInMillisec = today.getTimeInMillis() - recoveryDate.getTimeInMillis();
				if (diffInMillisec >= 1) {
					long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000);
					String s;
					s = "Congratulations on your " + diffInDays + DateTimeUtil.getOrdinalFor ((int) diffInDays) + " day\r\n of recovery";
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
					textView.setText(s);
				}
				else {
					textView.setText("Select a recovery date in past");
				}
			}
			else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(R.string.recoveryDatePrompt);
				textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
				textView.setVisibility(View.VISIBLE);
			}
		}
		else {
			textView.setVisibility(View.GONE);
			textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
			textView.setVisibility(View.GONE);
		}
		*/
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
		initRecoveryText();
	}

	public void onLogoutDialogPositiveClick(DialogFragment dialog) {
		
		Credentials.removeFromPreferences(getApplicationContext());
		
		initLoginLogoutButton();
	}
	
	/***************************************************************/
	
	public void initRecoveryText () {
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);
		// textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
		
		if (DateTimeUtil.getRecoveryDateAllowed (getApplicationContext())){
			Calendar recoveryDate = DateTimeUtil.getRecoveryDate(getApplicationContext());
			if (recoveryDate != null) {
				Calendar today = Calendar.getInstance();
				long diffInMillisec = today.getTimeInMillis() - recoveryDate.getTimeInMillis();
				if (diffInMillisec >= 1) {
					long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000);
					String s = "Congratulations on your " + diffInDays + 
									DateTimeUtil.getOrdinalFor ((int) diffInDays) + " day of recovery ";
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
					textView.setText(s);
				}
				else {
					textView.setText("Select a recovery date in past");
				}
			}
			else {
				textView.setVisibility(View.VISIBLE);
				textView.setText(R.string.recoveryDatePrompt);
				textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
				textView.setVisibility(View.VISIBLE);
			}
		}
		else {
			textView.setVisibility(View.GONE);
			textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
			textView.setVisibility(View.GONE);
		}
	}
	
	
	
	public static class DatePickerFragment extends DialogFragment
    					implements DatePickerDialog.OnDateSetListener {
		@Override
	    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the current date as the default date in the picker
	        final Calendar c = Calendar.getInstance();
	        int year = c.get(Calendar.YEAR);
	        int month = c.get(Calendar.MONTH);
	        int day = c.get(Calendar.DAY_OF_MONTH);

	        // Create a new instance of DatePickerDialog and return it
	        return new DatePickerDialog(getActivity(), this, year, month, day);
	    }

	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	    	TextView topTextView = (TextView) getActivity(). findViewById(R.id.textViewRecoveryDate);
	    	Calendar recoveryDate = Calendar.getInstance();
	    	
	    	recoveryDate.set(Calendar.YEAR, year);
	    	recoveryDate.set(Calendar.MONTH, monthOfYear);
	    	recoveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	    	DateTimeUtil.setRecoveryDate( this.getActivity().getApplicationContext(), recoveryDate);
	    	
	    	//String myFormat = "'Recovery Date: 'MM/dd/yy"; //In which you need put here
	     	//SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);	     	
	     	//topTextView.setText(sdf.format(recoveryDate.getTime()));
	    	
	    	Calendar today = Calendar.getInstance();
	    	topTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
	    	
	    	long diffInMillisec = today.getTimeInMillis() - recoveryDate.getTimeInMillis();
			if (diffInMillisec >= 1) {
				long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000);
				String s = "Congratulations on your " + diffInDays + 
								DateTimeUtil.getOrdinalFor ((int) diffInDays) + " day of recovery ";
				topTextView.setText(s);
			}
			else {
				topTextView.setText("Select a recovery date in past");
			}
	    }
	}
	
	
	public void onClickShowDatePickerDialog(View v) {
		DialogFragment datePickerFragment = new DatePickerFragment();
	    datePickerFragment.show(getSupportFragmentManager(), "datePicker");
	};
	
	public void onClickBlockRecoveryDate (View v) {
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);
		textView.setVisibility(View.GONE);
		textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
		textView.setVisibility(View.GONE);
		// DateTimeUtil.resetRecoveryDate(this.getApplicationContext());
		DateTimeUtil.setRecoveryDateAllowed(this.getApplicationContext(), false);
	}
	

}
