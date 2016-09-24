package org.mcjug.aameetingmanager.meeting;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.AAMeetingApplication;
import org.mcjug.aameetingmanager.DaysOfWeekMultiSpinner;
import org.mcjug.aameetingmanager.LocationFinder;
import org.mcjug.aameetingmanager.LocationFinder.LocationResult;
import org.mcjug.aameetingmanager.MultiSpinner;
import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;
import org.mcjug.meetingfinder.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindMeetingFragment extends Fragment {
    private static final String TAG = FindMeetingFragment.class.getSimpleName();
    private static final String EMPTY_TIME = "--:--";

    private EditText nameEditText;
    private EditText addressEditText;
    private Button refreshLocationButton;
    private Button startTimeButton;
    private Button startTimeClearButton;
    private Button endTimeButton;
    private Button endTimeClearButton;
    private Button findMeetingButton;
    private Calendar startTimeCalendar;
    private Calendar endTimeCalendar;
    private DaysOfWeekMultiSpinner daysOfWeekSpinner;
    private MultiSpinner meetingTypesSpinner;
    private Spinner distanceSpinner;
    private TextView meetingTypesTextView;

    private TimePickerDialog.OnTimeSetListener startTimeDialogListener;
    private TimePickerDialog.OnTimeSetListener endTimeDialogListener;
    private ProgressDialog locationProgress;
    private LocationResult locationResult;
    private FindMeetingTask findMeetingTask;
    private Map<String, Integer> meetingTypeIds = new HashMap<String, Integer>();

    private Context context;
    private boolean is24HourTime;
    private boolean showMeetingTypes;
    private SharedPreferences prefs;
    private List<String> meetingTypesToDisplay;

    private final String START_TEXT = "StartText";
    private final String END_TEXT = "EndText";
    private final String DAYS_OF_WEEK_STRING = "DaysOfWeekString";
    private final String MEETING_TYPE_SELECTION_TEXT = "MeetingTypeSelectionText";
    private final String MEETING_TYPE_SELECTION_FLAGS = "MeetingTypeSelectionFlags";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(AAMeetingApplication.getInstance());
        showMeetingTypes = prefs.getBoolean(getString(R.string.meetingTypesPreferenceKey), false);
        Log.i(TAG, "onCreate: showMeetingTypes " + showMeetingTypes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

        distanceSpinner = (Spinner) view.findViewById(R.id.findMeetingDistanceSpinner);
        List<String> distanceValues = Arrays.asList(getResources().getStringArray(R.array.searchDistanceValues));
        distanceSpinner.setSelection(distanceValues.indexOf("10"));

        nameEditText = (EditText) view.findViewById(R.id.findMeetingNameEditText);
        nameEditText.setHint("Optional");
        addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);
        addressEditText.setSelectAllOnFocus(true);

        refreshLocationButton = (Button) view.findViewById(R.id.findMeetingRefreshLocationButton);
        refreshLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Context context = getActivity();
                    locationProgress = ProgressDialog.show(context, context.getString(R.string.getLocationMsg),
                            context.getString(R.string.waitMsg));
                    LocationFinder locationTask = new LocationFinder(getActivity(), locationResult);
                    locationTask.requestLocation();
                } catch (Exception ex) {
                    Log.d(TAG, "Error current location " + ex);
                }
            }
        });

        findMeetingButton = (Button) view.findViewById(R.id.findMeetingFindButton);
        findMeetingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentActivity activity = getActivity();
                    String addressName = addressEditText.getText().toString();
                    Address address = LocationUtil.getAddressFromLocationName(addressName, activity);
                    if (address == null) {
                        toast(6000, "Not able to validate address. Please try entering a zip code only or rebooting phone if problem persists.");
                    } else {
                        findMeetingTask = new FindMeetingTask(activity, getFindMeetingParams(), false,
                                activity.getString(R.string.findMeetingProgressMsg));
                        findMeetingTask.execute();
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "Error getting meetings: " + ex);
                }
            }
        });


        daysOfWeekSpinner = (DaysOfWeekMultiSpinner) view.findViewById(R.id.findMeetingDaysOfWeekSpinner);
        List<String> daysOfWeekListItems = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekLong));

        is24HourTime = DateTimeUtil.is24HourTime(getActivity());

        startTimeCalendar = Calendar.getInstance();
        endTimeCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();

        boolean need_to_init = true;
        String meetingTypeSelectionText = null;
        boolean[] meetingTypeSelectionFlags = null;

        startTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startTimeCalendar.set(Calendar.MINUTE, 0);
        endTimeCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endTimeCalendar.set(Calendar.MINUTE, 59);

        startTimeButton = (Button) view.findViewById(R.id.findMeetingStartTimeButton);
        endTimeButton = (Button) view.findViewById(R.id.findMeetingEndTimeButton);

        if (savedInstanceState != null) {
            if (savedInstanceState.isEmpty())
                Log.i(TAG, "Can't restore state, the bundle is empty.");
            else {
                int hourOfDay;
                // Restore Saved Instance after rotation
                String startCalendarText = savedInstanceState.getString(START_TEXT);
                if (startCalendarText.equals(EMPTY_TIME)) {
                    startTimeButton.setText(EMPTY_TIME);
                } else {
                    hourOfDay = Integer.parseInt(startCalendarText.substring(0, 2));
                    if(startCalendarText.split(" ").length == 2) {
                        if (hourOfDay == 12) hourOfDay = 0;
                        if (startCalendarText.split(" ")[1].equals("PM"))
                            hourOfDay += 12;
                    }

                    startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTimeCalendar.set(Calendar.MINUTE, Integer.parseInt(startCalendarText.substring(3, 5)));
                    startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
                }

                String endCalendarText = savedInstanceState.getString(END_TEXT);
                if (endCalendarText.equals(EMPTY_TIME)) {
                    endTimeButton.setText(EMPTY_TIME);
                } else {
                    hourOfDay = Integer.parseInt(endCalendarText.substring(0, 2));
                    if(endCalendarText.split(" ").length == 2) {
                        if (hourOfDay == 12) hourOfDay = 0;
                        if (endCalendarText.split(" ")[1].equals("PM"))
                            hourOfDay += 12;
                    }

                    endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTimeCalendar.set(Calendar.MINUTE, Integer.parseInt(endCalendarText.substring(3, 5)));
                    endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
                    Log.i(TAG, "onCreateView: saved END_TEXT " + endCalendarText +
                            " Start " + startTimeButton.getText().toString() +
                            " End " + endTimeButton.getText().toString());
                }

                meetingTypeSelectionText = savedInstanceState.getString(MEETING_TYPE_SELECTION_TEXT);
                meetingTypeSelectionFlags = savedInstanceState.getBooleanArray(MEETING_TYPE_SELECTION_FLAGS);

                String selectedDaysOfWeekString = savedInstanceState.getString(DAYS_OF_WEEK_STRING);

                daysOfWeekSpinner.setItems(daysOfWeekListItems,
                        selectedDaysOfWeekString,
                        getString(R.string.all),
                        getString(R.string.all),
                        new MultiSpinnerListener() {
                            @Override
                            public void onItemsSelected(boolean[] selected) {
                            }
                        });

                need_to_init = false;
            }
        }

        if (need_to_init) {

            startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
            endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            daysOfWeekSpinner.setItems(daysOfWeekListItems,
                    daysOfWeekListItems.get(dayOfWeek),
                    getString(R.string.all),
                    getString(R.string.all),
                    new MultiSpinnerListener() {
                        @Override
                        public void onItemsSelected(boolean[] selected) {
                        }
                    });
        }

        meetingTypesTextView = (TextView) view.findViewById(R.id.findMeetingTypesTextView);
        meetingTypesSpinner = (MultiSpinner) view.findViewById(R.id.findMeetingTypesSpinner);

        // showMeetingTypes = prefs.getBoolean(getString(R.string.meetingTypesPreferenceKey), false);

        meetingTypes(true, meetingTypeSelectionText, meetingTypeSelectionFlags);

        Log.i(TAG, "onCreateView: showMeetingTypes " + showMeetingTypes +
                " Start " + startTimeButton.getText().toString() +
                " End " + endTimeButton.getText().toString());

        return view;
    }

    private void meetingTypes(boolean initialize, String meetingTypeSelectionText, boolean[] meetingTypeSelectionFlags  ) {
        // do nothing if there is no initialization phase or showMeetingTypes was not changed
        boolean currentShowMeetingTypes = prefs.getBoolean(getString(R.string.meetingTypesPreferenceKey), false);
        if (!initialize && showMeetingTypes == currentShowMeetingTypes)
            return;


        showMeetingTypes = currentShowMeetingTypes;
        if (showMeetingTypes) {
            Log.i(TAG, "meetingTypes update: creating ");
            List<MeetingType> meetingTypes = AAMeetingApplication.getInstance().getMeetingTypes();
            meetingTypesToDisplay = new ArrayList<String>();
            meetingTypeIds.clear();
            for (int i = 0; i < meetingTypes.size(); i++) {
                MeetingType meetingType = meetingTypes.get(i);
                meetingTypesToDisplay.add(meetingType.getName());
                meetingTypeIds.put(meetingType.getShortName().trim(), Integer.valueOf(meetingType.getId()));
            }

            if (meetingTypeSelectionText == null) {
                meetingTypeSelectionText = getString(R.string.any);
            }
            if (meetingTypeSelectionFlags == null || meetingTypeSelectionFlags.length == 0) {
                meetingTypeSelectionFlags = new boolean[meetingTypesToDisplay.size()];
            }

            meetingTypesTextView.setVisibility(View.VISIBLE);

            meetingTypesSpinner.setItems(meetingTypesToDisplay,
                    meetingTypeSelectionText,
                    meetingTypeSelectionFlags,
                    getString(R.string.any), null,
                    new MultiSpinnerListener() {
                        @Override
                        public void onItemsSelected(boolean[] selected) {
                        }
                    });
            meetingTypesSpinner.setVisibility(View.VISIBLE);
        }
        else {
            Log.i(TAG, "meetingTypes update: hiding ");
            meetingTypesTextView.setVisibility(View.GONE);
            if (meetingTypesSpinner != null) {
                meetingTypesSpinner.setVisibility(View.GONE);
            }
        }
    }

    private void updateTimeWidgets(final boolean is24HourTime) {
        View view = getView();

        startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startTimeCalendar.set(Calendar.MINUTE, minute);
                startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar, is24HourTime));
            }
        };

        startTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), startTimeDialogListener, startTimeCalendar
                        .get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), is24HourTime);
                dialog.show();
            }
        });

        startTimeClearButton = (Button) view.findViewById(R.id.findMeetingStartTimeClearButton);
        startTimeClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = startTimeButton.getWidth();
                startTimeButton.setText(EMPTY_TIME);
                startTimeButton.setWidth(width);
            }
        });

        endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endTimeCalendar.set(Calendar.MINUTE, minute);
                endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar, is24HourTime));
            }
        };

        endTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), endTimeDialogListener,
                        endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), is24HourTime);
                dialog.show();
            }
        });

        endTimeClearButton = (Button) view.findViewById(R.id.findMeetingEndTimeClearButton);
        endTimeClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = endTimeButton.getWidth();
                endTimeButton.setText(EMPTY_TIME);
                endTimeButton.setWidth(width);
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        context = getActivity();

        /*
        if (!showMeetingTypes) {
            meetingTypesTextView.setVisibility(View.GONE);
            if (meetingTypesSpinner != null) {
                meetingTypesSpinner.setVisibility(View.GONE);
            }
        }
        */

        Location location = LocationUtil.getLastKnownLocation(context);
        String address = LocationUtil.getFullAddress(location, context);
        if (address == null || address.equals("")) {
            addressEditText.setText("Please type in zip code or refresh");
        } else {
            addressEditText.setText(address);
        }
        addressEditText.requestFocus();

        locationResult = new LocationResult() {
            @Override
            public void setLocation(Location location) {
                locationProgress.cancel();

                if (location == null) {
                    location = LocationUtil.getLastKnownLocation(getActivity());
                }

                if (location == null) {
                    Toast.makeText(getActivity(),
                            "Not able to get current location. Please check if GPS is turned or you have a network data connection.",
                            Toast.LENGTH_LONG).show();
                } else {
                    String addressStr = LocationUtil.getFullAddress(location, getActivity());
                    if (addressStr.trim().equals("")) {
                        Toast.makeText(getActivity(),
                                "Not able to get address from location. Please check for a network data connection",
                                Toast.LENGTH_LONG).show();
                    } else {
                        addressEditText.setText(addressStr);
                    }
                }
            }
        };

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        updateTimeWidgets(is24HourTime);
        meetingTypes(false, null, null);
        Log.i(TAG, "onResume: showMeetingTypes=" + showMeetingTypes  +
                " Start " + startTimeButton.getText().toString() +
                " End " + endTimeButton.getText().toString());
        //meetingTypes(true, meetingTypeSelectionText, meetingTypeSelectionFlags);
        super.onResume();
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart: showMeetingTypes=" + showMeetingTypes +
                " Start " + startTimeButton.getText().toString() +
                " End " + endTimeButton.getText().toString());
        super.onStart();
    }

    public void toast(int millisec, String msg) {
        Handler handler = null;
        final Toast[] toasts = new Toast[1];
        for(int i = 0; i < millisec; i+=2000) {
            toasts[0] = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
            toasts[0].show();
            if(handler == null) {
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toasts[0].cancel();
                    }
                }, millisec);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState == null) {
            Log.d(TAG, "onSaveInstanceState: outState == null");
        } else {
            outState.putString(START_TEXT, startTimeButton.getText().toString());
            outState.putString(END_TEXT, endTimeButton.getText().toString());
            if (showMeetingTypes) {
                outState.putString(MEETING_TYPE_SELECTION_TEXT, ((String) meetingTypesSpinner.getSelectedItem()));
                outState.putBooleanArray(MEETING_TYPE_SELECTION_FLAGS, meetingTypesSpinner.getSelected());
            }
            outState.putString(DAYS_OF_WEEK_STRING, (String) daysOfWeekSpinner.getSelectedItem());

            Log.i(TAG, "onSaveInstanceState: Saved Start " +
                    startTimeButton.getText().toString() +
                    " End " + endTimeButton.getText().toString());
        }
    }

    private String getFindMeetingParams() throws Exception {
        StringBuilder builder = new StringBuilder();
        //List<NameValuePair> params = new ArrayList<NameValuePair>();


        String addressName = addressEditText.getText().toString();
        Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
        //params.add(new BasicNameValuePair("lat", String.valueOf(address.getLatitude())));
        //params.add(new BasicNameValuePair("long", String.valueOf(address.getLongitude())));
        builder.append("lat=" + String.valueOf(address.getLatitude()));
        builder.append("&long=" + String.valueOf(address.getLongitude()));

        String[] mileValues = getResources().getStringArray(R.array.searchDistanceValues);
        //params.add(new BasicNameValuePair("distance_miles", mileValues[distanceSpinner.getSelectedItemPosition()]));
        builder.append("&distance_miles=" + mileValues[distanceSpinner.getSelectedItemPosition()]);

        String name = nameEditText.getText().toString().trim();
        if (!name.equals("")) {
            //params.add(new BasicNameValuePair("name", name));
            builder.append("&name=" + Uri.encode(name));
        }

        if (!startTimeButton.getText().equals(EMPTY_TIME)) {
            //params.add(new BasicNameValuePair("start_time__gte", DateTimeUtil.getFindMeetingTimeStr(startTimeCalendar)));
            builder.append("&start_time__gte=" + DateTimeUtil.getFindMeetingTimeStr(startTimeCalendar));
        }

        if (!endTimeButton.getText().equals(EMPTY_TIME)) {
            //params.add(new BasicNameValuePair("end_time__lte", DateTimeUtil.getFindMeetingTimeStr(endTimeCalendar)));
            builder.append("&end_time__lte=" + DateTimeUtil.getFindMeetingTimeStr(endTimeCalendar));
        }

        String[] daysOfWeekSelections = ((String) daysOfWeekSpinner.getSelectedItem()).split(",");
        if (daysOfWeekSelections[0].equalsIgnoreCase(getString(R.string.all))) {
            daysOfWeekSelections = getString(R.string.all_days_of_week_value).split(",");
        }

        List<String> daysOfWeek;
        if (daysOfWeekSelections.length == 1) {
            daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekLong));
        } else if (daysOfWeekSelections.length == 2 || daysOfWeekSelections.length == 3) {
            daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekMedium));
        } else {
            daysOfWeek = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekShort));
        }

        for (String str : daysOfWeekSelections) {
            int idx = daysOfWeek.indexOf(str.trim());
            //params.add(new BasicNameValuePair("day_of_week_in", String.valueOf(idx + 1)));
            builder.append("&day_of_week_in=" + String.valueOf(idx + 1));
        }

        if (showMeetingTypes) {
            String[] meetingTypeSelections = ((String) meetingTypesSpinner.getSelectedItem()).split(",");
            if (!meetingTypeSelections[0].equals(getString(R.string.any))) {
                for (String str : meetingTypeSelections) {
                    Integer id = meetingTypeIds.get(str.trim());
                    //params.add(new BasicNameValuePair("type_ids", String.valueOf(id)));
                    builder.append("&type_ids=" + String.valueOf(id));
                }
            }
        }

        //params.add(new BasicNameValuePair("order_by", getString(R.string.sortingDefault)));
        builder.append("&order_by=" + getString(R.string.sortingDefault));

        int paginationSize = getActivity().getResources().getInteger(R.integer.paginationSize);
        //params.add(new BasicNameValuePair("offset", "0"));
        //params.add(new BasicNameValuePair("limit", String.valueOf(paginationSize)));
        builder.append("&offset=0");
        builder.append("&limit=" + String.valueOf(paginationSize));

        //String paramStr = URLEncodedUtils.format((List<? extends NameValuePair>) params, "utf-8");
        Log.d(TAG, "Find meeting request params: " + builder.toString());

        return builder.toString();
    }
}
