package org.mcjug.aameetingmanager.authentication;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.AAMeetingManager;
import org.mcjug.meetingfinder.R;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.ValidationUtil;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class RegisterFragment extends Fragment {
	private static final String TAG = RegisterFragment.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		// Inflate the layout for this fragment		
		final View view = inflater.inflate(R.layout.register_fragment, container, false);		
		final Button button = (Button)view.findViewById(R.id.registerButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				
				final EditText emailAddressEditText = (EditText)view.findViewById(R.id.emailAddressEditText);
				String username = emailAddressEditText.getText().toString();

				final EditText passwordEditText = (EditText)view.findViewById(R.id.passwordEditText);
				String password = passwordEditText.getText().toString();
				
				final EditText confirmPasswordEditText = (EditText)view.findViewById(R.id.confirmPasswordEditText);
				String confirmPassword = confirmPasswordEditText.getText().toString();
				
				//hide keyboard
				FragmentActivity activity = getActivity();
				InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
				
				//Validate username/password input:
				if (username.equals("")) {
					displayErrorMessageDialog(activity, R.string.emptyEmailAddress);
					emailAddressEditText.requestFocus();
					return;
				}
				
				if (!ValidationUtil.isEmailValid(username)) {
					displayErrorMessageDialog(activity, R.string.invalidEmailAddress);
					emailAddressEditText.requestFocus();
					return;
				}

				if (password.equals("")) {
					displayErrorMessageDialog(activity, R.string.emptyPassword);
					passwordEditText.requestFocus();
					return;
				}
				
				//TODO:  Check if password follows complexity rules
				
				if (!password.equals(confirmPassword)) {
					
					passwordEditText.setText(null);
					passwordEditText.requestFocus();

					confirmPasswordEditText.setText(null);
					
					setBorder(passwordEditText, confirmPasswordEditText);

					displayErrorMessageDialog(activity, R.string.passwordsDoNotMatchError);
					return;
				}
				
				//show progress indicator
				ProgressDialog progressDialog = 
					ProgressDialog.show(activity, activity.getString(R.string.registeringMsg), activity.getString(R.string.waitMsg));

				//submit create account
				new CreateUserTask(username, password, progressDialog).execute();
				
            }

			@TargetApi(16)
			public void setBorder(final EditText passwordEditText,
					final EditText confirmPasswordEditText) {
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	        		//TODO:  Change the error border so it looks better
					passwordEditText.setBackground(getResources().getDrawable(R.drawable.error_border));
					confirmPasswordEditText.setBackground(getResources().getDrawable(R.drawable.error_border));
	        	}
	        }
        });
		
		return view;
	}
	
	private void displayErrorMessageDialog(Context context, int errorMessageResId) {
		displayErrorMessageDialog(context, errorMessageResId, null);
	}
	
	private void displayErrorMessageDialog(Context context, int errorMessageResId, String errorInstanceMessage) {
		String dialogMessage = String.format(getString(errorMessageResId), errorInstanceMessage).toString();
		
		displayAlertDialog(context, R.string.registerErrorDialogTitle, dialogMessage);
	}
	
	private void displayAlertDialog(Context context, int titleResId, String dialogMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(titleResId)
			   .setMessage(dialogMessage)
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		

		builder.show();		
	}

	private class CreateUserTask extends AsyncTask<Void, String, String> {
		private String username;
		private String password;
		private ProgressDialog progressDialog;
		
		public CreateUserTask(String username, String password, ProgressDialog progressDialog) {
			super();
			this.username = username;
			this.password = password;
			this.progressDialog = progressDialog;
		}
		
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errorMessage = null;
			
			String url = HttpUtil.getSecureRequestUrl(getActivity(), R.string.create_user_url_path);
			HttpClient client = HttpUtil.createHttpClient(); 
			try {  
				HttpPost httpPost = new HttpPost(url);
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
				nameValuePairs.add(new BasicNameValuePair("username", username));  
				nameValuePairs.add(new BasicNameValuePair("password", password));  
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
				
				HttpResponse httpResponse = client.execute(httpPost);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					errorMessage = httpResponse.getStatusLine().toString();
					Log.d(TAG, "Response code for create user=" + statusCode + "; status line: " + errorMessage);
				}
			
			} catch (Exception e) {
				Log.d(TAG, "Exception creating user", e);
				errorMessage = "Unexpected error";
			} finally {
				client.getConnectionManager().shutdown();  
			}
	
			
			return errorMessage;
		}


		@Override
		protected void onPostExecute(String errorMessage) {
			super.onPostExecute(errorMessage);
			
			progressDialog.dismiss();
			
			final FragmentActivity activity = getActivity();
			if (errorMessage != null) {
				displayErrorMessageDialog(activity, R.string.registrationError, errorMessage);
				return;
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.registrationSuccessDialogTitle)
				   .setMessage(R.string.registrationSuccess)
				   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							//Go back to main activity
							startActivity(new Intent(activity.getApplicationContext(), AAMeetingManager.class)
													.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
							activity.finish();
						}
					});
			builder.show();		
		}
		
		
	}
}
