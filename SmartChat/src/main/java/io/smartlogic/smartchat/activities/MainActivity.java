package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.CameraFragment;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int mUIFlag = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        getActionBar().hide();

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CameraFragment())
                    .commit();
        }
    }
}
