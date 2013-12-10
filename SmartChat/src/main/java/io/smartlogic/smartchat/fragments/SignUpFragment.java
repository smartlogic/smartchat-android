package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
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
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.models.User;

public class SignUpFragment extends Fragment {
    public SignUpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        Button signUpButton = (Button) rootView.findViewById(R.id.sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegistrationTask().execute();
            }
        });

        return rootView;
    }

    private class RegistrationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            EditText email = (EditText) getView().findViewById(R.id.email);
            EditText password = (EditText) getView().findViewById(R.id.password);
            EditText phoneNumber = (EditText) getView().findViewById(R.id.phone_number);

            User user = new User();
            user.setEmail(email.getText().toString());
            user.setPassword(password.getText().toString());
            user.setPhoneNumber(phoneNumber.getText().toString());

            ApiClient client = new ApiClient();
            String base64PrivateKey = client.registerUser(user);


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.EXTRA_PRIVATE_KEY, base64PrivateKey);
            editor.commit();

            return null;
        }
    }
}
