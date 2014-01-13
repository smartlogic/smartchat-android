package io.smartlogic.smartchat.data;

import android.content.UriMatcher;
import android.net.Uri;

import io.smartlogic.smartchat.Constants;

public class DataUriManager {
    public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/");

    protected static final int FRIENDS = 1;
    protected static final int FRIENDS_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(Constants.AUTHORITY, "friends", FRIENDS);
        uriMatcher.addURI(Constants.AUTHORITY, "friends/#", FRIENDS_ID);
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
}
