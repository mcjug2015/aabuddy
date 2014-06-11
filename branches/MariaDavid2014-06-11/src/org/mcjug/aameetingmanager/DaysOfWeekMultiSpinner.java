package org.mcjug.aameetingmanager;

import android.content.Context;
import android.util.AttributeSet;
import org.mcjug.meetingfinder.R;

public class DaysOfWeekMultiSpinner extends MultiSpinner {
	public DaysOfWeekMultiSpinner(Context context) {
		super(context);
	}

	public DaysOfWeekMultiSpinner(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public DaysOfWeekMultiSpinner(Context context, AttributeSet attributeSet, int mode) {
		super(context, attributeSet, mode);
	}

	@Override
    protected void setItemSpinnerText(StringBuffer spinnerBuffer, int itemIdx) {
		String[] daysOfWeek;
		int listSize = getNumSelected();
		if (listSize == 1) {
			daysOfWeek = getContext().getResources().getStringArray(R.array.daysOfWeekLong);
		} else if (listSize == 2 || listSize == 3) {
			daysOfWeek = getContext().getResources().getStringArray(R.array.daysOfWeekMedium);
		}else {
            daysOfWeek = getContext().getResources().getStringArray(R.array.daysOfWeekShort);
        }
		spinnerBuffer.append(daysOfWeek[itemIdx]);
	}
}
