package org.mcjug.aameetingmanager.help;

import org.mcjug.meetingfinder.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelpFragment extends Fragment {
	//private static final String TAG = HelpFragment.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.help_fragment, container,
				false);

		WebView webView = (WebView) view.findViewById(R.id.WebView01);

		WebSettings webSettings = webView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		webSettings.setJavaScriptEnabled(true);

		webView.setWebViewClient(new AABuddyWebViewClient());

		webView.loadUrl("file:///android_asset/index.html");

		return view;
	}

	private class AABuddyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }

}
