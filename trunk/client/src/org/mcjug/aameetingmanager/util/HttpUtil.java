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

public class HttpUtil {

	public static DefaultHttpClient createHttpClient() {
		boolean bTrustAll = true;  //TODO:  Change to read from shared preferences
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(
				new Scheme("https",
						   (bTrustAll ? new EasySSLSocketFactory() : SSLSocketFactory.getSocketFactory()),
						   443));

		HttpParams httpParams = new BasicHttpParams();
//		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
//		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
//				new ConnPerRouteBean(30));
//		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//		
		ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams,
				schemeRegistry);

	   return new DefaultHttpClient(cm, httpParams);
        
	}
}
