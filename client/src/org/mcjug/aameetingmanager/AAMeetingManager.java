package org.mcjug.aameetingmanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.authentication.LoginFragmentActivity;
import org.mcjug.aameetingmanager.authentication.LogoutDialogFragment;
import org.mcjug.aameetingmanager.help.HelpFragmentActivity;
import org.mcjug.aameetingmanager.meeting.FindMeetingFragmentActivity;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;

import org.mcjug.aameetingmanager.util.ServiceConfig;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AAMeetingManager extends SherlockFragmentActivity 
implements LogoutDialogFragment.LogoutDialogListener {

	private static final String TAG = AAMeetingManager.class.getSimpleName();	
	private static final String LOGOUT_TAG = "logoutTag";
	private CheckBox mCheckboxBoot, mCheckboxAppLoad;
	private TextView mText;
	//private DownloadServerMessage downloadService = null;
	// private BroadcastReceiver receiver = null;
	private ScheduleReceiver scheduleReceiver;
	private ServerMessage processedServerMessage = null;	
	private ServiceConfig config;
	
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
		
		mText = (TextView) findViewById(R.id.messageFromServer);
			
		initMessageFromServer();

		// updateTicker(receivedBroadcastMessage);
		//handler.postDelayed (runnableTicker, 1000);
		// Show that the message is not fresh:
		// receivedBroadcastMessage += "_";
	
		initRecoveryText();
		
	}
	
	private void initiateScheduleReceiver () {
		scheduleReceiver = new ScheduleReceiver(); 
    	registerReceiver (scheduleReceiver, new IntentFilter(ScheduleReceiver.NOTIFICATION));
		Intent intent = new Intent(ScheduleReceiver.NOTIFICATION);
		sendBroadcast(intent);
	}
	
	
	private String receivedBroadcastMessage = "";

	private BroadcastReceiver localReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "LocalBroadcastReceiver onReceive: Broadcast intent detected " + intent.getAction());
			String status = "Undefined";
			if (intent.hasExtra(ServiceConfig.SERVICESTATUS)) {
				status = intent.getExtras().getString(ServiceConfig.SERVICESTATUS);	
			}
			if (intent.hasExtra(ServiceConfig.LOADEDMESSAGE)) {
				receivedBroadcastMessage = intent.getExtras().getString(ServiceConfig.LOADEDMESSAGE);
				if (receivedBroadcastMessage != "") {
					processServerMessage(receivedBroadcastMessage);
					if (processedServerMessage != null) {
						receivedBroadcastMessage = processedServerMessage.toString();
						Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedBroadcastMessage);
						showServiceMessage();
					}
					else {
						Log.v(TAG, "ServerMessage is empty, status " + status);	
					}
				}
				else {
					Log.v(TAG, "JSON is empty, status " + status);
				}
	        }
			else {
				receivedBroadcastMessage = "Not found, status " + status;
			}
			
			Log.v(TAG, "LocalBroadcastReceiver onReceive broadcastResult: " + receivedBroadcastMessage);
			
			/*
			int resultCode = downloadService.getResult();
			if (resultCode  == RESULT_OK) {
				String stringJson = downloadService.getLoadedString();
				if (stringJson !="") {
					processServerMessage(stringJson);
					if (receivedServerMessage != null) {
						receivedBroadcastMessage = receivedServerMessage.toString();
						Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedBroadcastMessage);
						showServiceMessage();
					}
					else
						Log.v(TAG, "ServerMessage is empty");
				}
				else
					Log.v(TAG, "JSON is empty");
			}
			else
				Log.v(TAG, "Result is not OK");
			*/

		}
	};

	/*
	private int tickerNumber = 0;

	private void updateTicker (String message) {
		tickerNumber++;
		mText.setText(tickerNumber + ". " + message);
	}
	*/

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

	@Override
	protected void onResume() {
		super.onResume();
		initLoginLogoutButton();
		initRecoveryText();
		registerReceiver(localReceiver, new IntentFilter(DownloadServerMessage.NOTIFICATION));	
        Log.v(TAG, "onResume: localBroadcastReceiver registered");
        // && !config.isActiveScheduleReceiver()
        if (config.isCheckboxAppLoadChecked() && (config.serviceMode.getServiceRunMode() > 0)) {
        	initiateScheduleReceiver();
        	Log.v(TAG, "onResume: scheduleReceiver registered");
        }
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    config.saveConfig(getApplicationContext());
        Log.v(TAG, "onPause");
        unregisterReceiver(localReceiver);
	    //handler.removeCallbacks(runnableTicker);
	}
	
	@Override
	protected void onDestroy() {
		doUnbindService();
	    config.saveConfig(getApplicationContext());
	    Log.v(TAG, "onDestroy: broadcastReceiver unregistered");
		super.onDestroy();
	}
	
	@SuppressWarnings("unused")
	private DownloadServerMessage mBoundService;
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.v(TAG, "MainActivity onServiceConnected");
	    	mBoundService = ((DownloadServerMessage.ServiceBinder)service).getService();
	    }
	    public void onServiceDisconnected(ComponentName className) {
	        mBoundService = null;
	    }
	};
	
	boolean mIsBound = false;
	void doBindService() {
		Intent intent = new Intent(this, DownloadServerMessage.class);
	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}
	
	void doUnbindService() {
	    if (mIsBound) {
	        unbindService(mConnection);
	        mIsBound = false;
	    }
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
		if (config.isCheckboxAppLoadChecked()) {
        	if((config.serviceMode.getServiceRunMode() > 0)) {
            	initiateScheduleReceiver();
            	Log.v(TAG, "onCreate: scheduleReceiver registered");        		
        	}
        	else {
        		// If RUN_ONCE run downloader service once
        		startService(new Intent(this, DownloadServerMessage.class));
        		
    			Log.v(TAG, "onCreate: run DownloaderService once");
        	}
        }
        Log.v(TAG, "broadcastReceiver initialized: " + receivedBroadcastMessage + "/" + config.serviceMode.name());
	}

/*
	private Handler handler = new Handler();

	private Runnable runnableTicker = new Runnable() {
		@Override
		public void run() {
			showServerMessage();
			handler.postDelayed(this, 30000);
		}
	};
	
	private void showServerMessage () {
		
		mText.setVisibility(View.GONE);
		if (downloadService != null) {
			int resultCode = downloadService.getResult();
			if (resultCode  == RESULT_OK) {
				String stringJson = downloadService.getLoadedString();
				if (stringJson !="") {
					processServerMessage(stringJson);
					if (receivedServerMessage != null) {
						Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedServerMessage.toString());
						mText.setVisibility(View.VISIBLE);
						String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
						mText.setText(timeStamp + " " + receivedServerMessage.firstShortMessage());
						// textView.setText(receivedServerMessage.toString());
					}
					else
						Log.v(TAG, "ServerMessage is empty");
				}
				else
					Log.v(TAG, "JSON is empty");	
			}
			else 
				Log.v(TAG, "Result not OK: " + resultCode);
		}
		else
			Log.v(TAG, "Service is disconnected");
	}
	*/
	public void processServerMessage (String messageJson) {
		if (messageJson.length() > 0) {
			// Log.v(TAG, "processing " + messageJson);
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Gson gson = gsonBuilder.create();
			processedServerMessage = gson.fromJson(messageJson, ServerMessage.class);
			return;
		}
		processedServerMessage = null;
	}

	public void showServiceMessage () {
		if (processedServerMessage != null) {
			mText.setVisibility(View.VISIBLE);
			String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
			mText.setText(timeStamp + " " + processedServerMessage.firstShortMessage());			
		}
		else
			mText.setVisibility(View.INVISIBLE);
	}
	
	/*
	@SuppressWarnings("unused")
	private DownloadServerMessage downloadService = null;

	@SuppressWarnings("unused")
	private ServiceConnection downloadServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			DownloadServerMessage.ServiceBinder bndr = (DownloadServerMessage.ServiceBinder) binder;
			downloadService = bndr.getService();
			Log.v(TAG, "onServiceConnected");
		}

		public void onServiceDisconnected(ComponentName className) {
			downloadService = null;
			Log.v(TAG, "onServiceDisconnected");
		}
	};
	 */

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
			initiateScheduleReceiver();
			Log.v(TAG, "onButtonStartClick -- ScheduleReceiver ");
		}
		else {
			startService(new Intent(this, DownloadServerMessage.class));
			Log.v(TAG, "onButtonStartClick -- DownloaderService");
		}
		
		doBindService();
	}
	
	public void onButtonStopClick (View v) {
		Log.v(TAG, "onButtonStopClick");
		doUnbindService();
	}
	
	public void onButtonInfoClick (View v) {
		CharSequence text = "Hello there!";
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
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
