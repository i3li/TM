package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = GroupsActivity.class.getName();
    private static final int RC_SIGN_IN = 1;
    //

    // Views

    // Before signing in
        private LinearLayout signinLinearLayout;
        private TextView signinErrorTextView;
        private Button signinButton;
    //

    // After signing in
        private RecyclerView groupsRecyclerView;
    //

    //

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setupViews();
        if (auth.getCurrentUser() == null)
            setupViewsForSignIn(null);
        else
            setupViewsForGroups();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                user = auth.getCurrentUser();
                setupViewsForGroups();
            } else {
                int messageId = R.string.sign_in_message;
                if (response != null)
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                        messageId = R.string.connection_error;
                    else
                        messageId = R.string.general_error;
                setupViewsForSignIn(getString(messageId));
            }
        }
    }

    private void setupViews() {
        signinLinearLayout = (LinearLayout) findViewById(R.id.ll_sign_in);
        signinErrorTextView = (TextView) findViewById(R.id.tv_sign_in_error);
        signinButton = (Button) findViewById(R.id.btn_sign_in);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        groupsRecyclerView = (RecyclerView) findViewById(R.id.rv_groups);
    }

    private void setupViewsForSignIn(String errorMessage) {
        groupsRecyclerView.setVisibility(View.GONE);
        signinLinearLayout.setVisibility(View.VISIBLE);
        String text = errorMessage == null ? getString(R.string.sign_in_message) : errorMessage;
        signinErrorTextView.setText(text);
    }

    private void setupViewsForGroups() {
        signinLinearLayout.setVisibility(View.GONE);
        groupsRecyclerView.setVisibility(View.VISIBLE);
        loadGroups();
    }

    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    private void loadGroups() {

    }

}
