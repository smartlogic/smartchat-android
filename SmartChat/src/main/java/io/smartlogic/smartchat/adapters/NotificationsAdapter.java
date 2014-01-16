package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.models.Notification;

public class NotificationsAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public NotificationsAdapter(Context context, Cursor friends) {
        super(context, friends, false);

        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.adapter_notifications, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Notification notification = Notification.fromCursor(cursor);

        TextView friendUsername = (TextView) view.findViewById(R.id.creator_username);
        friendUsername.setText(notification.getCreatorUsername());

        TextView viewed = (TextView) view.findViewById(R.id.viewed);
        if (notification.isViewed()) {
            viewed.setText("viewed");
        } else {
            viewed.setText("new");
        }
    }
}
