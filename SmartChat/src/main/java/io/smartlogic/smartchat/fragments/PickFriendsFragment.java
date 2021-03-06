package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.HashSet;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.activities.PickFriendsActivity;
import io.smartlogic.smartchat.adapters.FriendSelectorAdapter;
import io.smartlogic.smartchat.data.DataUriManager;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.services.UploadService;

public class PickFriendsFragment extends ListFragment implements FriendSelectorAdapter.OnFriendCheckedListener,
        PickFriendsActivity.OnDoneSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

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

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onFriendChecked(Friend friend) {
        if (mCheckedFriendIds.contains(friend)) {
            mCheckedFriendIds.remove(friend.getId());
        } else {
            mCheckedFriendIds.add(friend.getId());
        }
    }

    @Override
    public boolean isFriendSelected(Friend friend) {
        return mCheckedFriendIds.contains(friend);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Friend friend = mAdapter.positionSelected(position);
        onFriendChecked(friend);

        CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkbox);
        checkBox.setChecked(!checkBox.isChecked());
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
        intent.putExtra(Constants.EXTRA_FILE_PATH, getArguments().getString(Constants.EXTRA_FILE_PATH));
        intent.putExtra(Constants.EXTRA_DRAWING_PATH, getArguments().getString(Constants.EXTRA_DRAWING_PATH, ""));
        intent.putExtra(Constants.EXTRA_EXPIRE_IN, getArguments().getInt(Constants.EXTRA_EXPIRE_IN));
        getActivity().startService(intent);

        intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataUriManager.getFriendsUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mAdapter == null) {
            mAdapter = new FriendSelectorAdapter(getActivity(), cursor, PickFriendsFragment.this);

            setListAdapter(mAdapter);
        } else {
            mAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }
}
