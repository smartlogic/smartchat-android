package io.smartlogic.smartchat.fragments;

import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import io.smartlogic.smartchat.ContactsAdapter;

public class AddContactsFragment extends ListFragment {
    public AddContactsFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("smartchat", "fragment loaded");

        Cursor cursor = getActivity().getContentResolver().
                query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        setListAdapter(new ContactsAdapter(getActivity(), cursor));
    }
}
