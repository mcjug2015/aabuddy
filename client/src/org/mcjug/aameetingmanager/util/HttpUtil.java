package org.mcjug.aameetingmanager.util;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HttpUtil {

	public static DefaultHttpClient createHttpClient() {
		Context context = AAMeetingApplication.getInstance();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		boolean defaultSslTrustAllFlag = Boolean.getBoolean(context.getString(R.string.sslTrustAllFlagDefaultValue));			
		boolean bSslTrustAllFlag = 
			prefs.getBoolean(context.getString(R.string.sslTrustAllFlagPreferenceName), 
							 defaultSslTrustAllFlag);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(
				new Scheme("https",
						   (bSslTrustAllFlag ? new EasySSLSocketFactory() : SSLSocketFactory.getSocketFactory()),
						   443));

		HttpParams httpParams = new BasicHttpParams();
		ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams,
				schemeRegistry);

	   return new DefaultHttpClient(cm, httpParams);
        
	}
	
	public static String getSecureRequestUrl(Context context, int requestUrlResourceId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String defaultServerBase = context.getString(R.string.meetingServerSecureBaseUrlDefaultValue);			
		String serverBaseUrl = prefs.getString(context.getString(R.string.meetingServerSecureBaseUrlPreferenceName), defaultServerBase);
		
		StringBuilder baseUrl = new StringBuilder(serverBaseUrl);
		baseUrl.append(context.getString(requestUrlResourceId));
		return baseUrl.toString();
	}

	public static String getUnsecureRequestUrl(Context context, int requestUrlResourceId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String defaultServerBase = context.getString(R.string.meetingServerUnsecureBaseUrlDefaultValue);			
		String serverBaseUrl = prefs.getString(context.getString(R.string.meetingServerUnsecureBaseUrlPreferenceName), defaultServerBase);
		
		StringBuilder baseUrl = new StringBuilder(serverBaseUrl);
		baseUrl.append(context.getString(requestUrlResourceId));
		return baseUrl.toString();
	}

}
