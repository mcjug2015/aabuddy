package org.mcjug.aameetingmanager;

import org.mcjug.aameetingmanager.HelpListAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;


public class HelpFragment extends Fragment {
	//private static final String TAG = HelpFragment.class.getSimpleName();

	private HelpListAdapter listAdapter;
	private ExpandableListView myList;
	private String[] questions;
	private String[] answers;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.help_fragment, container,
				false);

		loadHelpMessages();

		// get reference to the ExpandableListView
		myList = (ExpandableListView) view.findViewById(R.id.expandableList);
		// create the adapter by passing your ArrayList data
		listAdapter = new HelpListAdapter(getActivity(), questions, answers);
		// attach the adapter to the list
		myList.setAdapter(listAdapter);
		
		//collapse all Groups
		collapseAll();

		return view;
	}

	private void loadHelpMessages() {
		questions = getResources().getStringArray(R.array.questions);
		answers = getResources().getStringArray(R.array.answers);
	}
	
	private void collapseAll() {
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++) {
			myList.collapseGroup(i);
		}
	}

}
