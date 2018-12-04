package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ViewGroupActivity extends TMActivity implements MembersAdapter.MemberItemClickListener {

    private static final String TAG = "ViewGroupActivity";

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

    private void setupViews() {
        descTextView = findViewById(R.id.tv_group_view_desc);
        addMemberButton = findViewById(R.id.fab_add_member);
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Implementation
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
        // TODO: Implementation
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
}
