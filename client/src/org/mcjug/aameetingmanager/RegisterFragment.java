package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
				
				
				if (!password.equals(confirmPassword)) {
					
					passwordEditText.setText(null);
					confirmPasswordEditText.setText(null);
					
					setBorder(passwordEditText, confirmPasswordEditText);
					
					Toast.makeText(getActivity(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//TODO:  show progress indicator
				//submit create account
				new CreateUserTask(username, password).execute();
				
            }

			@TargetApi(16)
			public void setBorder(final EditText passwordEditText,
					final EditText confirmPasswordEditText) {
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					passwordEditText.setBackground(getResources().getDrawable(R.drawable.error_border));
					confirmPasswordEditText.setBackground(getResources().getDrawable(R.drawable.error_border));
	        	}
	        }
        });
		
		return view;
	}
	

	private class CreateUserTask extends AsyncTask<Void, Boolean, Boolean> {
		private String username;
		private String password;
		
		public CreateUserTask(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}
		
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			String url = getCreateUserUrl();
			HttpClient client = new DefaultHttpClient();
			try {  
				HttpPost httpPost = new HttpPost(url);
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
				nameValuePairs.add(new BasicNameValuePair("username", username));  
				nameValuePairs.add(new BasicNameValuePair("password", password));  
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
				
				HttpResponse httpResponse = client.execute(httpPost);
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					return true;
				}
			
				Log.d(TAG, "Response code for create user=" + statusCode);
			} catch (Exception e) {
				Log.d(TAG, "Exception creating user", e);
			} finally {
				client.getConnectionManager().shutdown();  
			}
	
			
			return false;
		}


		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if (!result) {
				Toast.makeText(getActivity(), "Request failed!  Please try again.", Toast.LENGTH_LONG).show();
				return;
			}
			
			Toast.makeText(getActivity(), "Look for email to complete your registration.", Toast.LENGTH_LONG).show();
			
			//Go back to main activity
			startActivity(new Intent(getActivity().getApplicationContext(), AAMeetingManager.class)
									.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

		}
		
		
	}
	
	private String getCreateUserUrl() {
		StringBuilder baseUrl = new StringBuilder();

		FragmentActivity activity = getActivity();
		String defaultServerBase = activity.getString(R.string.meetingServerBaseUrlDefaultValue);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		String serverBaseUrl = prefs.getString(activity.getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
		
		baseUrl.append(serverBaseUrl);
		baseUrl.append(activity.getString(R.string.create_user_url_path));
		
		return baseUrl.toString();
	}
}
