package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.models.Friend;

public class FriendSelectorAdapter extends ArrayAdapter<Friend> {
    private Context mContext;
    private List<Friend> mFriends;
    private OnFriendCheckedListener mFriendCheckedListener;

    public FriendSelectorAdapter(Context context, List<Friend> friends, OnFriendCheckedListener friendCheckedListener) {
        super(context, R.layout.adapter_friends);

        this.mContext = context;
        this.mFriends = friends;
        this.mFriendCheckedListener = friendCheckedListener;
    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.adapter_friend_select, null);
        } else {
            view = convertView;
        }

        TextView friendUsername = (TextView) view.findViewById(R.id.username);
        friendUsername.setText(mFriends.get(position).getUsername());

        final int friendPosition = position;
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFriendCheckedListener.onFriendChecked(mFriends.get(friendPosition), isChecked);
            }
        });

        return view;
    }

    public interface OnFriendCheckedListener {
        public void onFriendChecked(Friend fiend, boolean isChecked);
    }
}
