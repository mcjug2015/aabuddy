package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class AAMeetingManager extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
	private static final String TAG = AAMeetingManager.class.getSimpleName();

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;
	
	/**
	 * A simple factory that returns dummy views to the Tabhost
	 * @author mwho
	 */
	class TabFactory implements TabContentFactory {

		private final Context mContext;

	    /**
	     * @param context
	     */
	    public TabFactory(Context context) {
	        mContext = context;
	    }

	    /** (non-Javadoc)
	     * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
	     */
	    public View createTabContent(String tag) {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }

	}
	
	/** (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Inflate the layout
		setContentView(R.layout.aa_meeting_manager);
		
		// Initialize the TabHost
		this.initializeTabHost(savedInstanceState);
		if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
		
		// Initialize ViewPager
		this.intializeViewPager();
	}

    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    private void intializeViewPager() {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(Fragment.instantiate(this, SubmitMeetingFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, FindMeetingFragment.class.getName()));
		this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
		
		this.mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(this);
    }
   
	private void initializeTabHost(Bundle args) {
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        AAMeetingManager.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("SubmitMeeting").setIndicator("Submit Meeting"));
        AAMeetingManager.addTab(this, this.mTabHost, this.mTabHost.newTabSpec("FindMeeting").setIndicator("Find Meeting"));
        
        mTabHost.setOnTabChangedListener(this);
	}

	/**
	 * Add Tab content to the Tabhost
	 * @param activity
	 * @param tabHost
	 * @param tabSpec
	 */
	private static void addTab(AAMeetingManager activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	public void onPageSelected(int arg0) {
		this.mTabHost.setCurrentTab(arg0);
	}

	public void onPageScrollStateChanged(int arg0) {
	}

	public void onTabChanged(String tabId) {
		int pos = this.mTabHost.getCurrentTab();
		if (this.mViewPager != null) {
			this.mViewPager.setCurrentItem(pos);
		}	
	}
}

