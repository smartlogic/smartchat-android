package io.smartlogic.smartchat.data;

import android.content.UriMatcher;
import android.net.Uri;

import io.smartlogic.smartchat.Constants;

public class DataUriManager {
    public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/");

    protected static final int FRIENDS = 1;
    protected static final int FRIENDS_ID = 2;

    protected static final int NOTIFICATIONS = 3;
    protected static final int NOTIFICATIONS_ID = 4;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(Constants.AUTHORITY, "friends", FRIENDS);
        uriMatcher.addURI(Constants.AUTHORITY, "friends/#", FRIENDS_ID);

        uriMatcher.addURI(Constants.AUTHORITY, "notifications/", NOTIFICATIONS);
        uriMatcher.addURI(Constants.AUTHORITY, "notifications/#", NOTIFICATIONS_ID);
    }

    public static int match(Uri uri) {
        return uriMatcher.match(uri);
    }

    public static Uri getFriendsUri() {
        Uri.Builder builder = CONTENT_URI.buildUpon();
        builder.appendPath("friends");
        return builder.build();
    }

    public static Uri getFriendUri(long id) {
        Uri.Builder builder = getFriendsUri().buildUpon();
        builder.appendPath(String.valueOf(id));
        return builder.build();
    }

    public static Uri getNotificationsUri() {
        Uri.Builder builder = CONTENT_URI.buildUpon();
        builder.appendPath("notifications");
        return builder.build();
    }

    public static Uri getNotificationUri(long id) {
        Uri.Builder builder = getNotificationsUri().buildUpon();
        builder.appendPath(String.valueOf(id));
        return builder.build();
    }
}
