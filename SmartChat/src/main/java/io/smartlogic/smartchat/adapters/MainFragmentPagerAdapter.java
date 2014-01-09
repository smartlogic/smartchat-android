package io.smartlogic.smartchat.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

public class MainFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private final Activity mContext;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    public MainFragmentPagerAdapter(FragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());

        this.mContext = activity;
        this.mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if (mContext.getActionBar() != null) {
                    if (i == 0 && v < 0.5) {
                        mContext.getActionBar().hide();
                    } else {
                        mContext.getActionBar().show();
                    }
                }
            }

            @Override
            public void onPageSelected(int i) {
                mContext.setTitle(mTabs.get(i).title);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void addTab(Class<?> clss, Bundle args, String title) {
        TabInfo info = new TabInfo(clss, args, title);
        mTabs.add(info);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    static final class TabInfo {
        private final Class<?> clss;
        private final Bundle args;
        private final String title;

        TabInfo(Class<?> _class, Bundle _args, String _title) {
            clss = _class;
            args = _args;
            title = _title;
        }
    }
}
