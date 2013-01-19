package org.mcjug.aameetingmanager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import org.mcjug.aameetingmanager.util.Base64;

public class Credentials {
	
	private String username;
	private String password;
	
	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isSet() {
		if (username != null && !username.equals("") && password != null && !password.equals("")) {
			return true;
		}
		
		return false;
	}

	public static Credentials readFromPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		String username = prefs.getString(context.getString(R.string.usernamePreferenceName), "");
		String password = prefs.getString(context.getString(R.string.passwordPreferenceName), "");

		return new Credentials(username, password);
	}
	
	public static void removeFromPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = prefs.edit();
		
		editor.remove(context.getString(R.string.usernamePreferenceName));
		editor.remove(context.getString(R.string.passwordPreferenceName));
		
		editor.commit();
	}
	
	public static void saveToPreferences(Context context, String username, String password) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		Editor editor = prefs.edit();
		
		editor.putString(context.getString(R.string.usernamePreferenceName), username);
		editor.putString(context.getString(R.string.passwordPreferenceName), password);
		
		editor.commit();
	}
	
	public String validateCredentialsFromServer(Context context) {
		DefaultHttpClient client = new DefaultHttpClient(); 
		try {
			String baseUrl = getValidateCredentialsBaseUrl(context);

			HttpPost request = new HttpPost(baseUrl);
	        request.addHeader("Authorization", "Basic " + getBasicAuthorizationHeader());
			
			HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) {
	        	return String.format(context.getString(R.string.validateCredentialsError), response.getStatusLine().toString());
	        }
		} catch (Exception e) {  
			return String.format(context.getString(R.string.validateCredentialsError), e);
		} finally {
			client.getConnectionManager().shutdown();  
		}

		return null;
	}

	private static String getValidateCredentialsBaseUrl(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		StringBuilder baseUrl = new StringBuilder();
		
		String defaultServerBase = context.getString(R.string.meetingServerBaseUrlDefaultValue);			
		String serverBaseUrl = prefs.getString(context.getString(R.string.meetingServerBaseUrlPreferenceName), defaultServerBase);
		
		baseUrl.append(serverBaseUrl);
		baseUrl.append(context.getString(R.string.validate_user_url_path));
		
		return baseUrl.toString();
	}

	public String getBasicAuthorizationHeader() {
        String base64EncodedCredentials = Base64.encodeToString(
                (username+":"+password).getBytes(), Base64.DEFAULT);
        base64EncodedCredentials = base64EncodedCredentials.replace("\n", "");

        return base64EncodedCredentials;
	}
}
