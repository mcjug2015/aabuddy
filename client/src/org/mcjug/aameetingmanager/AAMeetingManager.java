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

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
			if (buildRecoveryDateText(this.getApplicationContext(), recoveryDate, textView)) {
				SpannableString link = makeLinkSpan("Date", new View.OnClickListener() {          
	                @Override
	                public void onClick(View view) {
	                	onClickShowDatePickerDialog(view);
	                }
	            });
				textView.append(link);
				makeLinksFocusable(textView);
			}
			// textView.setVisibility(View.VISIBLE);
			ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
			imageButton.setVisibility(View.VISIBLE);
		}
		else {
			
			textView.setVisibility(View.GONE);
			//textView = (TextView) findViewById(R.id.textViewRecoveryDateBlock);
			//textView.setVisibility(View.GONE);
			ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
			imageButton.setVisibility(View.GONE);
		}
	}
	
	public static boolean buildRecoveryDateText(Context context, Calendar recoveryDate, TextView textView) {
		boolean needToAddDate = false;
		textView.setVisibility(View.VISIBLE);
		if (recoveryDate != null) {
			Calendar today = Calendar.getInstance();
			long diffInMillisec = today.getTimeInMillis() - recoveryDate.getTimeInMillis();
			if (diffInMillisec >= 1) {
				long diffInDays = diffInMillisec / (24 * 60 * 60 * 1000) + 1;
				String s = "Congratulations on your " + diffInDays + 
								DateTimeUtil.getOrdinalFor ((int) diffInDays) + " day of recovery";
				textView.setText(s);
			}
			else {
				textView.setText("Select a recovery date in past");
			}
		}
		else {
			textView.setText(context.getString(R.string.recoveryDatePrompt));
			needToAddDate = true;
		}
		return needToAddDate;
	}

	private static void makeLinksFocusable(TextView textView) {
		MovementMethod movementMethodm = textView.getMovementMethod();  
        if ((movementMethodm == null) || !(movementMethodm instanceof LinkMovementMethod)) {  
            if (textView.getLinksClickable()) {  
            	textView.setMovementMethod(LinkMovementMethod.getInstance());  
            }  
        }
	}

	public static class DatePickerFragment extends DialogFragment
    					implements DatePickerDialog.OnDateSetListener {
		@Override
	    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
			// If Recovery DAte was saved, use it as the default in the picker
			Calendar c = DateTimeUtil.getRecoveryDate(getActivity());
			if (c == null) {
		        // If not set, use the current date
		        c = Calendar.getInstance();
			}
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
	    	
	    	buildRecoveryDateText(this.getActivity().getApplicationContext(), recoveryDate, topTextView);

	    }
	}
	
	// private static SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
    private static SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(listener), 0, text.length(), 
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
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
    
	public void onClickShowDatePickerDialog(View v) {
		DialogFragment datePickerFragment = new DatePickerFragment();
	    datePickerFragment.show(getSupportFragmentManager(), "datePicker");
	};
	
	public void onClickHideRecoveryDate (View v) {
		TextView textView = (TextView) findViewById(R.id.textViewRecoveryDate);
		textView.setVisibility(View.GONE);
		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonRecoveryDateHide);
		imageButton.setVisibility(View.GONE);
		DateTimeUtil.setRecoveryDateAllowed(getApplicationContext(), false);
		Toast.makeText(getApplicationContext(), 
				getApplicationContext().getString(R.string.howToRecoverToastNote), 
				Toast.LENGTH_SHORT).show();	
	}
}
