package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.os.Bundle;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.InitialLoginFragment;
import io.smartlogic.smartchat.fragments.LoginFragment;
import io.smartlogic.smartchat.fragments.SignUpFragment;

public class LoginActivity extends Activity {
    private boolean initialLoginFragment = true;

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

    @Override
    public void onBackPressed() {
        if (initialLoginFragment) {
            finish();
        } else {
            initialLoginFragment = true;

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new InitialLoginFragment())
                    .commit();
        }
    }

    public void switchToLogin() {
        initialLoginFragment = false;

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    public void switchToSignUp() {
        initialLoginFragment = false;

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SignUpFragment())
                .commit();
    }
}
