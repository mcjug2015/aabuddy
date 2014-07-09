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


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AAMeetingManager extends SherlockFragmentActivity 
implements LogoutDialogFragment.LogoutDialogListener {

	private static final String TAG = AAMeetingManager.class.getSimpleName();	
	private static final String LOGOUT_TAG = "logoutTag";
	private DownloadServerMessage downloadService = null;
	private BroadcastReceiver receiver = null;
	private ServerMessage receivedServerMessage = null;	
	
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
		initMessageFromServer();
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
		initMessageFromServer();
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    unbindService(downloadServiceConnection);
	    handler.removeCallbacks(runnableTicker);
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

		Intent intent= new Intent(this, DownloadServerMessage.class);
		bindService(intent, downloadServiceConnection, Context.BIND_AUTO_CREATE);
		Log.v(TAG, "onResume bindService");

		TextView textView = (TextView) findViewById(R.id.messageFromServer);
		//textView.setVisibility(View.GONE);
		textView.setText("init...");

		//showServerMessage();
		handler.postDelayed (runnableTicker, 1000);
	}


	private Handler handler = new Handler();

	private Runnable runnableTicker = new Runnable() {
		@Override
		public void run() {
			showServerMessage();
			handler.postDelayed(this, 60000);
		}
	};

	private void showServerMessage () {
		TextView textView = (TextView) findViewById(R.id.messageFromServer);
		textView.setVisibility(View.GONE);
		if (downloadService != null) {
			int resultCode = downloadService.getResult();
			if (resultCode  == RESULT_OK) {
				String stringJson = downloadService.getLoadedString();
				if (stringJson !="") {
					processServerMessage(stringJson);
					if (receivedServerMessage != null) {
						Log.v(TAG, "ServerMessage created from gson.fromJson: " + receivedServerMessage.toString());
						textView.setVisibility(View.VISIBLE);
						String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
						textView.setText(timeStamp + " " + receivedServerMessage.firstShortMessage());
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
	
	public void processServerMessage (String messageJson) {
		if (messageJson.length() > 0) {
			// Log.v(TAG, "processing " + messageJson);
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Gson gson = gsonBuilder.create();
			receivedServerMessage = gson.fromJson(messageJson, ServerMessage.class);
			return;
		}
		receivedServerMessage = null;
	}

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
