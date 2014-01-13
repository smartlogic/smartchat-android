package io.smartlogic.smartchat.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.data.DataUriManager;
import io.smartlogic.smartchat.models.Friend;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    private Context mContext;
    private ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        logFriends();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String email = prefs.getString(Constants.EXTRA_EMAIL, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        ApiClient client = new ApiClient(email, encodedPrivateKey);

        List<Friend> friends = client.getFriends();

        Cursor cursor = mContentResolver.query(DataUriManager.getFriendsUri(), new String[]{"id"}, null, null, null);

        if (cursor == null) {
            throw new RuntimeException("Cursor is null");
        }

        List<Integer> idsInDatabase = new ArrayList<Integer>();

        while (cursor.moveToNext()) {
            idsInDatabase.add(cursor.getInt(cursor.getColumnIndex("id")));
        }

        cursor.close();

        for (Friend friend : friends) {
            if (idsInDatabase.contains(friend.getId())) {
                mContentResolver.update(DataUriManager.getFriendUri(friend.getId()), friend.getAttributes(), "id = ?", new String[]{String.valueOf(friend.getId())});
            } else {
                mContentResolver.insert(DataUriManager.getFriendsUri(), friend.getAttributes());
            }
        }
    }

    private void logFriends() {
        Cursor cursor = mContext.getContentResolver().query(DataUriManager.getFriendsUri(), null, null, null, null);

        if (cursor == null) {
            throw new RuntimeException("Cursor is null");
        }

        Log.d(TAG, "Friend count: " + String.valueOf(cursor.getCount()));

        while (cursor.moveToNext()) {
            Log.d(TAG, Friend.fromCursor(cursor).toString());
        }

        cursor.close();
    }
}
