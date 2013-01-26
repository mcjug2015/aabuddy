package org.mcjug.aameetingmanager;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.mcjug.aameetingmanager.R;

public class HelpListAdapter extends BaseExpandableListAdapter {
	//private static final String TAG = MyListAdapter.class.getSimpleName();

	private Context context;
	private String questions[];
	private String answers[];
	
	public HelpListAdapter(Context context, String[] questionsFrom, String[] answersFrom) {
		this.context = context;
		this.questions = new String[questionsFrom.length];
		this.answers = new String[answersFrom.length];
		System.arraycopy(questionsFrom, 0, this.questions, 0, answersFrom.length);
		System.arraycopy(answersFrom, 0, this.answers, 0, answersFrom.length);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return answers[childPosition];
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View view, ViewGroup parent) {

		if (view == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.child_row, null);
		}

		TextView answerTextView = (TextView) view.findViewById(R.id.answer);
		answerTextView.setText(Html.fromHtml(answers[groupPosition]));

		return view;
	}

	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	public Object getGroup(int groupPosition) {
		return questions[groupPosition];
	}

	public int getGroupCount() {
		return questions.length;
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isLastChild, View view,
			ViewGroup parent) {

		if (view == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.group_row, null);
		}

		TextView heading = (TextView) view.findViewById(R.id.heading);
		heading.setText(Html.fromHtml(questions[groupPosition]));

		return view;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
