package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class SelectMemberActivity extends TMFBActivity implements MembersAdapter.MemberItemClickListener {

    private static final String TAG = "SelectMemberActivity";

    public static final String GROUP_KEY_KEY = "_group_key_";
    public static final String MEMBER_KEY_KEY = "_member_key_";
    public static final String MEMBER_NAME_KEY = "_member_name_";

    @Override
    int getLayoutResource() {
        return R.layout.activity_select_member;
    }

    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;

    private MembersAdapter adapter;

    private String groupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        groupKey = intent.getStringExtra(GROUP_KEY_KEY);
        setupViews();
    }

    private void setupViews() {
        progressBar = findViewById(R.id.pb_select_member_members);
        membersRecyclerView = findViewById(R.id.rv_members);
        Query groupMembersQuery = databaseRef.child(DBConstants.groupUsersPath).child(groupKey).child(DBConstants.groupUsersUsersKey);
        DatabaseReference usersRef = databaseRef.child(DBConstants.usersPath);
        FirebaseRecyclerOptions<UserProfile> options = new FirebaseRecyclerOptions.Builder<UserProfile>().setIndexedQuery(groupMembersQuery, usersRef, UserProfile.class).build();
        if (adapter != null)
            adapter.stopListening();
        adapter = new MembersAdapter(options, this, null, false, null);
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

    @Override
    public void onMemberItemClick(String userId, String username) {
        Intent intent = new Intent();
        intent.putExtra(MEMBER_KEY_KEY, userId);
        intent.putExtra(MEMBER_NAME_KEY, username);
        setResult(RESULT_OK, intent);
        finish();
    }
}
