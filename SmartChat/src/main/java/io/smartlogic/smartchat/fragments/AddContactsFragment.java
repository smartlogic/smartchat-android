package io.smartlogic.smartchat.fragments;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.adapters.ContactsAdapter;
import io.smartlogic.smartchat.adapters.GroupiesAdapter;
import io.smartlogic.smartchat.api.ContextApiClient;
import io.smartlogic.smartchat.api.responses.FriendSearchResults;
import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.sync.AccountHelper;
import io.smartlogic.smartchat.views.GroupiesView;

public class AddContactsFragment extends Fragment implements ContactsAdapter.OnContactAddedListener, GroupiesAdapter.OnGroupieAddedListner {
    private Cursor mContactsCursor;
    private List<FriendSearch.Friend> mContactsOnSmartChat;
    private List<FriendSearch.Friend> mGroupies;

    private ListView mContactsListView;
    private GroupiesView mGroupiesView;

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
    public void onGroupieAdded(FriendSearch.Friend groupie) {
        new AddContactTask().execute(groupie.getAddLink());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_contacts, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGroupiesView = new GroupiesView(getActivity());
        mContactsListView = (ListView) view.findViewById(R.id.contacts_list);
        mContactsListView.addHeaderView(mGroupiesView);
        mContactsListView.addHeaderView(View.inflate(getActivity(), R.layout.list_view_contacts_header, null));

        TextView empty = (TextView) view.findViewById(R.id.empty_contacts_list);
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

            ContextApiClient client = new ContextApiClient(getActivity());

            FriendSearchResults friendSearchResults = client.searchForFriends(phoneNumbers, emails);
            mContactsOnSmartChat = friendSearchResults.getFoundFriends();
            mGroupies = friendSearchResults.getGroupies();

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

                mContactsListView.setAdapter(new ContactsAdapter(getActivity(), AddContactsFragment.this, mContactsCursor));
                if (mContactsListView.getCount() == 0) {
                    TextView emptyContactsList = (TextView) getView().findViewById(R.id.empty_contacts_list);
                    emptyContactsList.setVisibility(View.VISIBLE);
                }

                mGroupiesView.setAdapter(new GroupiesAdapter(getActivity(), AddContactsFragment.this, mGroupies));
            }
        }
    }

    private class AddContactTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... friendUrls) {
            ContextApiClient client = new ContextApiClient(getActivity());
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
