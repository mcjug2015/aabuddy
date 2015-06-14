package org.mcjug.aameetingmanager;

import android.content.Context;
import android.util.AttributeSet;

import org.mcjug.meetingfinder.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		} else {
            daysOfWeek = getContext().getResources().getStringArray(R.array.daysOfWeekShort);
        }
		spinnerBuffer.append(daysOfWeek[itemIdx]);
	}

	@Override
	public void setItems(List<String> items, String selectedItemText, String noneSelectedText, String allSelectedText, MultiSpinnerListener listener)  {

		boolean[] selected = new boolean[items.size()];

		String[] selectedItemsArray =  selectedItemText.split(", ");
		if (allSelectedText != null && selectedItemText.equals(allSelectedText)) {
			Arrays.fill(selected, Boolean.TRUE);
		}
		else {

			String[] allDaysOfWeekArray;
			List<String> allItems;
			if (selectedItemsArray[0].length() <= 2) {
				allDaysOfWeekArray = getContext().getResources().getStringArray(R.array.daysOfWeekShort);
				allItems = new ArrayList(Arrays.asList(allDaysOfWeekArray));
			} else if (selectedItemsArray[0].length() == 3) {
				allDaysOfWeekArray = getContext().getResources().getStringArray(R.array.daysOfWeekMedium);
				allItems = new ArrayList(Arrays.asList(allDaysOfWeekArray));
			} else {
				allItems = items;
			}

			for (String item : selectedItemsArray) {
				if (allItems.contains(item)) {
					selected[allItems.indexOf(item)] = true;
				}
			}
		}

		super.setItems(items, selectedItemText, selected, noneSelectedText, allSelectedText,listener);

	}

}
