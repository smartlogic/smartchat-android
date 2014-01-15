package io.smartlogic.smartchat.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.HashSet;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.activities.UploadActivity;
import io.smartlogic.smartchat.adapters.FriendSelectorAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.ContextApiClient;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.services.UploadService;

public class PickFriendsFragment extends ListFragment implements FriendSelectorAdapter.OnFriendCheckedListener,
        UploadActivity.OnDoneSelectedListener {

    private FriendSelectorAdapter mAdapter;
    private HashSet<Integer> mCheckedFriendIds;

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
        int[] friendIds = new int[mCheckedFriendIds.size()];
        int i = 0;
        for (int id : mCheckedFriendIds) {
            friendIds[i] = id;
            i++;
        }

        Intent intent = new Intent(getActivity(), UploadService.class);
        intent.putExtra(Constants.EXTRA_FRIEND_IDS, friendIds);
        intent.putExtra(Constants.EXTRA_PHOTO_PATH, getArguments().getString(Constants.EXTRA_PHOTO_PATH));
        intent.putExtra(Constants.EXTRA_DRAWING_PATH, getArguments().getString(Constants.EXTRA_DRAWING_PATH, ""));
        getActivity().startService(intent);

        intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private class LoadFriendsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ContextApiClient client = new ContextApiClient(getActivity());
            List<Friend> friends = client.getFriends();

            mAdapter = new FriendSelectorAdapter(getActivity(), friends, PickFriendsFragment.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setListAdapter(mAdapter);
            setListShown(true);
        }
    }
}
