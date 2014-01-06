package io.smartlogic.smartchat.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.adapters.ContactsAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.hypermedia.FriendSearch;

public class AddContactsFragment extends ListFragment implements ContactsAdapter.OnContactAddedListener {
    private Cursor mContactsCursor;
    private List<FriendSearch.Friend> mContactsOnSmartChat;

    public AddContactsFragment() {
    }

    @Override
    public void onContactAdded(int contactId) {
        for (FriendSearch.Friend friend : mContactsOnSmartChat) {
            if (friend.contactId == contactId) {
                new AddContactTask().execute(friend.getAddLink());
                return;
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListShown(false);

        new FindContactsTask().execute();
    }

    private class FindContactsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Map<String, Integer> phoneNumbers = new HashMap<String, Integer>();

            String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "' and " +
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '" + ("1") + "'";

            Cursor cursor = getActivity().getContentResolver().
                    query(ContactsContract.Contacts.CONTENT_URI, null, selection, null, null);

            int idFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);

            cursor.moveToFirst();

            do {
                int contactId = cursor.getInt(idFieldColumnIndex);

                String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                String[] phoneSelectionArgs = new String[]{String.valueOf(contactId)};

                Cursor contactCursor = getActivity().getContentResolver().
                        query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, phoneSelection, phoneSelectionArgs, null);
                contactCursor.moveToFirst();

                int phoneFieldColumnIndex = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (contactCursor.getCount() < 1) {
                    continue;
                }

                do {
                    String phoneNumber = contactCursor.getString(phoneFieldColumnIndex);
                    phoneNumbers.put(phoneNumber, contactId);
                }
                while (contactCursor.moveToNext());

                contactCursor.close();
            } while (cursor.moveToNext());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String email = prefs.getString(Constants.EXTRA_EMAIL, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(email, encodedPrivateKey);
            mContactsOnSmartChat = client.searchForFriends(phoneNumbers);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String selection = "";
            String[] selectionArgs = new String[mContactsOnSmartChat.size()];
            for (int i = 0; i < mContactsOnSmartChat.size(); i++) {
                selection += ContactsContract.Contacts._ID + " = ? and ";
                selectionArgs[i] = String.valueOf(mContactsOnSmartChat.get(i).contactId);
            }

            // Check that selection is long enough to remove "and "
            if (mContactsOnSmartChat.size() == 0) {
                selection = selection.substring(0, selection.length() - 4);
            }

            mContactsCursor = getActivity().getContentResolver().
                    query(ContactsContract.Contacts.CONTENT_URI, null, selection, selectionArgs, null);

            setListAdapter(new ContactsAdapter(getActivity(), AddContactsFragment.this, mContactsCursor));
            setListShown(true);
        }
    }

    private class AddContactTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... friendUrls) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String email = prefs.getString(Constants.EXTRA_EMAIL, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(email, encodedPrivateKey);
            client.addFriend(friendUrls[0]);

            return null;
        }
    }
}
