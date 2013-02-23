package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MeetingAdapter extends ArrayAdapter<Meeting>{
	private List<Meeting> meetings;

	public MeetingAdapter(Context context, int textViewResourceId, List<Meeting> meetings) {
		super (context, textViewResourceId, meetings);
		this.meetings = new ArrayList<Meeting>();
		this.meetings.addAll(meetings);
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings.clear();
		this.meetings.addAll(meetings);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		ViewHolder holder;
		if (view == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.meeting_list_row, null);
			
			holder = new ViewHolder();
			holder.address = (TextView) view.findViewById(R.id.meetingAddress);
			holder.day = (TextView) view.findViewById(R.id.meetingDay);
			holder.description = (TextView) view.findViewById(R.id.meetingDescription);
			holder.distance = (TextView) view.findViewById(R.id.meetingDistance);
			holder.time = (TextView) view.findViewById(R.id.meetingTime);
			holder.name = (TextView) view.findViewById(R.id.meetingName);
			   
			view.setTag(holder);
		} else {
		    holder = (ViewHolder) convertView.getTag();
		}

		Meeting meeting = getItem(position);
		if (meeting != null) {
			holder.address.setText(meeting.getAddress());
			holder.day.setText(meeting.getDayOfWeek());
			holder.description.setText(meeting.getDescription());
			holder.distance.setText(meeting.getDistance());
			holder.time.setText(meeting.getTimeRange());
			holder.name.setText(meeting.getName());			
		}

		return view;
	}   

	private class ViewHolder {
		TextView address;
		TextView day;
		TextView description;
		TextView distance;
		TextView time;
		TextView name;
	}
}