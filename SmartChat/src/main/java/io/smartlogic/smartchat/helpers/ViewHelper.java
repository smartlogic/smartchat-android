package io.smartlogic.smartchat.helpers;

import android.app.Activity;
import android.os.Build;
import android.view.View;

public class ViewHelper {
    public static void hideSystemUI(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = 0;
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static void showSystemUI(Activity activity) {
        if (Build.VERSION.SDK_INT > 16) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
        }
    }
}
