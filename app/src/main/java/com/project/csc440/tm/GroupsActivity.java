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
    /**
     * TAG's constants are used for debugging purposes.
     * We use them as the first parameters in Log.x methods to indicate the class name.
     */
    private static final String TAG = GroupsActivity.class.getName();
    /**
     * This constant is the request code that is used to start the sign in activity.
     * It can be any integer.
     * We use it in two places:
     *  1. When we start the sign in activity we pass as the request code in the 'startActivityForResults' method
     *  2. In the 'onActivityResult' method to check whether we are returning from the sing in activity or from another activity
     */
    private static final int RC_SIGN_IN = 1;


    // Views
    /* ----- Before signing in ----- */
    private LinearLayout signinLinearLayout;
    private TextView signinErrorTextView;
    private Button signinButton;
    /* -----                   ----- */

    /* ----- After signing in ----- */
    private RecyclerView groupsRecyclerView;
    /* -----                  ----- */

    private  FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setupViews();
        if (auth.getCurrentUser() == null) // User is not signed in
            setupViewsForSignIn(null);
        else { // User is signed in
            user = auth.getCurrentUser();
            setupViewsForGroups();
        }
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
                if (response != null && response.getError() != null)
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                        messageId = R.string.connection_error;
                    else
                        messageId = R.string.general_error;
                setupViewsForSignIn(getString(messageId));
            }
        }
    }

    /**
     * A helper method that initializes all UI properties.
     */
    private void setupViews() {
        signinLinearLayout = findViewById(R.id.ll_sign_in);
        signinErrorTextView = findViewById(R.id.tv_sign_in_error);
        signinButton = findViewById(R.id.btn_sign_in);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        groupsRecyclerView = findViewById(R.id.rv_groups);
    }

    /**
     * A helper method that hides views that are only for signed in users, and displays views for anonymous users.
     * @param errorMessage The message that needs to be displayed to the user.
     *                     If set to null, then the default message is displayed -which asks the user to sign in-
     *                     instead of the error message.
     */
    private void setupViewsForSignIn(String errorMessage) {
        groupsRecyclerView.setVisibility(View.GONE);
        signinLinearLayout.setVisibility(View.VISIBLE);
        String text = errorMessage == null ? getString(R.string.sign_in_message) : errorMessage;
        signinErrorTextView.setText(text);
    }

    /**
     * A helper method that displays views that are only for signed in users, and hides views for anonymous users.
     * After that, this method loads all groups that the user is a member in.
     */
    private void setupViewsForGroups() {
        signinLinearLayout.setVisibility(View.GONE);
        groupsRecyclerView.setVisibility(View.VISIBLE);
        loadGroups();
    }

    /**
     * A helper method that displays the sign in wizard.
     */
    private void signIn() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
    }

    /**
     * A helper method for loading groups into the recycler view.
     */
    private void loadGroups() {
        // Query for groups the user is member in
        Query userGroups = database.getReference().child(DBConstants.usersPath).child(user.getUid()).child(DBConstants.userGroupsKey);
        
    }

}
