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

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Called");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString(Constants.EXTRA_EMAIL, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        ApiClient client = new ApiClient(email, encodedPrivateKey);

        String photoPath = intent.getExtras().getString(Constants.EXTRA_PHOTO_PATH);
        String drawingPath = intent.getExtras().getString(Constants.EXTRA_DRAWING_PATH, "");
        int[] friendIds = intent.getExtras().getIntArray(Constants.EXTRA_FRIEND_IDS);

        List<Integer> friendIdList = new ArrayList<Integer>();
        for (int friendId : friendIds) {
            friendIdList.add(friendId);
        }

        client.uploadMedia(friendIdList, photoPath, drawingPath);

        File photoFile = new File(photoPath);
        photoFile.delete();

        Log.d(TAG, "Finished");
    }
}
