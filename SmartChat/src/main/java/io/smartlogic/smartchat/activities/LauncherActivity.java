package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import io.smartlogic.smartchat.Constants;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString(Constants.EXTRA_EMAIL, "");

        Intent intent;
        if (email.equals("")) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);

            if (getIntent().getExtras() != null) {
                intent.putExtra(Constants.EXTRA_SYNC, getIntent().getExtras().getBoolean(Constants.EXTRA_SYNC, false));
            }
        }

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }
}
