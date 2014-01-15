package io.smartlogic.smartchat.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.smartlogic.smartchat.Constants;

public class LogoutActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();

        new AlertDialog.Builder(this)
                .setTitle("Authentication Error")
                .setMessage("There was an error authenticating to SmartChat. Please sign in again.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }
                })
                .create()
                .show();
    }

    private void signOut() {
        // remove account and preferences

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().clear().commit();

        AccountManager accountManager = AccountManager.get(this);

        if (accountManager == null) {
            return;
        }

        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        // Account already exists
        if (accounts.length < 1) {
            return;
        }

        Account account = accounts[0];
        accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
            @Override
            public void run(AccountManagerFuture<Boolean> future) {
                Intent intent = new Intent(LogoutActivity.this, LauncherActivity.class);
                startActivity(intent);
            }
        }, null);
    }
}
