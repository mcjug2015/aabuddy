package org.mcjug.aameetingmanager;

import android.content.Context;
import android.util.AttributeSet;

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

	protected void setItemSpinnerText(StringBuffer spinnerBuffer, int itemIdx) {
		String[] daysOfWeekAbbr = getContext().getResources().getStringArray(R.array.daysOfWeekAbbr);
		spinnerBuffer.append(daysOfWeekAbbr[itemIdx]);
	}
}
