package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewGroupActivity extends InGroupActivity implements MembersAdapter.MemberDeleteClickListener {

    private static final String TAG = "ViewGroupActivity";

    private static final int RC_ADD_MEMBER = 1;

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
        groupKey = getGroupKey();
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
                exitGroup();
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
        adapter = new MembersAdapter(options, null, group.getAdmin(), group.getAdmin().equals(getCurrentUser().getUid()), getCurrentUser().getUid().equals(group.getAdmin()) ? this : null);
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
        if (groupListener != null)
            Log.w(TAG, "loadGroupInfo: MULTIPLE LISTENERS");
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
                                    Toast.makeText(ViewGroupActivity.this, getString(R.string.member_already_on_the_group, member.getEmail()), Toast.LENGTH_LONG).show();
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
                                                Toast.makeText(ViewGroupActivity.this, getString(R.string.success_member_addition_message, member.getName()), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewGroupActivity.this, getString(R.string.no_user_with_email, email), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
        
    }

    @Override
    public void onMemberDeleteClick(final String userId, final String username) {
        VerificationDialogFragment.getInstance(getString(R.string.delete_member_verification, username), getString(R.string.yes), getString(R.string.no), new VerificationDialogFragment.VerificationDialogFragmentListener() {
            @Override
            public void onYes() {
                deleteMember(userId, getString(R.string.success_deleting_member_message, username));
            }

        }).show(getSupportFragmentManager(), VerificationDialogFragment.class.getName());
    }

    private void update(Map<String, Object> map, final String message) {
        Log.i(TAG, "update: Map: " + map);
        databaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null)
                    handleDatabaseError(databaseError);
                else
                    Toast.makeText(ViewGroupActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMember(final String userId, final String message) {
        /* Pathes for exiting groups
        1. group_users/group_key/users/current_user_id
        2. user_groups/current_user_id/groups/group_key
        Update admin if the current is an admin
        3. groups/group_key/admin/new_admin_key
        To get new_admin_key:
        4. group_users/group_key/users/ get first two users
        if there is only one member (the current admin) delete the whole group
        5. groups/group_key
        6. group_tasks/group_key
        7. delete all of the tasks assigned to the member in the group

        For lack of time, only 1,2,3,4 is implemented

         */

        String groupUsersPath = DBConstants.groupUsersPath+ "/" + groupKey + "/" + DBConstants.groupUsersUsersKey + "/" + userId;
        String userGroupsPath = DBConstants.userGroupsPath + "/" + userId + "/" + DBConstants.userGroupsGroupsKey + "/" + groupKey;

        final Map<String, Object> map = new HashMap<>();
        map.put(groupUsersPath, null);
        map.put(userGroupsPath, null);

        if (group.getAdmin().equals(userId)) {
            // Add paths to update the admin
            // First, check if there is any other member on the group
            databaseRef.child(DBConstants.groupUsersPath).child(groupKey).child(DBConstants.groupUsersUsersKey).limitToFirst(2).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 2) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        DataSnapshot firstMemberDS = iterator.next();
                        String newAdmin = firstMemberDS.getKey();
                        Log.i(TAG, "onDataChange: The new admin is " + newAdmin);
                        if (newAdmin.equals(userId)) {
                            // Get the second one
                            newAdmin = iterator.next().getKey();
                            Log.i(TAG, "onDataChange: Never mind, the new admin is " + newAdmin);
                        }
                        map.put(DBConstants.groupsPath + "/" + groupKey + "/" + DBConstants.groupsAdminKey, newAdmin);
                        update(map, message);
                    } else { // only the admin on the group
                        update(map, message);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleDatabaseError(databaseError);
                }
            });
        } else {
            update(map, message);
        }
    }

    private void exitGroup() {
        VerificationDialogFragment.getInstance(getString(R.string.exit_group_verification), getString(R.string.yes), getString(R.string.no), new VerificationDialogFragment.VerificationDialogFragmentListener() {
            @Override
            public void onYes() {
                deleteMember(getCurrentUser().getUid(), getString(R.string.success_user_exiting_message));
            }

        }).show(getSupportFragmentManager(), VerificationDialogFragment.class.getName());
    }

}
