package org.mcjug.aameetingmanager.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.meeting.MeetingListFragment.ListActionModeCallback;
import org.mcjug.meetingfinder.R;

import java.net.URI;
import java.util.List;

public class InfiniteScrollListener implements OnScrollListener {
	private static final String TAG = InfiniteScrollListener.class.getSimpleName();

	private boolean loading = true;
	private Context context;
	private SharedPreferences prefs;
	private int offset = 0;
	private int previousTotal = -1;
	private int paginationSize;
	private ListView listView;
	private View footerView;
	private ListActionModeCallback listActionBarCallback;

	public InfiniteScrollListener(Context context, ListView listView, View footerView, ListActionModeCallback listActionBarCallback) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		paginationSize = context.getResources().getInteger(R.integer.paginationSize);
		this.listView = listView;
		this.footerView = footerView;
		this.listActionBarCallback= listActionBarCallback;
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int lastInScreen = firstVisibleItem + visibleItemCount;
		if ((lastInScreen >= totalItemCount) && !(loading) && (previousTotal != totalItemCount)) {
			loading = true;
			previousTotal = totalItemCount;
			loadMeetings();
		}
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void reset() {
		offset = 0;
		previousTotal = -1;
	}

	private void loadMeetings() {
		try {
			String meetingUrl = prefs.getString(context.getString(R.string.meetingUrl), "");
			List<NameValuePair> meetingParams = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");

			NameValuePair param;
			offset += paginationSize;
			for (int i = 0; i < meetingParams.size(); i++) {
				param = meetingParams.get(i);
				if (param.getName().equals("offset")) {
					meetingParams.set(i, new BasicNameValuePair("offset",   String.valueOf(offset)));

				} else if (param.getName().equals("limit")) {
					meetingParams.set(i, new BasicNameValuePair("limit",  String.valueOf(offset + paginationSize)));
				}
			}

			String paramStr = URLEncodedUtils.format(meetingParams, "utf-8");
			listView.addFooterView(footerView);

			FindMeetingTask findMeetingTask = new FindMeetingTask(context, paramStr, true, null);
			findMeetingTask.execute();
		} catch (Exception ex) {
			Log.d(TAG, "Error getting meetings: " + ex);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView listView, int arg1) {
		listActionBarCallback.stopAction();
	}
}
