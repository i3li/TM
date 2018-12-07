package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class ViewGroupActivity extends TMActivity implements MembersAdapter.MemberItemClickListener {

    private static final String TAG = "ViewGroupActivity";

    private static final int RC_ADD_MEMBER = 1;

    public static final String GROUP_KEY_KEY = "_group_key_";
    public static final String GROUP_NAME_KEY = "_group_name_";

    private TextView descTextView;
    private FloatingActionButton addMemberButton;
    private RecyclerView membersRecyclerView;

    private MembersAdapter adapter;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
        groupRef = database.getReference().child(DBConstants.groupsPath).child(groupKey);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_MEMBER)
            if (resultCode == RESULT_OK)
                addMember(data.getStringExtra(AddMemberActivity.MEMBER_EMAIL_KEY));
    }

    private void setupViews() {
        descTextView = findViewById(R.id.tv_group_view_desc);
        addMemberButton = findViewById(R.id.fab_add_member);
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddMemberActivity();
            }
        });
        membersRecyclerView = findViewById(R.id.rv_members);
        loadGroupInfo();
    }

    private void updateFieldsWithGroup(Group group) {
        this.group = group;
        setTitle(group.getName());
        descTextView.setText(group.getDescription());
        if (group.getAdmin().equals(user.getUid()))
            addMemberButton.show();
        else
            addMemberButton.hide();

        // Query for group members
        Query groupMembersQuery = database.getReference().child(DBConstants.groupUsersPath).child(groupKey).child(DBConstants.groupUsersUsersKey);
        DatabaseReference usersRef = database.getReference().child(DBConstants.usersPath);
        FirebaseRecyclerOptions<UserProfile> options = new FirebaseRecyclerOptions.Builder<UserProfile>().setIndexedQuery(groupMembersQuery, usersRef, UserProfile.class).build();
        if (adapter != null)
            adapter.stopListening();
        adapter = new MembersAdapter(options, this, group.getAdmin());
//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                memberProgressBar.setVisibility(View.GONE);
//                adapter.unregisterAdapterDataObserver(this);
//            }
//        });
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

            }
        });
    }

    @Override
    public void onMemberItemClick(String userId) {

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
        DatabaseReference usersRef = database.getReference().child(DBConstants.usersPath);
        usersRef.orderByChild(DBConstants.usersEmailKey).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile member = dataSnapshot.getValue(UserProfile.class);
                if (member == null)
                    Toast.makeText(ViewGroupActivity.this, getString(R.string.no_user_with_email) + " " + email, Toast.LENGTH_LONG).show();
                else {
                    // TODO: Add member to the group

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        Group newGroup = new Group(name, desc, user.getUid());
//
//        DatabaseReference groupsRef = database.getReference().child(DBConstants.groupsPath);
//        String newGroupKey = groupsRef.push().getKey();
//
//        // Paths
//        String groupsPath = DBConstants.groupsPath + "/" + newGroupKey;
//        String userGroupsPath = DBConstants.userGroupsPath + "/" + user.getUid() + "/" + DBConstants.userGroupsGroupsKey + "/" + newGroupKey;
//        String groupUsersPath = DBConstants.groupUsersPath + "/" + newGroupKey + "/" + DBConstants.groupUsersUsersKey + "/" + user.getUid();
//
//        // To push in all places atomically
//        Map<String, Object> allInserts = new HashMap<>();
//        allInserts.put(groupsPath, newGroup);
//        allInserts.put(userGroupsPath, true);
//        allInserts.put(groupUsersPath, true);
//
//        database.getReference().updateChildren(allInserts, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                if (databaseError != null)
//                    // There is an error
//                    handleDatabaseError(databaseError);
//                else
//                    handleSuccessfullOperation(getString(R.string.success_group_creation_message));
//            }
//        });
    }

}
