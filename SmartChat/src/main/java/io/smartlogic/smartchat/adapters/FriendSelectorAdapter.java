package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.models.Friend;

public class FriendSelectorAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private OnFriendCheckedListener mFriendCheckedListener;

    public FriendSelectorAdapter(Context context, Cursor friends, OnFriendCheckedListener friendCheckedListener) {
        super(context, friends, false);

        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mFriendCheckedListener = friendCheckedListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.adapter_friend_select, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final Friend friend = Friend.fromCursor(cursor);

        TextView friendUsername = (TextView) view.findViewById(R.id.username);
        friendUsername.setText(friend.getUsername());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(mFriendCheckedListener.isFriendSelected(friend));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFriendCheckedListener.onFriendChecked(friend);
            }
        });
    }

    public Friend positionSelected(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return Friend.fromCursor(cursor);
    }

    public interface OnFriendCheckedListener {
        public void onFriendChecked(Friend fiend);

        public boolean isFriendSelected(Friend friend);
    }
}
