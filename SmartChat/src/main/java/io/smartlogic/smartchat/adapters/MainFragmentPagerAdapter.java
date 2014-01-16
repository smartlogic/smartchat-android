package io.smartlogic.smartchat.adapters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.AddContactsWithInviteFragment;
import io.smartlogic.smartchat.fragments.CameraFragment;
import io.smartlogic.smartchat.fragments.FriendsFragment;
import io.smartlogic.smartchat.fragments.NotificationsFragment;

public class MainFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private static final int TAB_COUNT = 4;
    private static final int POSITION_LIST = 0;
    private static final int POSITION_CAMERA = 1;
    private static final int POSITION_FRIENDS = 2;
    private static final int POSITION_ADD_FRIENDS = 3;
    private final Activity mContext;
    private final ViewPager mViewPager;
    private String[] titles;
    private PageChangeListener mPageChangeListener;

    public MainFragmentPagerAdapter(FragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());

        mContext = activity;
        mViewPager = pager;
        mViewPager.setAdapter(this);

        mPageChangeListener = new PageChangeListener();
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setCurrentItem(POSITION_CAMERA);
    }

    public void displayNotifications() {
        mViewPager.setCurrentItem(POSITION_LIST);
        mContext.setTitle(getTitle(POSITION_LIST));
        if (mContext.getActionBar() != null) {
            mContext.getActionBar().show();
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case POSITION_LIST:
                return Fragment.instantiate(mContext, NotificationsFragment.class.getName(), null);
            case POSITION_CAMERA:
                return Fragment.instantiate(mContext, CameraFragment.class.getName(), null);
            case POSITION_FRIENDS:
                return Fragment.instantiate(mContext, FriendsFragment.class.getName(), null);
            case POSITION_ADD_FRIENDS:
                return Fragment.instantiate(mContext, AddContactsWithInviteFragment.class.getName(), null);
        }

        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    private String getTitle(int position) {
        if (titles == null) {
            titles = new String[]{mContext.getString(R.string.notifications), "", mContext.getString(R.string.friends), mContext.getString(R.string.add_contacts)};
        }

        return titles[position];
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int i, float v, int i2) {
            if (mContext.getActionBar() != null) {
                if (i == POSITION_CAMERA && v < 0.5) {
                    mContext.getActionBar().hide();
                } else {
                    mContext.getActionBar().show();
                }
            }
        }

        @Override
        public void onPageSelected(int i) {
            mContext.setTitle(getTitle(i));
            mContext.invalidateOptionsMenu();
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }
}
