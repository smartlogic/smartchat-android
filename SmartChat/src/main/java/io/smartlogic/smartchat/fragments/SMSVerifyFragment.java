package io.smartlogic.smartchat.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.ContactsActivity;
import io.smartlogic.smartchat.api.ContextApiClient;
import io.smartlogic.smartchat.api.responses.SMSVerificationResponse;

public class SMSVerifyFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_verify, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button sendVerification = (Button) view.findViewById(R.id.verify_my_number);
        sendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendSMSVerification().execute();
            }
        });

        Button skip = (Button) view.findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToContacts();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        goToContacts();
    }

    private void goToContacts() {
        Intent intent = new Intent(getActivity(), ContactsActivity.class);
        startActivity(intent);
    }

    private class SendSMSVerification extends AsyncTask<Void, Void, Void> {
        private SMSVerificationResponse mVerificationCode;

        @Override
        protected Void doInBackground(Void... params) {
            ContextApiClient client = new ContextApiClient(getActivity());

            mVerificationCode = client.getSmsVerificationCode();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mVerificationCode != null) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("address", mVerificationCode.getPhoneNumber());
                sendIntent.putExtra("sms_body", "Please do not change! - " + mVerificationCode.getSmsVerificationCode());
                sendIntent.setType("vnd.android-dir/mms-sms");
                startActivityForResult(sendIntent, 1);
            }
        }
    }
}
