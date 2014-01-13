package io.smartlogic.smartchat.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import io.smartlogic.smartchat.Constants;

public class AccountHelper {
    public static Account getAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);

        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        if (accounts.length > 0) {
            return accounts[0];
        }

        return null;
    }
}
