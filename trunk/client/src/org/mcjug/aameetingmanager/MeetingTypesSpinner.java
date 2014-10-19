package org.mcjug.aameetingmanager;

import java.util.List;

import org.mcjug.aameetingmanager.meeting.MeetingType;

import android.content.Context;
import android.util.AttributeSet;

public class MeetingTypesSpinner extends MultiSpinner {
	public MeetingTypesSpinner(Context context) {
		super(context);
	}

	public MeetingTypesSpinner(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public MeetingTypesSpinner(Context context, AttributeSet attributeSet, int mode) {
		super(context, attributeSet, mode);
	}

	@Override
	protected void setItemSpinnerText(StringBuffer spinnerBuffer, int itemIdx) {
		List<String> items = getItems();
		int numSelected = getNumSelected();
		if (numSelected == 0) {	
			spinnerBuffer.append(getNoneSelectedText());	
		} else {
			String itemString = items.get(itemIdx);
			List<MeetingType> meetingTypes = AAMeetingApplication.getInstance().getMeetingTypes();
			for (int i = 0; i < meetingTypes.size(); i++) {
				MeetingType meetingType = meetingTypes.get(i);
				if (itemString.equals(meetingType.getName())) {
					spinnerBuffer.append(meetingType.getShortName());
				}
			}
		}
	}
}
