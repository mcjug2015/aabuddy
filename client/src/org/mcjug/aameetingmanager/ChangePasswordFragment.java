package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.util.HttpUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Toast;

public class ChangePasswordFragment extends Fragment {
	private static final String TAG = ChangePasswordFragment.class.getSimpleName();
	private Activity activity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Inflate the layout for this fragment		
		final View view = inflater.inflate(R.layout.change_password_fragment, container, false);		
		
		final Button button = (Button)view.findViewById(R.id.changePasswordButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Context context = getActivity().getApplicationContext();
				
				final EditText newPasswordEditText = (EditText)view.findViewById(R.id.newPasswordEditText);
				
				// hide keyboard
				FragmentActivity activity = getActivity();
				InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				
				String newPassword = newPasswordEditText.getText().toString();
				if (newPassword.trim().length() == 0) {
					newPasswordEditText.setError(context.getString(R.string.emptyPassword));
					newPasswordEditText.requestFocus();
					return;
				}
				
				final EditText confirmPasswordEditText = (EditText)view.findViewById(R.id.confirmPasswordEditText);
				String confirmPassword = confirmPasswordEditText.getText().toString();
				if (!newPassword.equals(confirmPassword)) {
					confirmPasswordEditText.setError(context.getString(R.string.passwordsDoNotMatchError));
					confirmPasswordEditText.requestFocus();
					return;
				}
	
				new ChangePasswordTask(newPassword).execute();
            }

        });
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
	}

	private class ChangePasswordTask extends AsyncTask<Void, String, String> {
		private Credentials credentials;
		private String newPassword;
		private ProgressDialog progressDialog;
		
		public ChangePasswordTask(String newPassword) {
			this.newPassword = newPassword;
		}		
		
		@Override
		protected void onPreExecute() {
			Activity activity = getActivity();
			progressDialog = 
					ProgressDialog.show(activity, activity.getString(R.string.changePasswordProgressMsg), activity.getString(R.string.waitMsg));
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String errorMessage = null;
			
			HttpClient client = HttpUtil.createHttpClient(); 
			try {  
				String url = HttpUtil.getSecureRequestUrl(activity, R.string.change_password_url_path);
				HttpPost httpPost = new HttpPost(url);
				
				credentials = Credentials.readFromPreferences(getActivity());
				httpPost.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
				nameValuePairs.add(new BasicNameValuePair("new_password", newPassword));  
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
				
				HttpResponse httpResponse = client.execute(httpPost);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					errorMessage = httpResponse.getStatusLine().toString();
					Log.d(TAG, "Error changing password: " + statusCode + "; status line: " + errorMessage);
				}
			
			} catch (Exception e) {
				errorMessage = "Error changing password: " + e;
				Log.d(TAG, "Error changing password", e);
			} finally {
				client.getConnectionManager().shutdown();  
			}
			
			return errorMessage;
		}

		@Override
		protected void onPostExecute(String errorMsg) {
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
			}
			
			if (errorMsg == null) {
				Credentials.saveToPreferences(activity, credentials.getUsername(), newPassword);
				Toast.makeText(activity, activity.getString(R.string.passwordChangedMsg), Toast.LENGTH_LONG).show();	
			
				// Wait for toast to go away
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(4000);
							activity.startActivity(new Intent(activity, AAMeetingManager.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						} catch (Exception e) {
						}
					}
				});	
				
				thread.start();
			
			} else {	
				Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();	
			}
		}
	}

}
