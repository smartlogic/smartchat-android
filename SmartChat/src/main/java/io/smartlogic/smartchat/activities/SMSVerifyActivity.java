package io.smartlogic.smartchat.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.SMSVerifyFragment;

public class SMSVerifyActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verify);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SMSVerifyFragment())
                    .commit();
        }
    }
}
