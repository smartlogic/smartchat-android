package io.smartlogic.smartchat.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.FriendsAdapter;
import io.smartlogic.smartchat.data.DataUriManager;

public class FriendsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private FriendsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView empty = (TextView) view.findViewById(android.R.id.empty);
        empty.setVisibility(View.GONE);

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataUriManager.getFriendsUri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter = new FriendsAdapter(getActivity(), cursor);
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }
}
