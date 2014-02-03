package io.smartlogic.smartchat.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.MainFragmentPagerAdapter;
import io.smartlogic.smartchat.api.GCMRegistration;
import io.smartlogic.smartchat.sync.SyncClient;

public class MainActivity extends FragmentActivity {
    public static final String TAG = "main activity";
    ViewPager mViewPager;
    MainFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT > 16) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        if (getIntent().getBooleanExtra(Constants.EXTRA_SYNC, false)) {
            new SyncNowTask().execute();
        }

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);
        setContentView(mViewPager);

        mPagerAdapter = new MainFragmentPagerAdapter(this, mViewPager);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean(Constants.EXTRA_GO_TO_NOTIFICATIONS)) {
                mPagerAdapter.displayNotifications();
                if (getActionBar() != null) {
                    getActionBar().show();
                }
            } else if (getIntent().getExtras().getBoolean(Constants.EXTRA_GO_TO_ADD_CONTACTS)) {
                mPagerAdapter.displayAddContacts();
                if (getActionBar() != null) {
                    getActionBar().show();
                }
            }
        }

        new GCMRegistration(this).check();

        new CreateAccountTask(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT > 16 && mViewPager.getCurrentItem() == mPagerAdapter.POSITION_CAMERA) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private class SyncNowTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            new SyncClient(MainActivity.this).sync();

            return null;
        }
    }

    private class CreateAccountTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public CreateAccountTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            AccountManager accountManager = AccountManager.get(mContext);

            if (accountManager == null) {
                return null;
            }

            Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

            // Account already exists
            if (accounts.length > 0) {
                return null;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String username = prefs.getString(Constants.EXTRA_USERNAME, "");

            Account account = new Account(username, Constants.ACCOUNT_TYPE);
            accountManager.addAccountExplicitly(account, "", null);

            ContentResolver.setIsSyncable(account, Constants.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, Constants.AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, Constants.AUTHORITY, new Bundle(), 60 * 60 * 24); // Once a day

            return null;
        }
    }
}
