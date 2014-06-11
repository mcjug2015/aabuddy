package org.mcjug.aameetingmanager.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.mcjug.meetingfinder.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class HttpUtil {

	public static DefaultHttpClient createHttpClient() {
		Context context = AAMeetingApplication.getInstance();
		boolean defaultSslTrustAllFlag = Boolean.parseBoolean(context.getString(R.string.sslTrustAllFlagDefaultValue));

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(
				new Scheme("https",
						   (defaultSslTrustAllFlag ? new EasySSLSocketFactory() : SSLSocketFactory.getSocketFactory()),
						   443));

		HttpParams httpParams = new BasicHttpParams();
		ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams,
				schemeRegistry);

	   return new DefaultHttpClient(cm, httpParams);

	}

	public static String getSecureRequestUrl(Context context, int requestUrlResourceId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String defaultServerBase = context.getString(R.string.meetingServerSecureBaseUrlDefaultValue);

		StringBuilder baseUrl = new StringBuilder(defaultServerBase);
		baseUrl.append(context.getString(requestUrlResourceId));
		return baseUrl.toString();
	}

	public static String getUnsecureRequestUrl(Context context, int requestUrlResourceId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String defaultServerBase = context.getString(R.string.meetingServerUnsecureBaseUrlDefaultValue);

		StringBuilder baseUrl = new StringBuilder(defaultServerBase);
		baseUrl.append(context.getString(requestUrlResourceId));
		return baseUrl.toString();
	}

	public static String getContent(HttpResponse httpResponse) throws Exception {
		StringBuilder responseStr = new StringBuilder();
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					responseStr.append(line);
				}
			} finally {
				inputStream.close();
			}
		}

		return responseStr.toString();
	}
}
