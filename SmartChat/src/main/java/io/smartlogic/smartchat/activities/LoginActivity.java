package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.InitialLoginFragment;
import io.smartlogic.smartchat.fragments.LoginFragment;
import io.smartlogic.smartchat.fragments.SignUpFragment;

public class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new InitialLoginFragment())
                    .commit();
        }
    }

    public void switchToLogin() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    public void switchToSignUp() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SignUpFragment())
                .commit();
    }
}
