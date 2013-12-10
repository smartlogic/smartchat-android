package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.smartlogic.smartchat.activities.LoginActivity;
import io.smartlogic.smartchat.R;

public class InitialLoginFragment extends Fragment {
    public InitialLoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_initial_login, container, false);

        Button loginButton = (Button) rootView.findViewById(R.id.log_in);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).switchToLogin();
            }
        });

        Button signUpButton = (Button) rootView.findViewById(R.id.sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity) getActivity()).switchToSignUp();
            }
        });

        return rootView;
    }
}
