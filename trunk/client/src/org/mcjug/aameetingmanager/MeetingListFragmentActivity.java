package org.mcjug.aameetingmanager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MeetingListFragmentActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_list);
    }
}