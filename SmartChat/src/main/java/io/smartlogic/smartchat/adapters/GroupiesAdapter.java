package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.hypermedia.FriendSearch;

public class GroupiesAdapter extends ArrayAdapter<FriendSearch.Friend> {
    private Context mContext;
    private OnGroupieAddedListner mGroupieAddedListner;
    private List<FriendSearch.Friend> mFriends;

    public GroupiesAdapter(Context context, OnGroupieAddedListner groupieAddedListner, List<FriendSearch.Friend> friends) {
        super(context, R.layout.adapter_contacts);

        mContext = context;
        mGroupieAddedListner = groupieAddedListner;
        mFriends = friends;
    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public FriendSearch.Friend getItem(int position) {
        return mFriends.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.adapter_contacts, null);
        } else {
            view = convertView;
        }

        TextView contactName = (TextView) view.findViewById(R.id.contact_name);
        contactName.setText(mFriends.get(position).username);

        final FriendSearch.Friend friend = getItem(position);
        final Button addContact = (Button) view.findViewById(R.id.add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupieAddedListner.onGroupieAdded(friend);
                addContact.setBackgroundResource(android.R.drawable.checkbox_on_background);
            }
        });

        return view;
    }

    public interface OnGroupieAddedListner {
        public void onGroupieAdded(FriendSearch.Friend groupie);
    }
}
