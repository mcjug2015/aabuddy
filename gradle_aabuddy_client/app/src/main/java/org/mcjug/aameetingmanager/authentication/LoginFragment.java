package org.mcjug.aameetingmanager.authentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.mcjug.aameetingmanager.AAMeetingManager;
import org.mcjug.aameetingmanager.meeting.SubmitMeetingFragmentActivity;
import org.mcjug.meetingfinder.R;

public class LoginFragment extends Fragment {
	private static final String TAG = LoginFragment.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		// Inflate the layout for this fragment		
		final View view = inflater.inflate(R.layout.login_fragment, container, false);		
		final Button loginButton = (Button)view.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final EditText emailAddressEditText = (EditText)view.findViewById(R.id.loginEmailAddressEditText);
				String username = emailAddressEditText.getText().toString();

				final EditText passwordEditText = (EditText)view.findViewById(R.id.passwordEditText);
				String password = passwordEditText.getText().toString();

				//hide keyboard
				FragmentActivity activity = getActivity();
				InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(emailAddressEditText.getWindowToken(), 0);

				if (username.equals("")) {
					displayErrorMessageDialog(R.string.emptyEmailAddress);
					emailAddressEditText.requestFocus();
					return;
				}

				if (password.equals("")) {
					displayErrorMessageDialog(R.string.emptyPassword);
					passwordEditText.requestFocus();
					return;
				}

				//show progress indicator
				ProgressDialog progressDialog = 
					ProgressDialog.show(activity, activity.getString(R.string.loggingInProgressMsg), 
							activity.getString(R.string.waitMsg));
				
				//Validate login success
				new ValidateCredentialsTask(username, password, progressDialog).execute();
            }
        });
		
		final Button registerButton = (Button)view.findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity().getApplicationContext(), RegisterFragmentActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });
		
		final Button resetPasswordButton = (Button)view.findViewById(R.id.resetPasswordButton);
		resetPasswordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity().getApplicationContext(), ResetPasswordFragmentActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });
		
		
		return view;
	}
	
	private void displayErrorMessageDialog(int errorMessageResId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		
		builder.setTitle(R.string.loginErrorDialogTitle)
			   .setMessage(errorMessageResId)
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		

		builder.show();
	}
	
	private class ValidateCredentialsTask extends AsyncTask<Void, String, String> {
		
		private String username;
		private String password;
		private ProgressDialog progressDialog;
		
		public ValidateCredentialsTask(String username, String password, ProgressDialog progressDialog) {
			super();
			this.username = username;
			this.password = password;
			this.progressDialog = progressDialog;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			Credentials credentials = new Credentials(username, password);
			return credentials.validateCredentialsFromServer(getActivity());
		}

		
		@Override
		protected void onPostExecute(String errorMessage) {
			progressDialog.dismiss();
			
			if (errorMessage == null) {
				//Save username/pwd if login succeeded
				Credentials.saveToPreferences(getActivity(), username, password);

				Toast.makeText(getActivity().getApplicationContext(), R.string.loginSuccessMessage, Toast.LENGTH_SHORT).show();

				// Wait for toast to go away
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						FragmentActivity activity = getActivity();
						try {
							Thread.sleep(2000);
							activity.setResult(Activity.RESULT_OK);

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                            boolean toRegister =  prefs.getBoolean(getString(R.string.redirectToRegister), false);
                            if(toRegister) {
								Log.d(TAG, "Redirecting to Register");
								startActivity(new Intent(activity.getApplicationContext(), SubmitMeetingFragmentActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
							}
                            else {
								Log.d(TAG, "Redirecting to AAMeetingManager");
								startActivity(new Intent(activity.getApplicationContext(), AAMeetingManager.class)
										.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
							}

							activity.finish();
						} catch (Exception e) {
							Log.d(TAG, "Error finishing activity: " + e.getMessage());
						}
					}
				});	
				
				thread.start();

			} else {
				displayLoginErrorMessage(errorMessage);
			}
		}
		
		private void displayLoginErrorMessage(String errorMessage) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			String dialogMessage = getString(R.string.loginErrorMessage)
				+ " " + errorMessage;
			
			builder.setTitle(R.string.loginErrorDialogTitle)
				   .setMessage(dialogMessage)
				   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
						public void onClick(DialogInterface dialog, int which) {
							LoginFragment.this.clearLoginFields();
							dialog.dismiss();
						}
					});

			builder.show();
		}
	}
	
	private void clearLoginFields() {
		View view = getView();
		
		EditText emailAddressEditText = (EditText)view.findViewById(R.id.loginEmailAddressEditText);
		emailAddressEditText.setText("");
		emailAddressEditText.requestFocus();

		EditText passwordEditText = (EditText)view.findViewById(R.id.passwordEditText);
		passwordEditText.setText("");
	}

}
