package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.os.Bundle;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.fragments.InviteUserFragment;

public class InviteUserActivity extends Activity implements InviteUserFragment.OnInvitationSentListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_user);

        if (savedInstanceState == null) {
            InviteUserFragment fragment = new InviteUserFragment();
            fragment.setmInvitationSentListener(this);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onInvitationSent() {
        finish();
    }
}
