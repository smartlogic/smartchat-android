package io.smartlogic.smartchat.sync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.AuthenticationException;
import io.smartlogic.smartchat.data.DataUriManager;
import io.smartlogic.smartchat.hypermedia.HalNotifications;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.models.Notification;

public class SyncClient {
    public static final String TAG = "SyncClient";

    private Context mContext;
    private ContentResolver mContentResolver;


    public SyncClient(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    public void sync() {
        logModel(DataUriManager.getFriendsUri(), "Friends", new CursorPrintable() {
            @Override
            public void onNext(Cursor cursor) {
                Log.d(TAG, Friend.fromCursor(cursor).toString());
            }
        });
        logModel(DataUriManager.getNotificationsUri(), "Notifications", new CursorPrintable() {
            @Override
            public void onNext(Cursor cursor) {
                Log.d(TAG, Notification.fromCursor(cursor).toString());
            }
        });

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String username = prefs.getString(Constants.EXTRA_USERNAME, "");
            String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            ApiClient client = new ApiClient(username, encodedPrivateKey);

            syncFriends(client);
            syncNotifications(client);
        } catch (AuthenticationException e) {
            Log.e(TAG, "Authentication error");
        }
    }

    private void syncFriends(ApiClient client) throws AuthenticationException {
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

    private void syncNotifications(ApiClient client) throws AuthenticationException {
        List<HalNotifications.Notification> notifications = client.getNotifications();

        Cursor cursor = mContentResolver.query(DataUriManager.getNotificationsUri(), new String[]{"file_url"}, null, null, "_id");

        if (cursor == null) {
            throw new RuntimeException("Cursor is null");
        }

        List<String> filesInDatabase = new ArrayList<String>();

        while (cursor.moveToNext()) {
            filesInDatabase.add(cursor.getString(cursor.getColumnIndex("file_url")));
        }

        cursor.close();

        for (HalNotifications.Notification halNotification : notifications) {
            Notification notification = new Notification();

            notification.setCreatorId(halNotification.getCreatorId());
            notification.setCreatorUsername(halNotification.getCreatorUsername());
            notification.setFileUrl(halNotification.getFileUrl());
            notification.setDrawingUrl(halNotification.getDrawingUrl());

            if (filesInDatabase.contains(notification.getFileUrl())) {
                mContentResolver.update(DataUriManager.getNotificationUri(notification.getDatabaseId()), notification.getAttributes(), "file_url = ?", new String[]{notification.getFileUrl()});
            } else {
                mContentResolver.insert(DataUriManager.getNotificationsUri(), notification.getAttributes());
            }
        }
    }

    private interface CursorPrintable {
        public void onNext(Cursor cursor);
    }

    private void logModel(Uri uri, String modelName, CursorPrintable printable) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, "_id");

        if (cursor == null) {
            throw new RuntimeException("Cursor is null");
        }

        Log.d(TAG, modelName + " count: " + String.valueOf(cursor.getCount()));

        while (cursor.moveToNext()) {
            printable.onNext(cursor);
        }

        cursor.close();
    }
}
