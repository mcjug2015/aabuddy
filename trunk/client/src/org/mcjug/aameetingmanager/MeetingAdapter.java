package org.mcjug.aameetingmanager;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MeetingAdapter extends ArrayAdapter<Meeting>{
	private List<Meeting> meetings;

	public MeetingAdapter(Context context, int textViewResourceId, List<Meeting> meetings) {
		super (context, textViewResourceId, meetings);
		this.meetings = meetings;
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings.clear();
		this.meetings.addAll(meetings);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final Context context = getContext();

		View view = convertView;
		ViewHolder holder;
		if (view == null){

			LayoutInflater inflater = LayoutInflater.from(context);
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

		final Meeting meeting = getItem(position);
		if (meeting != null) {
			holder.address.setText(meeting.getAddress());
			holder.day.setText(meeting.getDayOfWeek());
			holder.description.setText(meeting.getDescription());
			holder.distance.setText(meeting.getDistance());
			holder.time.setText(meeting.getTimeRange());
			holder.name.setText(meeting.getName());	
			
			final Button meetingNotThereButton = (Button)view.findViewById(R.id.meetingNotThereButton);
			meetingNotThereButton.setEnabled(!isMeetingInNotThereList(meeting.getId()));
			meetingNotThereButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					
					builder.setTitle(R.string.postMeetingNotThereConfirmDialogTitle)
						   .setMessage(R.string.postMeetingNotThereConfirmDialogMsg)
						   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
								public void onClick(DialogInterface dialog, int which) {
									//disable button
									meetingNotThereButton.setEnabled(false);
									
									//show progress indicator
									ProgressDialog progressDialog = 
										ProgressDialog.show(context, context.getString(R.string.postMeetingNotThereProgressMsg), 
												context.getString(R.string.waitMsg));

									//post meeting not there
									new PostMeetingNotThereTask(context, meeting.getId(), progressDialog).execute();
									
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					

					builder.show();
					
	            }
			});
		}

		return view;
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

	private class ViewHolder {
		TextView address;
		TextView day;
		TextView description;
		TextView distance;
		TextView time;
		TextView name;
	}
}
