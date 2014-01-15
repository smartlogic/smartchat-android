package io.smartlogic.smartchat.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.activities.LogoutActivity;
import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.models.User;

public class ContextApiClient {
    final private Context mContext;
    final private ApiClient mClient;

    public ContextApiClient(Context context) {
        this.mContext = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String username = prefs.getString(Constants.EXTRA_USERNAME, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        mClient = new ApiClient(username, encodedPrivateKey);
    }

    public User login(String username, String password) {
        try {
            return mClient.login(username, password);
        } catch (AuthenticationException e) {
            signUserOut();
        }
        return new User();
    }

    public List<FriendSearch.Friend> searchForFriends(Map<String, Integer> phoneNumbers, Map<String, Integer> emails) {
        try {
            return mClient.searchForFriends(phoneNumbers, emails);
        } catch (AuthenticationException e) {
            signUserOut();
        }
        return new ArrayList<FriendSearch.Friend>();
    }

    public void addFriend(String addFriendUrl) {
        try {
            mClient.addFriend(addFriendUrl);
        } catch (AuthenticationException e) {
            signUserOut();
        }
    }

    public void registerDevice(String deviceToken) {
        try {
            mClient.registerDevice(deviceToken);
        } catch (AuthenticationException e) {
            signUserOut();
        }
    }

    public List<Friend> getFriends() {
        try {
            mClient.getFriends();
        } catch (AuthenticationException e) {
            signUserOut();
        }
        return new ArrayList<Friend>();
    }

    public void inviteUser(String email, String message) {
        try {
            mClient.inviteUser(email, message);
        } catch (AuthenticationException e) {
            signUserOut();
        }
    }

    private void signUserOut() {
        Intent intent = new Intent(mContext, LogoutActivity.class);
        mContext.startActivity(intent);
    }
}
