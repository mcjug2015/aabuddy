package org.mcjug.aameetingmanager;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.DeleteMeetingTask.DeleteMeetingListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.nineoldandroids.widget.NineLinearLayout;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MeetingListFragment extends ListFragment {
	private static final String TAG = MeetingListFragment.class.getSimpleName();

	private SharedPreferences prefs;
	private String[] sortOrderValues; 
	private MeetingAdapter listAdapter;
	private InfiniteScrollListener infiniteScrollListener;
	private View footerView;
	private TextView meetingListNumItemsLabel;
	private int offset = 0;
	private Spinner sortOrderSpinner;
	
	private ProgressDialog deleteProgressDialog; 
	private Meeting selectedMeeting;
	private MenuItem meetingNotThereMenuItem;
	private ActionMode actionMode;
	private ListActionModeCallback listActionModeCallback;
	private String userName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.meeting_list_fragment, container, false);

		meetingListNumItemsLabel = (TextView)view.findViewById(R.id.meetingListNumItemsLabel);
		if (null != savedInstanceState) {
			offset = savedInstanceState.getInt("offset");
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();
		
		NineLinearLayout linearLayout = (NineLinearLayout)activity.getLayoutInflater().inflate(R.layout.abs__action_mode_close_item, null);
		ImageView imageView = (ImageView)linearLayout.getChildAt(0);
		imageView.setImageResource(R.drawable.ic_action_search);			

		AAMeetingApplication app = (AAMeetingApplication) getActivity().getApplicationContext();
		List<Meeting> meetings = new ArrayList<Meeting>();
		meetings.addAll(app.getMeetingListResults().getMeetings());

		ListView listView = getListView();
		footerView = getActivity().getLayoutInflater().inflate(R.layout.meeting_list_footer, null);
		listView.addFooterView(footerView);	

		listAdapter = new MeetingAdapter(getActivity(), R.layout.meeting_list_row, meetings);
		listView.setAdapter(listAdapter);

		listActionModeCallback = new ListActionModeCallback();
		
		infiniteScrollListener = new InfiniteScrollListener(getActivity(), getListView(), footerView, listActionModeCallback);
		infiniteScrollListener.setOffset(offset);
		
		listView.setOnScrollListener(infiniteScrollListener);
		listView.setItemsCanFocus(false);
		getListView().setSelector(android.R.color.transparent);

		Credentials credentials = Credentials.readFromPreferences(activity);
		userName = credentials.getUsername();

		try {
			prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			sortOrderValues = getResources().getStringArray(R.array.sortOrderValues);
			sortOrderSpinner = (Spinner)getView().findViewById(R.id.meetingListSortOrder);
			sortOrderSpinner.setOnItemSelectedListener(sortOrderItemSelectListener);
		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}
	}
	
	private OnItemSelectedListener sortOrderItemSelectListener = new OnItemSelectedListener() {
		private boolean initialSelection = true;

		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (initialSelection) {	
				initialSelection = false;
			} else {	
				sortList();
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};	
			
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		selectedMeeting = (Meeting)listView.getItemAtPosition(position);
		listAdapter.setSelectedItem(position);

		if (actionMode == null) {
			MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();
			actionMode = activity.startActionMode(listActionModeCallback);
		}
		
		meetingNotThereMenuItem.setEnabled(!isMeetingInNotThereList(selectedMeeting.getId()));
	}

	private void sortList() {		
		String meetingUrl = prefs.getString(getString(R.string.meetingUrl), "");
		List<NameValuePair> meetingParams = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");
		NameValuePair param;
		for (int i = 0; i < meetingParams.size(); i++) {
			param = meetingParams.get(i);
			if (param.getName().equals("order_by")) {
				meetingParams.set(i, new BasicNameValuePair(param.getName(),  sortOrderValues[sortOrderSpinner.getSelectedItemPosition()]));

			} else if (param.getName().equals("offset")) {
				meetingParams.set(i, new BasicNameValuePair(param.getName(), "0"));

			} else if (param.getName().equals("limit")) {
				int paginationSize = getActivity().getResources().getInteger(R.integer.paginationSize);
				meetingParams.set(i, new BasicNameValuePair(param.getName(), String.valueOf(paginationSize)));
			}
		}

		listActionModeCallback.stopAction();
		getListView().setSelection(0);			
		infiniteScrollListener.reset();
		
		String paramStr = URLEncodedUtils.format(meetingParams, "utf-8");
		FindMeetingTask findMeetingTask = new FindMeetingTask(getActivity(), paramStr, false, getString(R.string.sortMeetingProgressMsg));
		findMeetingTask.execute();
	}
	
	public class ListActionModeCallback implements ActionMode.Callback {
	
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			SherlockFragmentActivity activity = (SherlockFragmentActivity)getActivity();
			if (userName.trim().equals("")) {
				activity.getSupportMenuInflater().inflate(R.menu.meeting_list_menu_no_delete, menu);
				meetingNotThereMenuItem = menu.getItem(1);
			} else {
				activity.getSupportMenuInflater().inflate(R.menu.meeting_list_menu, menu);
				meetingNotThereMenuItem = menu.getItem(2);
			}
			
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.deleteMeeting:
				if (selectedMeeting.getCreator().equals(userName)) {
					getDeleteMeetingDialog().show();
				} else {
					Toast.makeText(getActivity(), getString(R.string.deleteMeetingMustBeCreatorMsg), Toast.LENGTH_LONG).show();		
				}
				mode.finish();
				return true;			
			
			case R.id.map:
				displayMap(selectedMeeting);
				mode.finish(); 
				return true;			
			
			case R.id.meetingNotThere:
				getMeetingNotThereDialog().show();
				mode.finish(); 
				return true;
				
			default:
				return true;
			}			
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			
			ListView listView = getListView();			
			listView.clearChoices();
			listView.clearFocus();
			listView.setSelector(android.R.color.transparent);
			
			listAdapter.setSelectedItem(-1);
			listAdapter.notifyDataSetChanged();
		}
		
		public void stopAction() {
			if (actionMode != null) {
				actionMode.finish();
			}
		}
	};

	@Override
	public void onResume() {
		try {
			MeetingListFragmentActivity activity = (MeetingListFragmentActivity)getActivity();			
			AAMeetingApplication app = (AAMeetingApplication) activity.getApplicationContext();	
			MeetingListResults meetingListResults = app.getMeetingListResults();
			listAdapter.setMeetings(meetingListResults.getMeetings());

			String numItemsLabel = String.format(getString(R.string.meetingListNumItems), 
					listAdapter.getCount(), meetingListResults.getTotalMeetingCount());
			meetingListNumItemsLabel.setText(numItemsLabel);

			infiniteScrollListener.setLoading(false);
			getListView().removeFooterView(footerView);
			
			if (deleteProgressDialog != null) {
				deleteProgressDialog.dismiss();
				deleteProgressDialog = null;
			}

		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}

		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("offset", infiniteScrollListener.getOffset());
	}

	private void displayMap(Meeting meeting) {
		String latitude = meeting.getLatitude();
		String longitude = meeting.getLongitude();
		if (latitude != null && latitude.length() != 0 && longitude != null && longitude.length() != 0) {

			// Display a marker with the address at the latitude and longitude
			String intentURI = "geo:" + latitude + ","+ longitude + "?z=17&q=" + latitude + "," + longitude;
			Uri geo = Uri.parse(intentURI);
			Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
			startActivity(geoMap);
		}
	}

	private AlertDialog.Builder getMeetingNotThereDialog() {
		final Context context = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(R.string.postMeetingNotThereConfirmDialogTitle)
		.setMessage(R.string.postMeetingNotThereConfirmDialogMsg)
		
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				meetingNotThereMenuItem.setEnabled(false);
				ProgressDialog progressDialog = ProgressDialog.show(context, getString(R.string.postMeetingNotThereProgressMsg), 
						context.getString(R.string.waitMsg));
				new PostMeetingNotThereTask(context, selectedMeeting.getId(), progressDialog).execute();
				dialog.dismiss();
			}
		})
		
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});	
		
		return builder;
	}
		
	private boolean isMeetingInNotThereList(int meetingId) {
		List<Integer> notThereList = AAMeetingApplication.getInstance().getMeetingNotThereList();
		if (notThereList != null) {
			for(Integer notThereMeetingId : notThereList) {
				if (notThereMeetingId == meetingId) {
					return true;
				}
			}
		}
		return false;
	}
	
	private DeleteMeetingListener deleteMeetingListener = new DeleteMeetingListener() {
		@Override
		public void deleteMeetingResults(Meeting meeting) {
			listAdapter.remove(meeting);
			
			String meetingUrl = prefs.getString(getString(R.string.meetingUrl), "");
			List<NameValuePair> meetingParams = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");		
			String paramStr = URLEncodedUtils.format(meetingParams, "utf-8");
			FindMeetingTask findMeetingTask = new FindMeetingTask(getActivity(), paramStr, false, null);
			findMeetingTask.execute();			
		}
	};
	
	private AlertDialog.Builder getDeleteMeetingDialog() {
		final Context context = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		String deleteMeetingMsg = String.format(getString(R.string.deleteMeetingConfirmMsg), selectedMeeting.getName());
		builder.setTitle(R.string.deleteMeetingConfirmTitle)
		.setMessage(deleteMeetingMsg)
		
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				deleteProgressDialog = ProgressDialog.show(context, 
						getString(R.string.deleteMeetingProgressMsg), 
						getString(R.string.waitMsg));
				
				new DeleteMeetingTask(getActivity(), selectedMeeting, deleteMeetingListener).execute();	
				dialog.dismiss();
			}
		})
		
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});	
		
		return builder;
	}
}
