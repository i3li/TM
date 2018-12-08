package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewTaskActivity extends InGroupActivity {

    private static final String TAG = "ViewTaskActivity";

    public static final String TASK_KEY_KEY = "_task_key_";
    public static final String TASK_NAME_KEY = "_task_name_";

    private static final int RC_SELECT_MEMBER = 1;

    private String groupKey;
    private String taskKey;
    private Task task;

    private DatabaseReference taskRef;

    private ValueEventListener taskListener;

    @Override
    int getLayoutResource() {
        return R.layout.activity_view_task;
    }

    private TextView dueDateTextView, assignedToTextView, detailsTextView;
    private LinearLayout detailsLinearLayout;
    private Button assignButton, accomplishButton;
    private MenuItem deleteMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        groupKey = getGroupKey();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_task, menu);
        deleteMenuItem = menu.findItem(R.id.menu_delete);
        if (task != null) {
            Log.i(TAG, "onCreateOptionsMenu: Task not null");
            deleteMenuItem.setVisible(getCurrentUser().getUid().equals(task.getOwner()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                VerificationDialogFragment.getInstance(getString(R.string.delete_task_verification), getString(R.string.yes), getString(R.string.no), new VerificationDialogFragment.VerificationDialogFragmentListener() {
                    @Override
                    public void onYes() {
                        deleteTask();
                    }

                }).show(getSupportFragmentManager(), VerificationDialogFragment.class.getName());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SELECT_MEMBER) {
            if (resultCode == RESULT_OK) {
                final String memberId = data.getStringExtra(SelectMemberActivity.MEMBER_KEY_KEY);
                final String memberName = data.getStringExtra(SelectMemberActivity.MEMBER_NAME_KEY);
                Log.i(TAG, "onActivityResult: The selected member is: " + memberId + ":" + memberName);
                // Add the member
                taskRef.child(DBConstants.taskAssigneeKey).setValue(memberId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null)
                            handleDatabaseError(databaseError);
                        else
                            Toast.makeText(ViewTaskActivity.this, memberId.equals(getCurrentUser().getUid()) ? getString(R.string.success_current_member_assignment) : getString(R.string.success_member_assignment, memberName), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void setupViews() {
        dueDateTextView = findViewById(R.id.tv_task_view_due_date);
        assignedToTextView = findViewById(R.id.tv_task_view_assigned_to_member_name);
        detailsTextView = findViewById(R.id.tv_task_view_details);
        detailsLinearLayout = findViewById(R.id.ll_task_view_details);
        assignButton = findViewById(R.id.btn_assign);
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewTaskActivity.this, SelectMemberActivity.class);
                intent.putExtra(SelectMemberActivity.GROUP_KEY_KEY, groupKey);
                startActivityForResult(intent, RC_SELECT_MEMBER);
            }
        });
        accomplishButton = findViewById(R.id.btn_accomplish);
        accomplishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskRef.child(DBConstants.taskAccomplishedKey).setValue(true, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null)
                            handleDatabaseError(databaseError);
                        else {
                            accomplishButton.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private  String formatDate(Date date) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, d MMMM yyy hh:mm a");
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
        return  dateFormat.format(date);
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

        if (task.getAssignee() == null || !task.getAssignee().equals(getCurrentUser().getUid()))
            accomplishButton.setVisibility(View.GONE);
        else
            accomplishButton.setVisibility(View.VISIBLE);

        if (task.getAssignee() == null) {
            assignedToTextView.setText(getString(R.string.not_assigned_yet));
            if (task.getOwner().equals(getCurrentUser().getUid()))
                assignButton.setVisibility(View.VISIBLE);
        } else {
            assignButton.setVisibility(View.GONE);
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
                if (dataSnapshot.exists()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    updateFieldsWithTask(task);
                } else
                    finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void deleteTask() {
        /* Two places for deleting tasks
        1. /tasks/
        2. group_tasks/current_group_id/current_task_id
         */
        String taskPath = DBConstants.tasksPath + "/" + taskKey;
        String groupTasksPath = DBConstants.groupTasksPath + "/" + groupKey + "/" + DBConstants.groupTasksTasksKey + "/" + taskKey;

        Map<String, Object> map = new HashMap<>();
        map.put(taskPath, null);
        map.put(groupTasksPath, null);

        databaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(ViewTaskActivity.this, R.string.success_task_deletion_message, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
