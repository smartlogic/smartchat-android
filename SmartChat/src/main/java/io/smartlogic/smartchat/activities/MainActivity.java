package io.smartlogic.smartchat.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.MainFragmentPagerAdapter;
import io.smartlogic.smartchat.api.GCMRegistration;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "main activity";
    ViewPager mViewPager;
    MainFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        mPagerAdapter = new MainFragmentPagerAdapter(this, mViewPager);

        new GCMRegistration(this).check();
    }
}
