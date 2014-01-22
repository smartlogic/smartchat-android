package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.ContactsActivity;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.GCMRegistration;
import io.smartlogic.smartchat.api.RegistrationException;
import io.smartlogic.smartchat.models.User;
import io.smartlogic.smartchat.sync.SyncClient;

public class SignUpFragment extends Fragment {
    private Button mSignUpButton;
    EditText mUsernameView;
    EditText mEmailView;
    EditText mPasswordView;

    Handler mHandler;

    public SignUpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mSignUpButton = (Button) rootView.findViewById(R.id.sign_up);
        enableSignUpButton();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHandler = new Handler();

        mUsernameView = (EditText) getView().findViewById(R.id.username);
        mEmailView = (EditText) getView().findViewById(R.id.email);
        mPasswordView = (EditText) getView().findViewById(R.id.password);
    }

    public void attemptSignUp() {
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.password_blank));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.email_blank));
            focusView = mEmailView;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailView.setError(getString(R.string.email_invalid));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.username_blank));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new RegistrationTask(username, email, password).execute();

            disableSignUpButton();
        }
    }

    private void enableSignUpButton() {
        mSignUpButton.setEnabled(true);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });
    }

    private void disableSignUpButton() {
        mSignUpButton.setOnClickListener(null);
        mSignUpButton.setEnabled(false);
    }

    private class RegistrationTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private String email;
        private String password;

        private boolean registrationSuccessful = true;

        public RegistrationTask(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            try {
                ApiClient client = new ApiClient();
                String base64PrivateKey = client.registerUser(user);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.EXTRA_PRIVATE_KEY, base64PrivateKey);
                editor.putString(Constants.EXTRA_USERNAME, user.getUsername());
                editor.commit();

                new GCMRegistration(getActivity()).check();

                new SyncClient(getActivity()).sync();
            } catch (RegistrationException e) {
                for (String key : e.errors.keySet()) {
                    EditText errorView = null;
                    final StringBuilder out = new StringBuilder();

                    if (key.equals("username")) {
                        out.append("Username ");
                        errorView = mUsernameView;
                    } else if (key.equals("email")) {
                        out.append("Email ");
                        errorView = mEmailView;
                    } else if (key.equals("password")) {
                        out.append("Password ");
                        errorView = mPasswordView;
                    }

                    for (String error : e.errors.get(key)) {
                        out.append(error);
                    }

                    final EditText view = errorView;
                    if (view != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                view.setError(out.toString());
                                view.requestFocus();

                                enableSignUpButton();
                            }
                        });
                    }
                }

                registrationSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (registrationSuccessful) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                startActivity(intent);
            }
        }
    }
}
