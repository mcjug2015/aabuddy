package org.mcjug.aameetingmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(emailAddressEditText.getWindowToken(), 0);

				//Validate login success
				new ValidateCredentialsTask(username, password).execute();
            }
        });
		
		final Button registerButton = (Button)view.findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getActivity().getApplicationContext(), RegisterFragmentActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });
		
		
		return view;
	}
	
	private class ValidateCredentialsTask extends AsyncTask<Void, String, String> {
		
		private String username;
		private String password;
		
		public ValidateCredentialsTask(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			Credentials credentials = new Credentials(username, password);
			return credentials.validateCredentialsFromServer(getActivity());
		}

		
		@Override
		protected void onPostExecute(String errorMessage) {
			if (errorMessage == null) {
				//Save username/pwd if login succeeded
				Credentials.saveToPreferences(getActivity(), username, password);

				//TODO: Change to dialog?
				Toast.makeText(getActivity().getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();

			} else {
				Toast.makeText(getActivity().getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
			}
		}
	}

}
