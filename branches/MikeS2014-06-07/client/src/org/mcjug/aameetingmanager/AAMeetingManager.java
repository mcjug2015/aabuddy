package org.mcjug.aameetingmanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.authentication.LogoutDialogFragment;
import org.mcjug.aameetingmanager.help.HelpFragmentActivity;
import org.mcjug.aameetingmanager.jsonobjects.*;
import org.mcjug.aameetingmanager.scheduleservice.*;
import org.mcjug.aameetingmanager.meeting.FindMeetingFragmentActivity;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AAMeetingManager extends SherlockFragmentActivity 
			implements LogoutDialogFragment.LogoutDialogListener {

	private static final String TAG = AAMeetingManager.class.getSimpleName();	
	private static final String LOGOUT_TAG = "logoutTag";
	private CheckBox mCheckboxBoot, mCheckboxAppLoad;
	private TextView mText;
	//private DownloadServerMessage downloadService = null;
	//private BroadcastReceiver receiver = null;
	//private ScheduleReceiver scheduleReceiver;
	//private ServerMessage processedServerMessage = null;	
	private ServiceConfig config;
	private ServiceHandler serviceHandler;
	private boolean messageReceiverInitiated = false;
	private boolean refreshServerMessage = false;
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		config = new ServiceConfig (getApplicationContext());
		// Inflate the layout
		setContentView(R.layout.aa_meeting_manager);
		
		addListenerOnCheckboxBoot();
		addListenerOnCheckboxAppLoad();
    	setRadioButton();
		addListenerOnFindMeetingImageView();
		addListenerOnSubmitMeetingImageView ();
		addListenerOnSettingsImageView();
		addListenerOnHelpImageView ();
		
		initMessageFromServer();
		initRecoveryText();
		
	}
	
	
	
	public void addListenerOnCheckboxBoot () {
    	mCheckboxBoot = (CheckBox) findViewById(R.id.checkBoxBoot);
    	mCheckboxBoot.setChecked(config.isCheckboxBootChecked());
   	 	mCheckboxBoot.setOnClickListener(new OnClickListener() {
    	  @Override
    	  public void onClick(View v) {
    		  config.setCheckboxBootIsChecked (((CheckBox) v).isChecked());
    	  }
    	});
      }
    
    public void addListenerOnCheckboxAppLoad () {
    	mCheckboxAppLoad = (CheckBox) findViewById(R.id.checkBoxAppLoad );
    	mCheckboxAppLoad.setChecked(config.isCheckboxAppLoadChecked());
    	mCheckboxAppLoad.setOnClickListener(new OnClickListener() { 
    	  @Override
    	  public void onClick(View v) {
    		  config.setCheckboxAppLoadChecked (((CheckBox) v).isChecked());
    	  }
    	});
      }

    public void setRadioButton () {
    	RadioButton radioButton;
    	switch (config.serviceMode.getServiceRunMode()) {
	    	case 0: radioButton = (RadioButton) findViewById(R.id.radioOnClick); break;
	    	case 1: radioButton = (RadioButton) findViewById(R.id.radioOneMin); break;
	    	default: radioButton = (RadioButton) findViewById(R.id.radioFiveMin); break;
    	}
    	radioButton.setChecked(true);	
    }
    
	public void addListenerOnFindMeetingImageView() {
		ImageView findMeetingImageView = (ImageView) findViewById(R.id.findMeetingImageView);
		findMeetingImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), FindMeetingFragmentActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		});

	}
	
	public void addListenerOnSubmitMeetingImageView() {
		ImageView submitMeetingImageView = (ImageView) findViewById(R.id.submitMeetingImageView);
		submitMeetingImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), SubmitMeetingFragmentActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

			}
		});
	}

	public void addListenerOnSettingsImageView() {
		ImageView settingsImageView = (ImageView)findViewById(R.id.settingsImageView);
		settingsImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), AdminPrefsActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		});
	}

	public void addListenerOnHelpImageView () {
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
	
	private void initiateScheduler () {		
		serviceHandler.startScheduler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initLoginLogoutButton();
		initRecoveryText();
		initMessageFromServer();
        Log.v(TAG, "onResume: localBroadcastReceiver registered");
        // && !config.isActiveScheduleReceiver()
        if (config.isCheckboxAppLoadChecked() && (config.serviceMode.getServiceRunMode() > 0)) {
        	initiateScheduler();
    		startTicker();
        	Log.v(TAG, "onResume: scheduleReceiver registered");
        }
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    config.saveConfig(getApplicationContext());
        Log.v(TAG, "onPause");
        if (serviceHandler != null) {
        	serviceHandler.stopReceiver();
            Log.v(TAG, "onPause: serviceHandler broadcast Receiver unregistered");
            serviceHandler.stopScheduler();	
            messageReceiverInitiated = false;
        }
	}
	
	@Override
	protected void onDestroy() {
	    config.saveConfig(getApplicationContext());
	    Log.v(TAG, "onDestroy: broadcastReceiver unregistered");
	    if (serviceHandler != null) {
		    serviceHandler.stopScheduler();
		    serviceHandler.cancelNotification();
	    }
		super.onDestroy();
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

	private void initMessageFromServer () {
		
		mText = (TextView) findViewById(R.id.messageFromServer);
		
		if (!messageReceiverInitiated) {
			serviceHandler = new ServiceHandler(getApplicationContext(), config.getHandlerDataSourceType());	
			serviceHandler.startReceiver();
			messageReceiverInitiated = true;
		}
		if (config.isCheckboxAppLoadChecked()) {
        	if((config.serviceMode.getServiceRunMode() > 0)) {
            	initiateScheduler();
            	Log.v(TAG, "initMessageFromServer: scheduleReceiver registered");        		
        	}
        	else {
        		// If RUN_ONCE run downloader service once
        		Intent intent = new Intent(this, DownloaderService.class);
        		intent.putExtra("URL", config.getURL());
        		startService(intent);
    			Log.v(TAG, "initMessageFromServer: run DownloaderService once");
        	}
        }
		else {
	    	Log.v(TAG, "initMessageFromServer: scheduleReceiver not registered");
		}
		getServerMessage();
		serviceHandler.cancelNotification();
	}

	private Handler handler = new Handler();

	//@SuppressWarnings("unused")
	private Runnable runnableTicker = new Runnable() {
		@Override
		public void run() {
			if (refreshServerMessage) {
				String receivedMessage = serviceHandler.getReceivedBroadcastMessage();
				showServerMessage(receivedMessage);
				handler.postDelayed(this, 10000);	
			}
			else
				Log.v(TAG, "runnableTicker: stopped");
		}
	};
	
	private void startTicker () {
		if (!refreshServerMessage) {
			refreshServerMessage = true;
			handler.postDelayed(runnableTicker, 1000);
		}		
	}
	
	private void getServerMessage () {
		ServerMessage sm = serviceHandler.getServerMessage();
		if (sm == null) {
			showServerMessage (serviceHandler.getReceivedBroadcastMessage());	
			Log.v(TAG, "onButtonInfoClick: empty getServerMessage, broadcast " + serviceHandler.getReceivedBroadcastMessage());
		}
		else {
			showServerMessage (sm.firstShortMessage());
		}	
	}
	
	public void showServerMessage (String messageText) {
		if (messageText != null) {
			mText.setVisibility(View.VISIBLE);
			String timeStamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime());
			mText.setText(timeStamp + " " + messageText);			
		}
		else
			mText.setVisibility(View.INVISIBLE);
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
	
	
	public void onButtonStartClick(View v) {
		
		if (config.serviceMode != ServiceConfig.ServiceRunModes.RUN_ONCE && !config.isActiveScheduleReceiver()) {
			initiateScheduler();
			startTicker();
			Log.v(TAG, "onButtonStartClick -- ScheduleReceiver ");
		}
		else {
			//startService(new Intent(this, DownloaderService.class));
			serviceHandler.startServiceOnce(config.getURL());
			refreshServerMessage = false;
			Log.v(TAG, "onButtonStartClick -- DownloaderService");
		}
		
        serviceHandler.startReceiver();
	}
	
	public void onButtonStopClick (View v) {
		Log.v(TAG, "onButtonStopClick");
		refreshServerMessage = false;
	}
	
	public void onButtonInfoClick (View v) {
		if (config.getHandlerDataSourceType() == ServiceConfig.DataSourceTypes.SIMPLE_MESSAGE ) {
			getServerMessage();
		}
		else
			Log.v(TAG, "onButtonInfoClick: config.getHandlerDataSourceType == " + config.getHandlerDataSourceType());
	}
	
	public void onButtonConfigureClick(View v) {
    	config.saveConfig(getApplicationContext());
	}
	
	public void onRadioButtonClicked(View v) {
	    RadioButton button = (RadioButton) v;
	    switch(button.getId()) {
	    	case R.id.radioOnClick:
	    		config.serviceMode = ServiceConfig.ServiceRunModes.RUN_ONCE;
	    		break;
	    	case R.id.radioOneMin:
	    		config.serviceMode = ServiceConfig.ServiceRunModes.ONE_MIN;
	    		break;
	    	default:
	    		config.serviceMode = ServiceConfig.ServiceRunModes.FIVE_MIN;
	    		break;
	    }
	    Log.v(TAG, "onRadioButtonClicked " + button.getText() + " service mode " + config.serviceMode);
	}
	
	
	
}
