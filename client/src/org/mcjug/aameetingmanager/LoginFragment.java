package org.mcjug.aameetingmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

				boolean bLoginSuccess = true;

				//TODO: Perform submit meeting with username/password
				
				if (bLoginSuccess) {
					//Save username/pwd if login succeeded
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
					Editor editor = prefs.edit();
					
					editor.putString(getString(R.string.usernamePreferenceName), username);
					editor.putString(getString(R.string.passwordPreferenceName), password);
					
					editor.commit();

					//TODO:  should go back to submit fragment and submit meeting
					startActivity(new Intent(getActivity().getApplicationContext(), AAMeetingManager.class)
											.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

				} else {
					Toast.makeText(getActivity().getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
				}
				
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

	

}
