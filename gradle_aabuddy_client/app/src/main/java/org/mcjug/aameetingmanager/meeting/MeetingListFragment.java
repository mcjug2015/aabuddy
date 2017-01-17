package org.mcjug.aameetingmanager.meeting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.meeting.DeleteMeetingTask.DeleteMeetingListener;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.meetingfinder.R;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	private TextView selectedMeetingDescription;
	private MenuItem meetingNotThereMenuItem;
	private ActionMode actionMode;
	private ListActionModeCallback listActionModeCallback;
	private String userName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.meeting_list_fragment, container, false);
		meetingListNumItemsLabel = (TextView) view.findViewById(R.id.meetingListNumItemsLabel);
		if (null != savedInstanceState) {
			offset = savedInstanceState.getInt("offset");
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        MeetingListFragmentActivity activity = (MeetingListFragmentActivity) getActivity();

		//LinearLayout linearLayout = (LinearLayout) activity.getLayoutInflater().inflate(
		//		R.layout.abc_action_mode_close_item, null);
		//ImageView imageView = (ImageView) linearLayout.getChildAt(0);
		//imageView.setImageResource(R.drawable.ic_action_search);

		AAMeetingApplication app = (AAMeetingApplication) getActivity().getApplicationContext();
		footerView = getActivity().getLayoutInflater().inflate(R.layout.meeting_list_footer, null);
		listActionModeCallback = new ListActionModeCallback();

		List<Meeting> meetings = new ArrayList<Meeting>();
		meetings.addAll(app.getMeetingListResults().getMeetings());
		ListView listView = getListView();
		listView.addFooterView(footerView);
		listAdapter = new MeetingAdapter(getActivity(), R.layout.meeting_list_row, meetings);
		listView.setAdapter(listAdapter);

		infiniteScrollListener = new InfiniteScrollListener(
											getActivity(),
											getListView(),
											footerView,
											listActionModeCallback);
		infiniteScrollListener.setOffset(offset);
		listView.setOnScrollListener(infiniteScrollListener);
		listView.setItemsCanFocus(false);

		getListView().setSelector(android.R.color.transparent);

		Credentials credentials = Credentials.readFromPreferences(activity);
		userName = credentials.getUsername();

		try {
			prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			sortOrderValues = getResources().getStringArray(R.array.sortOrderValues);
			sortOrderSpinner = (Spinner) getView().findViewById(R.id.meetingListSortOrder);
			sortOrderSpinner.setOnItemSelectedListener(sortOrderItemSelectListener);
		} catch (Exception e) {
			Log.d(TAG, "Error setting meeting list");
		}
	}

	private final OnItemSelectedListener sortOrderItemSelectListener = new OnItemSelectedListener() {
		private boolean initialSelection = true;

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (initialSelection) {
				initialSelection = false;
			} else {
				sortList();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		selectedMeeting = (Meeting) listView.getItemAtPosition(position);
		listAdapter.setSelectedItem(position);

		if (actionMode == null) {
            MeetingListFragmentActivity activity = (MeetingListFragmentActivity) getActivity();
			actionMode = activity.startSupportActionMode(listActionModeCallback);
		}

		meetingNotThereMenuItem.setEnabled(!isMeetingInNotThereList(selectedMeeting.getId()));

		if (Build.VERSION.SDK_INT >= 11) {
			clearPrevSelectedTextFormat();
			if (view != null) {
				TextView tvDescription = (TextView) view.findViewById(R.id.meetingDescription);
				tvDescription.setEllipsize(TextUtils.TruncateAt.MARQUEE);
				tvDescription.setMarqueeRepeatLimit(-1);
				tvDescription.setSelected(true);
				tvDescription.setSingleLine(true);
				selectedMeetingDescription = tvDescription;
				listAdapter.notifyDataSetChanged();
			}
		}
	}

	private void clearPrevSelectedTextFormat() {
		if (selectedMeetingDescription != null) {
			selectedMeetingDescription.setEllipsize(TextUtils.TruncateAt.END);
			selectedMeetingDescription.setMarqueeRepeatLimit(-1);
			selectedMeetingDescription = null;
		}
	}

	private void sortList() {
		String meetingUrl = prefs.getString(getString(R.string.meetingUrl), "");
		List<NameValuePair> meetingParams = URLEncodedUtils.parse(URI.create(meetingUrl), "utf-8");
		NameValuePair param;
		for (int i = 0; i < meetingParams.size(); i++) {
			param = meetingParams.get(i);
			if (param.getName().equals("order_by")) {
				meetingParams.set(
						i,
						new BasicNameValuePair(param.getName(), sortOrderValues[sortOrderSpinner
								.getSelectedItemPosition()]));

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
		FindMeetingTask findMeetingTask = new FindMeetingTask(getActivity(), paramStr, false,
				getString(R.string.sortMeetingProgressMsg));
		findMeetingTask.execute();
	}

	public class ListActionModeCallback implements ActionMode.Callback {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			FragmentActivity activity = (FragmentActivity) getActivity();
			if (userName.trim().equals("")) {
				activity.getMenuInflater().inflate(R.menu.meeting_list_menu_no_delete, menu);
				meetingNotThereMenuItem = menu.getItem(2);
			} else {
				activity.getMenuInflater().inflate(R.menu.meeting_list_menu, menu);
				meetingNotThereMenuItem = menu.getItem(2);
			}

			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but may be called multiple times if the mode is
		// invalidated.
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
					Toast.makeText(getActivity(), getString(R.string.deleteMeetingMustBeCreatorMsg), Toast.LENGTH_LONG)
							.show();
				}
				mode.finish();
				return true;

			case R.id.map:
				displayMap(selectedMeeting);
				mode.finish();
				return true;

			case R.id.calendar:
				displayCalendar(selectedMeeting);
				mode.finish();
				return true;

			case R.id.editMeetingTypes:
				displayMeetingTypesEditor(selectedMeeting);
				mode.finish();
				return true;

			case R.id.meetingNotThere:
				getMeetingNotThereDialog().show();
				mode.finish();
				return true;

			case R.id.share:
				shareMeeting(selectedMeeting);
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
			clearPrevSelectedTextFormat();

			listAdapter.setSelectedItem(-1);
			listAdapter.notifyDataSetChanged();
		}

		public void stopAction() {
			if (actionMode != null) {
				actionMode.finish();
			}
		}
	}

	@Override
	public void onResume() {
		try {
            MeetingListFragmentActivity activity = (MeetingListFragmentActivity) getActivity();
			AAMeetingApplication app = (AAMeetingApplication) activity.getApplicationContext();
			MeetingListResults meetingListResults = app.getMeetingListResults();

			listAdapter.setMeetings(meetingListResults.getMeetings());

			int numOfMeetingsInAdapter = listAdapter.getCount();
			int totalNumOfMeetings = meetingListResults.getTotalMeetingCount();
			String numItemsLabel = String.format(getString(R.string.meetingListNumItems), numOfMeetingsInAdapter, totalNumOfMeetings);
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
		Double latitude = meeting.getLatitude();
		Double longitude = meeting.getLongitude();
		if (latitude != null && longitude != null) {

			// Display a marker with the address at the latitude and longitude
			String intentURI = "geo:" + latitude + "," + longitude + "?z=17&q=" + latitude + "," + longitude;
			Uri geo = Uri.parse(intentURI);
			Intent geoMap = new Intent(Intent.ACTION_VIEW, geo);
			startActivity(geoMap);
		}
	}

	private void displayCalendar(Meeting meeting) {
		DateTimeUtil.addToCalendar(getActivity(), meeting);
	}

	private AlertDialog.Builder getMeetingNotThereDialog() {
		final Context context = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final EditText noteEditText = new EditText(getActivity());
		noteEditText.setHint("Optional Note");

		builder.setTitle(R.string.postMeetingNotThereConfirmDialogTitle)
				.setMessage(R.string.postMeetingNotThereConfirmDialogMsg)

				.setView(noteEditText).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						meetingNotThereMenuItem.setEnabled(false);
						ProgressDialog progressDialog = ProgressDialog.show(context,
								getString(R.string.postMeetingNotThereProgressMsg), context.getString(R.string.waitMsg));
						new PostMeetingNotThereTask(context, selectedMeeting.getId(),
								noteEditText.getText().toString(), progressDialog).execute();
						dialog.dismiss();
					}
				})

				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		return builder;
	}

	private void shareMeeting(Meeting meeting) {
		Context context = getActivity();
		String[] daysOfWeek = getResources().getStringArray(R.array.daysOfWeekLong);
		String day = daysOfWeek[meeting.getDayOfWeekIdx() - 1];

		Date startTime = meeting.getStartTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTime);
		String getTimeStr = DateTimeUtil.getTimeStr(calendar, DateTimeUtil.is24HourTime(context));

		String message = "\n" + "Please join me at " + meeting.getName() + " this coming " + day + " starting at "
				+ getTimeStr + ", located at " + meeting.getAddress() + ". Looking forward to seeing you there!";

       if (Build.VERSION.SDK_INT < 11) {
           Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
           shareIntent.setType("text/plain");
           shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Meeting");
           shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
           startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
       } else {
            PackageManager packageManager = context.getPackageManager();
            List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
            configureMailClients(packageManager, intentList, message);
            configureSMSClients(packageManager, intentList, message);

            if (!intentList.isEmpty()) {
                Intent chooserIntent = intentList.get(0);
                intentList.remove(0);
                Intent openInChooser = Intent.createChooser(chooserIntent, getString(R.string.share));
                if (intentList.size() > 0) {
                    LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
                    openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                    startActivity(openInChooser);
                }
            }
       }
	}

    private void configureMailClients(PackageManager packageManager, List<LabeledIntent> intentList, String message) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:"));	
		
		List<ResolveInfo> mailClients = packageManager.queryIntentActivities(emailIntent, 0);
    	for (ResolveInfo resolveInfo : mailClients) {
    		String packageName = resolveInfo.activityInfo.packageName;
    		Intent intent = new Intent(Intent.ACTION_SENDTO);
    	    intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
      		intent.setData(Uri.parse("mailto:"));
        	intent.putExtra(Intent.EXTRA_SUBJECT, "Meeting");
        	intent.putExtra(Intent.EXTRA_TEXT, message);
            intentList.add(new LabeledIntent(intent, packageName, resolveInfo.loadLabel(packageManager), resolveInfo.icon));
        }
	}  
	
	private void configureSMSClients(PackageManager packageManager, List<LabeledIntent> intentList, String message) {
		Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
		smsIntent.setData(Uri.parse("sms:"));
		
		List<ResolveInfo> smsClients = packageManager.queryIntentActivities(smsIntent, 0);
    	for (ResolveInfo resolveInfo : smsClients) {
    		String packageName = resolveInfo.activityInfo.packageName;
    		Intent intent = new Intent(Intent.ACTION_SEND);
    	    intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
      		intent.setData(Uri.parse("sms:"));
         	intent.putExtra(Intent.EXTRA_TEXT, message);
            intentList.add(new LabeledIntent(intent, packageName, resolveInfo.loadLabel(packageManager), resolveInfo.icon));
        }
	}  

	private void displayMeetingTypesEditor(final Meeting selectedMeeting) {
		final List<MeetingType> meetingTypes = AAMeetingApplication.getInstance().getMeetingTypes();

		final List<MeetingType> currentMeetingTypes = selectedMeeting.getMeetingTypes();
		final List<Integer> currentMeetingIds = new ArrayList<Integer>();
		final boolean[] itemsToSelect = new boolean[meetingTypes.size()];
		final CharSequence[] meetingTypeNames = new CharSequence[meetingTypes.size()];

		for (int i = 0; i < meetingTypes.size(); i++) {
			meetingTypeNames[i] = meetingTypes.get(i).getName();

			for (int j = 0; j < currentMeetingTypes.size(); j++) {
				if (currentMeetingTypes.get(j).getId() == meetingTypes.get(i).getId()) {
					itemsToSelect[i] = true;
					currentMeetingIds.add(currentMeetingTypes.get(j).getId());
				}
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.meetingTypesTitle))
				.setMultiChoiceItems(meetingTypeNames, (itemsToSelect.length == 0) ? null : itemsToSelect,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item, boolean isChecked) {
								MeetingType meetingType = meetingTypes.get(item);
								int id = meetingType.getId();
								if (isChecked) {
									currentMeetingIds.add(id);
									currentMeetingTypes.add(meetingType);
								} else if (currentMeetingIds.contains(id)) {
									currentMeetingIds.remove(Integer.valueOf(id));
									currentMeetingTypes.remove(meetingType);
								}
							}
						})

				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						selectedMeeting.sortMeetingTypes();
						listAdapter.notifyDataSetChanged();
						new UpdateMeetingTypesTask(getActivity(), selectedMeeting.getId(), currentMeetingIds).execute();
					}
				}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		Dialog dialog = builder.create();
		dialog.show();
	}

	private boolean isMeetingInNotThereList(int meetingId) {
		List<Integer> notThereList = AAMeetingApplication.getInstance().getMeetingNotThereList();
		if (notThereList != null) {
			for (Integer notThereMeetingId : notThereList) {
				if (notThereMeetingId == meetingId) {
					return true;
				}
			}
		}
		return false;
	}

	private final DeleteMeetingListener deleteMeetingListener = new DeleteMeetingListener() {
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
		builder.setTitle(R.string.deleteMeetingConfirmTitle).setMessage(deleteMeetingMsg)

		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteProgressDialog = ProgressDialog.show(context, getString(R.string.deleteMeetingProgressMsg),
						getString(R.string.waitMsg));

				new DeleteMeetingTask(getActivity(), selectedMeeting, deleteMeetingListener).execute();
				dialog.dismiss();
			}
		})

		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		return builder;
	}
}
