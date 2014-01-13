package io.smartlogic.smartchat.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DataProvider extends ContentProvider {
    private DatabaseHelper database;

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("friends");

        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DatabaseHelper.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        Cursor cursor = null;

        if (database.getReadableDatabase() != null) {
            cursor = qb.query(database.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy);
        }

        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id;
        Uri newUri = null;

        if (database != null && database.getWritableDatabase() != null) {
            id = database.getWritableDatabase().insert("friends", "id", values);
            newUri = DataUriManager.getFriendUri(id);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null, false);

            if (newUri != null) {
                getContext().getContentResolver().notifyChange(newUri, null, false);
            }
        }

        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        if (database != null && database.getWritableDatabase() != null) {
            count = database.getWritableDatabase().delete("friends", selection, selectionArgs);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        if (database != null && database.getWritableDatabase() != null) {
            count = database.getWritableDatabase().update("friends", values, selection, selectionArgs);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }

        return count;
    }
}
