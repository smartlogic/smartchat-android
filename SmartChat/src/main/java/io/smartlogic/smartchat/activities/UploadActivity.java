package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.PickFriendsFragment;

public class UploadActivity extends Activity {
    private PickFriendsFragment mFriendsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString(Constants.EXTRA_PHOTO_PATH, getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH));
            mFriendsFragment = new PickFriendsFragment();
            mFriendsFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mFriendsFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.done:
                mFriendsFragment.onDoneSelected();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface OnDoneSelectedListener {
        public void onDoneSelected();
    }
}