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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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

public class ResetPasswordFragment extends Fragment {
	private static final String TAG = ResetPasswordFragment.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment		
		final View view = inflater.inflate(R.layout.reset_password_fragment, container, false);		

		final FragmentActivity activity = getActivity();
		final EditText emailAddressEditText = (EditText)view.findViewById(R.id.emailAddressEditText);

		//pre-populate email address if set in preferences
		Credentials credentials = Credentials.readFromPreferences(activity);

		String username = credentials.getUsername();
		if (username != null) {
			emailAddressEditText.setText(username);
		}
		
		final Button button = (Button)view.findViewById(R.id.resetPasswordButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				
				String username = emailAddressEditText.getText().toString();

				//hide keyboard
				InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(emailAddressEditText.getWindowToken(), 0);
				
				if (!ValidationUtil.isEmailValid(username)) {
					displayErrorMessageDialog(activity, R.string.invalidEmailAddress);
					emailAddressEditText.requestFocus();
					return;
				}

				//show progress indicator
				ProgressDialog progressDialog = 
					ProgressDialog.show(activity, activity.getString(R.string.resetPasswordMsg), activity.getString(R.string.waitMsg));

				//submit create account
				new ResetPasswordTask(username, progressDialog).execute();
				
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

	private class ResetPasswordTask extends AsyncTask<Void, String, String> {
		private String username;
		private ProgressDialog progressDialog;
		
		public ResetPasswordTask(String username, ProgressDialog progressDialog) {
			super();
			this.username = username;
			this.progressDialog = progressDialog;
		}
		
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errorMessage = null;
			
			String url = HttpUtil.getSecureRequestUrl(getActivity(), R.string.reset_password_url_path);
			HttpClient client = HttpUtil.createHttpClient(); 
			try {  
				HttpPost httpPost = new HttpPost(url);
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);  
				nameValuePairs.add(new BasicNameValuePair("username", username));  
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
				
				HttpResponse httpResponse = client.execute(httpPost);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					errorMessage = httpResponse.getStatusLine().toString();
					Log.d(TAG, "Response code for create user=" + statusCode + "; status line: " + errorMessage);
				}
			
			} catch (Exception e) {
				Log.d(TAG, "Exception resetting password", e);
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
				displayErrorMessageDialog(activity, R.string.resetPasswordError, errorMessage);
				return;
			}
			
			Credentials.removeFromPreferences(activity);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.resetPasswordSuccessDialogTitle)
				   .setMessage(R.string.resetPasswordSuccess)
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
