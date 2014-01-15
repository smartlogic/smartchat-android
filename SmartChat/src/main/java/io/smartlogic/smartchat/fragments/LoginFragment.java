package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.GCMRegistration;
import io.smartlogic.smartchat.models.User;

public class LoginFragment extends Fragment {
    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        Button signUpButton = (Button) rootView.findViewById(R.id.log_in);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute();
            }
        });

        return rootView;
    }

    private class LoginTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            EditText username = (EditText) getView().findViewById(R.id.username);
            EditText password = (EditText) getView().findViewById(R.id.password);

            ApiClient client = new ApiClient();
            User user = client.login(username.getText().toString(), password.getText().toString());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.EXTRA_PRIVATE_KEY, user.getPrivateKey());
            editor.putString(Constants.EXTRA_USERNAME, user.getUsername());
            editor.commit();

            new GCMRegistration(getActivity()).check();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }
}
