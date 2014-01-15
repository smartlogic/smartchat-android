package io.smartlogic.smartchat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import io.smartlogic.smartchat.Constants;

public class GCMRegistration {
    public static final String TAG = "GCMRegistration";
    private Context mContext;

    public GCMRegistration(Context context) {
        this.mContext = context;
    }

    public void check() {
        String registrationId = getRegistrationId();

        if (registrationId.equals("")) {
            // Register the device
            new RegisterDevice(mContext, getAppVersion()).execute();
        }
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String registrationId = prefs.getString(Constants.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private class RegisterDevice extends AsyncTask<Void, Void, Void> {
        public Context mContext;
        private Integer appVersion;

        public RegisterDevice(Context context, Integer appVersion) {
            this.mContext = context;
            this.appVersion = appVersion;
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String username = prefs.getString(Constants.EXTRA_USERNAME, "");
            String base64PrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
            try {
                String registrationId = gcm.register(Constants.GCM_SENDER_ID);
                ApiClient client = new ApiClient(username, base64PrivateKey);
                client.registerDevice(registrationId);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
                editor.putString(Constants.PROPERTY_REG_ID, registrationId);
                editor.commit();

                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
