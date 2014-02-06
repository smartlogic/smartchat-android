package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.DisplaySmartChatActivity_;
import io.smartlogic.smartchat.adapters.NotificationsAdapter;
import io.smartlogic.smartchat.data.DataUriManager;
import io.smartlogic.smartchat.models.Notification;

public class NotificationsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private NotificationsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d("NotificationsFragment", "Clicked " + position);

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        Notification notification = Notification.fromCursor(cursor);

        if (notification.isViewed()) {
            return;
        }

        notification.markViewed();
        getActivity().getContentResolver().
                update(
                        DataUriManager.getNotificationUri(notification.getDatabaseId()),
                        notification.getAttributes(),
                        "_id = ?",
                        new String[]{String.valueOf(notification.getDatabaseId())}
                );

        Intent intent = new Intent(getActivity(), DisplaySmartChatActivity_.class);
        intent.putExtra(Constants.EXTRA_FILE_URL, notification.getFileUrl());
        intent.putExtra(Constants.EXTRA_DRAWING_FILE_URL, notification.getDrawingUrl());
        intent.putExtra(Constants.EXTRA_EXPIRE_IN, notification.getExpireIn());
        startActivity(intent);
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
        return new CursorLoader(getActivity(), DataUriManager.getNotificationsUri(), null, null, null, "_id DESC");
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        if (mAdapter == null) {
            mAdapter = new NotificationsAdapter(getActivity(), cursor);

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
