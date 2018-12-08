package com.project.csc440.tm;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class ViewGroupActivity extends TMFBActivity {

    private static final String TAG = "ViewGroupActivity";

    private static final int RC_ADD_MEMBER = 1;

    public static final String GROUP_KEY_KEY = "_group_key_";
    public static final String GROUP_NAME_KEY = "_group_name_";

    private ScrollView descScrollView;
    private TextView descTextView;
    private FloatingActionButton addMemberButton;
    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;

    private MembersAdapter adapter;

    private DatabaseReference groupRef;

    private String groupKey;
    private Group group;

    private ValueEventListener groupListener;

    @Override
    int getLayoutResource() {
        return R.layout.activity_view_group;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        groupKey = intent.getStringExtra(GROUP_KEY_KEY);
        setTitle(intent.getStringExtra(GROUP_NAME_KEY));
        groupRef = databaseRef.child(DBConstants.groupsPath).child(groupKey);
        setupViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (groupListener == null)
            loadGroupInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (groupListener != null) {
            groupRef.removeEventListener(groupListener);
            groupListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit:
                // TODO: imp
                Log.i(TAG, "onOptionsItemSelected: Exit");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_MEMBER)
            if (resultCode == RESULT_OK)
                addMember(data.getStringExtra(AddMemberActivity.MEMBER_EMAIL_KEY));
    }

    private void setupViews() {
        descScrollView = findViewById(R.id.sv_group_desc);
        descTextView = findViewById(R.id.tv_group_view_desc);
        addMemberButton = findViewById(R.id.fab_add_member);
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddMemberActivity();
            }
        });
        membersRecyclerView = findViewById(R.id.rv_members);
        progressBar = findViewById(R.id.pb_members);
        loadGroupInfo();
    }

    private void updateFieldsWithGroup(Group group) {
        this.group = group;
        setTitle(group.getName());
        if (group.getDescription() == null || group.getDescription().isEmpty())
            descScrollView.setVisibility(View.GONE);
        else {
            descScrollView.setVisibility(View.VISIBLE);
            descTextView.setText(group.getDescription());
        }
        if (group.getAdmin().equals(getCurrentUser().getUid()))
            addMemberButton.show();
        else
            addMemberButton.hide();

        // Query for group members
        Query groupMembersQuery = databaseRef.child(DBConstants.groupUsersPath).child(groupKey).child(DBConstants.groupUsersUsersKey);
        DatabaseReference usersRef = databaseRef.child(DBConstants.usersPath);
        FirebaseRecyclerOptions<UserProfile> options = new FirebaseRecyclerOptions.Builder<UserProfile>().setIndexedQuery(groupMembersQuery, usersRef, UserProfile.class).build();
        if (adapter != null)
            adapter.stopListening();
        adapter = new MembersAdapter(options, null, group.getAdmin(), group.getAdmin().equals(getCurrentUser().getUid()));
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                progressBar.setVisibility(View.GONE);
                adapter.unregisterAdapterDataObserver(this);
            }
        });
        membersRecyclerView.setAdapter(adapter);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();

    }

    private void loadGroupInfo() {
        groupListener = groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                updateFieldsWithGroup(group);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void startAddMemberActivity() {
        Intent intent = new Intent(this, AddMemberActivity.class);
        startActivityForResult(intent, RC_ADD_MEMBER);
    }

    private void addMember(final String email) {
        /* Two places for adding members
        1. group_users/group_key/users
        2. user_groups/new_member_key/groups
         */

        // First check if member exists
        final DatabaseReference usersRef = databaseRef.child(DBConstants.usersPath);
        usersRef.orderByChild(DBConstants.usersEmailKey).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 1) {
                    // Member exists
                    DataSnapshot memberDataSnapshot = dataSnapshot.getChildren().iterator().next();
                    final String memberKey = memberDataSnapshot.getKey();
                    final UserProfile member = memberDataSnapshot.getValue(UserProfile.class);
                    // Check if the user is not adding themselves
                    if (getCurrentUser().getUid().equals(memberKey))
                        Toast.makeText(ViewGroupActivity.this, getString(R.string.user_add_themselves), Toast.LENGTH_LONG).show();
                    else {
                        // Check if the user is already a member on the group
                        databaseRef.child(DBConstants.userGroupsPath).child(memberKey).child(DBConstants.userGroupsGroupsKey).orderByKey().equalTo(groupKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    // User is already a member on the group
                                    Toast.makeText(ViewGroupActivity.this, member.getEmail() + " " + getString(R.string.member_already_on_the_group), Toast.LENGTH_LONG).show();
                                } else {
                                    // Add member to the group
                                    String groupUsersPath = DBConstants.groupUsersPath+ "/" + groupKey + "/" + DBConstants.groupUsersUsersKey + "/" + memberKey;
                                    String userGroupsPath = DBConstants.userGroupsPath + "/" + memberKey + "/" + DBConstants.userGroupsGroupsKey + "/" + groupKey;

                                    Map<String, Object> allUpdates = new HashMap<>();
                                    allUpdates.put(groupUsersPath, true);
                                    allUpdates.put(userGroupsPath, true);

                                    databaseRef.updateChildren(allUpdates, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null)
                                                handleDatabaseError(databaseError);
                                            else
                                                Toast.makeText(ViewGroupActivity.this, member.getName() + " " + getString(R.string.success_member_addition_message), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                handleDatabaseError(databaseError);
                            }
                        });
                    }
                } else {
                    // Member does not exist
                    Toast.makeText(ViewGroupActivity.this, getString(R.string.no_user_with_email) + " " + email, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
        
    }

}
