package io.smartlogic.smartchat.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.api.ApiClient;

public class InviteUserFragment extends Fragment {
    private EditText mEmailView;
    private EditText mMessageView;

    private OnInvitationSentListener mInvitationSentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_user, null);

        Button pickFromContactsButton = (Button) view.findViewById(R.id.pick_from_contacts);
        pickFromContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 0);
            }
        });

        Button sendInvite = (Button) view.findViewById(R.id.send);
        sendInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InviteUserTask().execute();
            }
        });

        mEmailView = (EditText) view.findViewById(R.id.email);
        mMessageView = (EditText) view.findViewById(R.id.message);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri result = data.getData();
        String id = result.getLastPathSegment();

        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";

        Cursor cursor = getActivity().getContentResolver().
                query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, selection, new String[]{id}, null);

        final CharSequence[] emails = new String[cursor.getCount()];

        while (cursor.moveToNext()) {
            String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emails[cursor.getPosition()] = email;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select an Email")
                .setItems(emails, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mEmailView.setText(emails[which]);
                    }
                });
        builder.create().show();
    }

    public void setmInvitationSentListener(OnInvitationSentListener mInvitationSentListener) {
        this.mInvitationSentListener = mInvitationSentListener;
    }

    private class InviteUserTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String email = prefs.getString(Constants.EXTRA_EMAIL, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(email, encodedPrivateKey);
            client.inviteUser(mEmailView.getText().toString(), mMessageView.getText().toString());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mInvitationSentListener != null) {
                mInvitationSentListener.onInvitationSent();
            }
        }
    }

    public interface OnInvitationSentListener {
        public void onInvitationSent();
    }
}
