package io.smartlogic.smartchat.views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.GroupiesAdapter;

public class GroupiesView extends LinearLayout {
    private Context mContext;
    private GroupiesAdapter mAdapter;

    public GroupiesView(Context context) {
        super(context);

        this.mContext = context;

        setOrientation(LinearLayout.VERTICAL);
    }

    public void setAdapter(GroupiesAdapter adapter) {
        this.mAdapter = adapter;

        if (mAdapter.getCount() > 0) {
            addView(View.inflate(mContext, R.layout.list_view_groupies_header, null));

            for (int i = 0; i < mAdapter.getCount(); i++) {
                addView(View.inflate(mContext, R.layout.view_divider, null));
                View view = mAdapter.getView(i, null, null);
                addView(view);
            }
        }
    }
}
