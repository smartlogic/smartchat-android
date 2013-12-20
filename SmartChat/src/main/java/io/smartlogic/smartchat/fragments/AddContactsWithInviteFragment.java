package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.InviteUserActivity;

public class AddContactsWithInviteFragment extends AddContactsFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.invite, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite:
                Intent intent = new Intent(getActivity(), InviteUserActivity.class);
                startActivityForResult(intent, 0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
