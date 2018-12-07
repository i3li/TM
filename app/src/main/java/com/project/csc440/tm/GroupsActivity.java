package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GroupsActivity extends TMFBActivity implements GroupsAdapter.GroupItemClickListener {

    // Constants
    /**
     * TAG's constants are used for debugging purposes.
     * We use them as the first parameters in Log.x methods to indicate the class name.
     */
    private static final String TAG = "GroupsActivity";
    /**
     * This constant is the request code that is used to start the sign in activity.
     * It can be any integer.
     * We use it in two places:
     *  1. When we start the sign in activity we pass as the request code in the 'startActivityForResults' method
     *  2. In the 'onActivityResult' method to check whether we are returning from the sing in activity or from another activity
     */
    private static final int RC_SIGN_IN = 1;
    private static final int RC_CREATE_GROUP = 2;


    // Views
    private DrawerLayout drawerLayout;
    /* ----- Before signing in ----- */
    private LinearLayout signinLinearLayout;
    private TextView signinErrorTextView;
    private Button signinButton;
    /* -----                   ----- */

    /* ----- After signing in ----- */
    private RecyclerView groupsRecyclerView;
    private ProgressBar groupsProgressBar;
    private FloatingActionButton addGroupButton;

    private NavigationView navView;
    private TextView userDisplayNameTextView;
    private TextView userEmailTextView;
    private TextView noGroupsTextView;
    /* -----                  ----- */

    private GroupsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        if (getCurrentUser() == null) // User is not signed in
            setupViewsForSignIn(null);
        else // User is signed in
            setupViewsForGroups();
    }

    @Override
    int getLayoutResource() {
        return R.layout.activity_groups;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                updateCurrentUserProfile(); // After user is signed up, their profile should be added to the database
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
        } else if (requestCode == RC_CREATE_GROUP) {
            if (resultCode == RESULT_OK)
                createGroup(data.getStringExtra(CreateGroupActivity.GROUP_NAME_KEY),data.getStringExtra(CreateGroupActivity.GROUP_DESC_KEY));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (adapter != null)
//            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (adapter != null)
//            adapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onGroupItemClick(String groupKey, String groupName) {
        Intent intent = new Intent(this, TasksActivity.class);
        intent.putExtra(TasksActivity.GROUP_KEY_KEY, groupKey);
        intent.putExtra(TasksActivity.GROUP_NAME_KEY, groupName);
        startActivity(intent);
    }


    /**
     * A helper method that initializes all UI properties.
     */
    private void setupViews() {
        // Toolbar setup
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        drawerLayout = findViewById(R.id.dl_groups);
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
        groupsProgressBar = findViewById(R.id.pb_groups);
        addGroupButton = findViewById(R.id.fab_add_group);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateGroupActivity();
            }
        });
        noGroupsTextView = findViewById(R.id.tv_no_groups);
        navView = findViewById(R.id.nav_view_groups);
        View headerView = navView.getHeaderView(0);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_sign_out:
                        signOut();
                        break;
                        // TODO: Implement Edit Profile
                }
                return true;
            }
        });
        userDisplayNameTextView = headerView.findViewById(R.id.tv_user_display_name);
        userEmailTextView = headerView.findViewById(R.id.tv_user_email);
    }

    /**
     * A helper method that hides views that are only for signed in users, and displays views for anonymous users.
     * @param errorMessage The message that needs to be displayed to the user.
     *                     If set to null, then the default message is displayed -which asks the user to sign in-
     *                     instead of the error message.
     */
    private void setupViewsForSignIn(String errorMessage) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (adapter != null)
            adapter.stopListening();
        groupsRecyclerView.setVisibility(View.GONE);
        groupsProgressBar.setVisibility(View.GONE);
        addGroupButton.hide();
        noGroupsTextView.setVisibility(View.GONE);
        signinLinearLayout.setVisibility(View.VISIBLE);
        String text = errorMessage == null ? getString(R.string.sign_in_message) : errorMessage;
        signinErrorTextView.setText(text);
    }

    /**
     * A helper method that displays views that are only for signed in users, and hides views for anonymous users.
     * After that, this method loads all groups that the user is a member in.
     */
    private void setupViewsForGroups() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        loadDrawer();
        signinLinearLayout.setVisibility(View.GONE);
//        groupsRecyclerView.setVisibility(View.VISIBLE);
        groupsProgressBar.setVisibility(View.VISIBLE);
        addGroupButton.show();
        loadGroups();
    }

    /**
     * A helper method that displays the no groups text view, and hides the list of groups.
     */
    private void setupViewsForEmptyList() {
//        tasksRecyclerView.setVisibility(View.GONE);
        groupsProgressBar.setVisibility(View.GONE);
        noGroupsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * A helper method that hides the no groups text view, and displays the list of groups.
     */
    private void setupViewsForNonEmptyList() {
        groupsRecyclerView.setVisibility(View.VISIBLE);
        noGroupsTextView.setVisibility(View.GONE);
        groupsProgressBar.setVisibility(View.GONE);
    }

    /**
     * A helper method that displays the sign in wizard.
     */
    private void signIn() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
    }

    /**
     * A helper method that signs the signed in user out.
     */
    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setupViewsForSignIn(null);
            }
        });
    }

    /**
     * A helper method for loading groups into the recycler view.
     */
    private void loadGroups() {
        String currentUserId = getCurrentUser().getUid();
        final DatabaseReference groupsRef = databaseRef.child(DBConstants.groupsPath);

        // Query for groups the user is a member in
        final Query userGroupsQuery = databaseRef.child(DBConstants.userGroupsPath).child(currentUserId).child(DBConstants.userGroupsGroupsKey);

        // Check if there is at least one group
        userGroupsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0)
                    GroupsActivity.this.setupViewsForEmptyList();

                FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>().setIndexedQuery(userGroupsQuery, groupsRef, Group.class).build();
                adapter = new GroupsAdapter(options, GroupsActivity.this);
                adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        GroupsActivity.this.setupViewsForNonEmptyList();
                        adapter.unregisterAdapterDataObserver(this);
                    }
                });
                groupsRecyclerView.setAdapter(adapter);
                groupsRecyclerView.setLayoutManager(new LinearLayoutManager(GroupsActivity.this));
                adapter.startListening();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * A helper method for loading user info into the drawer.
     */
    private void loadDrawer() {
        FirebaseUser user = getCurrentUser();
        userDisplayNameTextView.setText(user.getDisplayName());
        userEmailTextView.setText(user.getEmail());
    }

    /**
     * A helper method for handling 'add group' event.
     */
    private void startCreateGroupActivity() {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivityForResult(intent, RC_CREATE_GROUP);
    }

    private void createGroup(String name, String desc) {
        /* Three places for adding groups
        1. groups/
        2. user_groups/current_user_id/groups
        3. group_users/new_group_id/users/current_user
         */

        FirebaseUser user = getCurrentUser();

        Group newGroup = new Group(name, desc, user.getUid());

        DatabaseReference groupsRef = databaseRef.child(DBConstants.groupsPath);
        String newGroupKey = groupsRef.push().getKey();

        // Paths
        String groupsPath = DBConstants.groupsPath + "/" + newGroupKey;
        String userGroupsPath = DBConstants.userGroupsPath + "/" + user.getUid() + "/" + DBConstants.userGroupsGroupsKey + "/" + newGroupKey;
        String groupUsersPath = DBConstants.groupUsersPath + "/" + newGroupKey + "/" + DBConstants.groupUsersUsersKey + "/" + user.getUid();

        // To push in all places atomically
        Map<String, Object> allInserts = new HashMap<>();
        allInserts.put(groupsPath, newGroup);
        allInserts.put(userGroupsPath, true);
        allInserts.put(groupUsersPath, true);

        databaseRef.updateChildren(allInserts, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null)
                    // There is an error
                    handleDatabaseError(databaseError);
                else
                    handleSuccessfullOperation(getString(R.string.success_group_creation_message));
            }
        });
    }

    private void handleSuccessfullOperation(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateCurrentUserProfile() {
        FirebaseUser user = getCurrentUser();
        String usersPath = DBConstants.usersPath + "/" + user.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put(usersPath, new UserProfile(user.getDisplayName(), user.getEmail()));
        databaseRef.updateChildren(updates);
    }

}