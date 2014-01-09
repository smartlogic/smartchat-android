package io.smartlogic.smartchat.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.FriendsAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.models.Friend;

public class FriendsFragment extends ListFragment {
    private FriendsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle(R.string.friends);
        }

        new LoadFriendsTask().execute();
    }

    private class LoadFriendsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String email = prefs.getString(Constants.EXTRA_EMAIL, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(email, encodedPrivateKey);
            List<Friend> friends = client.getFriends();

            mAdapter = new FriendsAdapter(getActivity(), friends);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setListAdapter(mAdapter);
        }
    }
}
