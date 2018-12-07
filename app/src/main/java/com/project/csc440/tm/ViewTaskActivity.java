package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewTaskActivity extends TMFBActivity {

    private static final String TAG = "ViewTaskActivity";

    public static final String TASK_KEY_KEY = "_task_key_";
    public static final String TASK_NAME_KEY = "_task_name_";

    private String taskKey;
    private Task task;

    private DatabaseReference taskRef;

    private ValueEventListener taskListener;

    @Override
    int getLayoutResource() {
        return R.layout.activity_view_task;
    }

    private TextView dueDateTextView, assignedToTextView, detailsTextView;
    private LinearLayout assignedToLinearLayout, detailsLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        taskKey = intent.getStringExtra(TASK_KEY_KEY);
        setTitle(intent.getStringExtra(TASK_NAME_KEY));
        taskRef = databaseRef.child(DBConstants.tasksPath).child(taskKey);
        setupViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (taskListener == null)
            loadTaskInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (taskListener != null) {
            taskRef.removeEventListener(taskListener);
            taskListener = null;
        }
    }

    private void setupViews() {
        dueDateTextView = findViewById(R.id.tv_task_view_due_date);
        assignedToTextView = findViewById(R.id.tv_task_view_assigned_to_member_name);
        detailsTextView = findViewById(R.id.tv_task_view_details);
        assignedToLinearLayout = findViewById(R.id.ll_task_view_assigned_to);
        detailsLinearLayout = findViewById(R.id.ll_task_view_details);
    }

    private  String formatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, d MMMM yyy hh:mm a");
        return  simpleDateFormat.format(date);
    }

    private void updateFieldsWithTask(Task task) {
        this.task = task;
        setTitle(task.getName());

        if (task.getDetails() == null || task.getDetails().isEmpty())
            detailsLinearLayout.setVisibility(View.GONE);
        else {
            detailsLinearLayout.setVisibility(View.VISIBLE);
            detailsTextView.setText(task.getDetails());
        }
        Date date = new Date(task.getDueDate());
        dueDateTextView.setText(formatDate(date));
        Date now = new Date();
        if (date.before(now))
            dueDateTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        else if ((date.getTime() - now.getTime()) <= Task.CLOSE_DUE_DATE_IN_DAYS*24*60*60*1000) {
            dueDateTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        if (task.getAssignee() == null)
            assignedToLinearLayout.setVisibility(View.GONE);
        else {
            assignedToLinearLayout.setVisibility(View.VISIBLE);
            databaseRef.child(DBConstants.usersPath).child(task.getAssignee()).child(DBConstants.usersNameKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        assignedToTextView.setText(dataSnapshot.getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handleDatabaseError(databaseError);
                }
            });
        }

    }

    private void loadTaskInfo() {
        taskListener = taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                updateFieldsWithTask(task);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }


}
