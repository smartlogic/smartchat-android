package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.models.Friend;

public class FriendsAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public FriendsAdapter(Context context, Cursor friends) {
        super(context, friends, false);

        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.adapter_friends, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Friend friend = Friend.fromCursor(cursor);

        TextView friendEmail = (TextView) view.findViewById(R.id.email);
        friendEmail.setText(friend.getEmail());
    }
}
