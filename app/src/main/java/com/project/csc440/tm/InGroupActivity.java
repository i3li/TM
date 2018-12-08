package com.project.csc440.tm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public abstract class InGroupActivity extends TMFBActivity {

    private static final String TAG = "InGroupActivity";

    public static final String GROUP_KEY_KEY = "_group_key_";

    private DatabaseReference membershipRef;
    private ValueEventListener membershipListener;

    private String groupKey;

    public String getGroupKey() { return groupKey; }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupKey = getIntent().getStringExtra(GROUP_KEY_KEY);
        Log.i(TAG, "onCreate: GroupKey: " + groupKey);
        membershipRef = databaseRef.child(DBConstants.groupUsersPath).child(groupKey).child(DBConstants.groupUsersUsersKey).child(getCurrentUser().getUid());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (membershipListener == null)
            listenToMembership();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        if (membershipListener != null) {
            membershipRef.removeEventListener(membershipListener);
            membershipListener = null;
        }
    }

    private void listenToMembership() {
        membershipListener = membershipRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.exists());
                if (!dataSnapshot.exists()) // Current user got deleted from the group
                    finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

}
