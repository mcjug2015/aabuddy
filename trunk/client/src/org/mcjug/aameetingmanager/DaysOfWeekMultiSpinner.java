package org.mcjug.aameetingmanager;

import android.content.Context;
import android.util.AttributeSet;

public class DaysOfWeekMultiSpinner extends MultiSpinner {

	private static final String[] DAYS_OF_WEEK_TEXT = {"Su", "M", "T", "W", "Th", "F", "Sa"};
	
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
		spinnerBuffer.append(DAYS_OF_WEEK_TEXT[itemIdx]);
	}
}
