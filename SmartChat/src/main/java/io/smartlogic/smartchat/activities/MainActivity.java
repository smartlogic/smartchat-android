package io.smartlogic.smartchat.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.MainFragmentPagerAdapter;
import io.smartlogic.smartchat.api.GCMRegistration;
import io.smartlogic.smartchat.fragments.AddContactsWithInviteFragment;
import io.smartlogic.smartchat.fragments.CameraFragment;
import io.smartlogic.smartchat.fragments.FriendsFragment;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "main activity";
    ViewPager mViewPager;
    MainFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Started MainActivity");

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        mPagerAdapter = new MainFragmentPagerAdapter(this, mViewPager);
        mPagerAdapter.addTab(CameraFragment.class, null);
        mPagerAdapter.addTab(FriendsFragment.class, null);
        mPagerAdapter.addTab(AddContactsWithInviteFragment.class, null);

        new GCMRegistration(this).check();
    }
}
