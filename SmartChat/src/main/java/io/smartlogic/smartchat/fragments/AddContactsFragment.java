package io.smartlogic.smartchat.fragments;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.ContactsAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.sync.AccountHelper;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_contacts, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView empty = (TextView) view.findViewById(android.R.id.empty);
        empty.setVisibility(View.GONE);

        final Button button = (Button) view.findViewById(R.id.find_friends);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                button.setVisibility(View.GONE);

                new FindContactsTask().execute();
            }
        });
    }

    private class FindContactsTask extends AsyncTask<Void, Void, Void> {
        Map<String, Integer> phoneNumbers = new HashMap<String, Integer>();
        Map<String, Integer> emails = new HashMap<String, Integer>();

        @Override
        protected Void doInBackground(Void... params) {
            loadPhoneNumbers();
            loadEmails();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String email = prefs.getString(Constants.EXTRA_EMAIL, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(email, encodedPrivateKey);
            mContactsOnSmartChat = client.searchForFriends(phoneNumbers, emails);

            return null;
        }

        private void loadPhoneNumbers() {
            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '" + ("1") + "'";

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
                    contactCursor.close();
                    continue;
                }

                do {
                    String phoneNumber = contactCursor.getString(phoneFieldColumnIndex);
                    phoneNumbers.put(phoneNumber, contactId);
                }
                while (contactCursor.moveToNext());

                contactCursor.close();
            } while (cursor.moveToNext());

            cursor.close();
        }

        private void loadEmails() {
            Cursor cursor = getActivity().getContentResolver().
                    query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            int idFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID);

            cursor.moveToFirst();

            do {
                int contactId = cursor.getInt(idFieldColumnIndex);
                String emailSelection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
                String[] emailSelectionArgs = new String[]{String.valueOf(contactId)};

                Cursor contactCursor = getActivity().getContentResolver().
                        query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, emailSelection, emailSelectionArgs, null);
                contactCursor.moveToFirst();

                int contactEmailColumnIndex = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                if (contactCursor.getCount() < 1) {
                    contactCursor.close();
                    continue;
                }

                do {
                    String email = contactCursor.getString(contactEmailColumnIndex);
                    emails.put(email, contactId);
                } while (contactCursor.moveToNext());

                contactCursor.close();
            } while (cursor.moveToNext());

            cursor.close();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String selection = ContactsContract.Contacts._ID + " = -1 or ";
            String[] selectionArgs = new String[mContactsOnSmartChat.size()];
            for (int i = 0; i < mContactsOnSmartChat.size(); i++) {
                selection += ContactsContract.Contacts._ID + " = ? or ";
                selectionArgs[i] = String.valueOf(mContactsOnSmartChat.get(i).contactId);
            }

            selection = selection.substring(0, selection.length() - 3);

            if (getActivity() != null) {
                mContactsCursor = getActivity().getContentResolver().
                        query(ContactsContract.Contacts.CONTENT_URI, null, selection, selectionArgs, null);

                ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);

                setListAdapter(new ContactsAdapter(getActivity(), AddContactsFragment.this, mContactsCursor));
            }
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

        @Override
        protected void onPostExecute(Void aVoid) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(AccountHelper.getAccount(getActivity()), Constants.AUTHORITY, bundle);
        }
    }
}
