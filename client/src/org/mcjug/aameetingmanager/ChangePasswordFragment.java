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

import android.app.ProgressDialog;
import android.content.Context;
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
				Context context = view.getContext();
				
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
					return;
				}
	
				ProgressDialog progressDialog = 
					ProgressDialog.show(context, context.getString(R.string.changePasswordProgressMsg), context.getString(R.string.waitMsg));
				
				new ChangePasswordTask(newPassword, progressDialog).execute();
            }

        });
		
		return view;
	}
	
	private class ChangePasswordTask extends AsyncTask<Void, String, String> {
		private String newPassword;
		private ProgressDialog progressDialog;
		
		public ChangePasswordTask(String newPassword, ProgressDialog progressDialog) {
			this.newPassword = newPassword;
			this.progressDialog = progressDialog;
		}		
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errorMessage = null;
			
			HttpClient client = HttpUtil.createHttpClient(); 
			try {  
				String url = HttpUtil.getSecureRequestUrl(getActivity(), R.string.change_password_url_path);
				HttpPost httpPost = new HttpPost(url);
				
				Credentials credentials = Credentials.readFromPreferences(getActivity());
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
			progressDialog.dismiss();
			
			if (errorMsg == null) {
				Toast.makeText(getActivity(), getActivity().getString(R.string.passwordChangedMsg), Toast.LENGTH_LONG).show();	
			} else {	
				Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();	
			}
		}
	}

}
