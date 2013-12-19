package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

public class MainFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private final Context mContext;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    public MainFragmentPagerAdapter(FragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());

        this.mContext = activity;
        this.mViewPager = pager;
        mViewPager.setAdapter(this);
    }

    public void addTab(Class<?> clss, Bundle args) {
        TabInfo info = new TabInfo(clss, args);
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

        TabInfo(Class<?> _class, Bundle _args) {
            clss = _class;
            args = _args;
        }
    }
}
