package io.smartlogic.smartchat.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.activities.UploadActivity;
import io.smartlogic.smartchat.adapters.FriendsAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.models.Friend;

public class PickFriendsFragment extends ListFragment implements FriendsAdapter.OnFriendCheckedListener,
        UploadActivity.OnDoneSelectedListener {

    private FriendsAdapter mAdapter;
    private HashSet<Integer> mCheckedFriendIds;
    private String photoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCheckedFriendIds = new HashSet<Integer>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListShown(false);

        new LoadFriendsTask().execute();
    }

    @Override
    public void onFriendChecked(Friend friend, boolean isChecked) {
        if (isChecked) {
            mCheckedFriendIds.add(friend.getId());
        } else {
            mCheckedFriendIds.remove(friend.getId());
        }
    }

    @Override
    public void onDoneSelected() {
        new UploadTask().execute();
    }

    private ApiClient getApiClient() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String email = prefs.getString(Constants.EXTRA_EMAIL, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        return new ApiClient(email, encodedPrivateKey);
    }

    private class LoadFriendsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ApiClient client = getApiClient();
            List<Friend> friends = client.getFriends();

            mAdapter = new FriendsAdapter(getActivity(), friends, PickFriendsFragment.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setListAdapter(mAdapter);
            setListShown(true);
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ApiClient client = getApiClient();

            photoPath = getArguments().getString(Constants.EXTRA_PHOTO_PATH);

            List<Integer> friendIds = new ArrayList<Integer>();
            friendIds.addAll(mCheckedFriendIds);

            client.uploadMedia(friendIds, photoPath);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
