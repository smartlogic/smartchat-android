package io.smartlogic.smartchat.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.AuthenticationException;

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString(Constants.EXTRA_USERNAME, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        ApiClient client = new ApiClient(username, encodedPrivateKey);

        String filePath = intent.getExtras().getString(Constants.EXTRA_FILE_PATH);
        String drawingPath = intent.getExtras().getString(Constants.EXTRA_DRAWING_PATH, "");
        int[] friendIds = intent.getExtras().getIntArray(Constants.EXTRA_FRIEND_IDS);
        int expireIn = intent.getExtras().getInt(Constants.EXTRA_EXPIRE_IN);

        List<Integer> friendIdList = new ArrayList<Integer>();
        for (int friendId : friendIds) {
            friendIdList.add(friendId);
        }

        try {
            client.uploadMedia(friendIdList, filePath, drawingPath, expireIn);
        } catch (AuthenticationException e) {
            Log.e(TAG, "Authentication error");
        }

        File file = new File(filePath);
        file.delete();

        file = new File(drawingPath);
        file.delete();
    }
}
