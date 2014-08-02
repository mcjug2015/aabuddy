package org.mcjug.aameetingmanager;

import java.util.Calendar;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.authentication.LogoutDialogFragment;
import org.mcjug.aameetingmanager.help.HelpFragmentActivity;
import org.mcjug.aameetingmanager.meeting.FindMeetingFragmentActivity;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;
import org.mcjug.messagemanager.MessageAlarmManager;
import org.mcjug.messagemanager.MessageService;
import org.mcjug.messagemanager.MessageUpdateReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AAMeetingManager extends SherlockFragmentActivity 
implements LogoutDialogFragment.LogoutDialogListener {

	private static final String TAG = AAMeetingManager.class.getSimpleName();	
	private static final String LOGOUT_TAG = "logoutTag";

	private MessageUpdateReceiver messageUpdateReceiver;

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

		CheckBox messageAlarm = (CheckBox) findViewById(R.id.messageAlarmCheckBox);
		messageAlarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					MessageAlarmManager.updateAlarm(getApplicationContext());
				} else {
					MessageAlarmManager.cancelAlarm(getApplicationContext());
				}
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
			// show logout button
			loginInImageView.setImageResource(R.drawable.logout);
			loginTextView.setText(getString(R.string.logout));

			loginImageViewClickListener = new OnClickListener() {
				public void onClick(View v) {
					LogoutDialogFragment logoutDialogFragment = new LogoutDialogFragment();					
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

		// Register for message updates
		messageUpdateReceiver = new MessageUpdateReceiver();
		IntentFilter filter = new IntentFilter(MessageService.MESSAGE_NOTIFICATION_INTENT_ACTION);
		registerReceiver(messageUpdateReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageUpdateReceiver);
	}

	public void onLogoutDialogPositiveClick(DialogFragment dialog) {
		Credentials.removeFromPreferences(getApplicationContext());
		initLoginLogoutButton();
	}

	private void initRecoveryText () {
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);

		if (DateTimeUtil.getRecoveryDateAllowed(getApplicationContext())) {
			Calendar recoveryDate = DateTimeUtil.getRecoveryDate(getApplicationContext());
			if (buildRecoveryDateText(getApplicationContext(), recoveryDate, textView)) {

				String recoveryDatePrompt = getApplicationContext().getString(R.string.recoveryDatePrompt);
				SpannableString link = new SpannableString(recoveryDatePrompt);
				int spanStart = recoveryDatePrompt.indexOf("Recovery Date");
				makeLinkSpan(link, spanStart, spanStart + "Recovery Date".length(), setDateClickListener);
				makeLinksFocusable(textView);
				textView.setText(link);				
			}

			ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
			imageButton.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
			ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
			imageButton.setVisibility(View.GONE);
		}
	}	

	private View.OnClickListener setDateClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(getApplicationContext(), AdminPrefsActivity.class).
					addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		}
	};

	private static boolean buildRecoveryDateText(Context context, Calendar recoveryDate, TextView textView) {
		boolean needToAddDate = false;
		textView.setVisibility(View.VISIBLE);

		if (recoveryDate != null) {
			Calendar today = Calendar.getInstance();
			long diffInMillisec = today.getTimeInMillis() - recoveryDate.getTimeInMillis();
			if (diffInMillisec >= 1) {
				long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000) + 1;
				String ordinal = DateTimeUtil.getOrdinalFor ((int) diffInDays);
				String congratulationsMsg = context.getString(R.string.recoveryDateCongratulationsMsg);
				String message = String.format(congratulationsMsg, diffInDays, ordinal);

				textView.setText(message);
			}
		} else {
			needToAddDate = true;
		}

		return needToAddDate;
	}

	private void makeLinksFocusable(TextView textView) {
		MovementMethod movementMethodm = textView.getMovementMethod();  
		if ((movementMethodm == null) || !(movementMethodm instanceof LinkMovementMethod)) {  
			if (textView.getLinksClickable()) {  
				textView.setMovementMethod(LinkMovementMethod.getInstance());  
			}  
		}
	}

	private void makeLinkSpan(SpannableString link, int spanStart, int spanEnd, View.OnClickListener listener) {
		link.setSpan(new ClickableString(listener), spanStart, spanEnd, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
	}

	private static class ClickableString extends ClickableSpan {  
		private View.OnClickListener mListener;          
		public ClickableString(View.OnClickListener listener) {              
			mListener = listener;  
		} 

		@Override  
		public void onClick(View v) { 
			mListener.onClick(v);  
		}   
	}    

	public void onClickHideRecoveryDate (View view) {
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);
		textView.setVisibility(View.GONE);

		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
		imageButton.setVisibility(View.GONE);

		DateTimeUtil.setRecoveryDateAllowed(getApplicationContext(), false);

		Toast.makeText(getApplicationContext(), 
				getApplicationContext().getString(R.string.howToRecoverToastNote), 
				Toast.LENGTH_LONG).show();	
	}
}
