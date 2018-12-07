package com.project.csc440.tm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TasksActivity extends TMFBActivity implements TasksAdapter.TaskItemClickListener {

    // Constants
    /**
     * TAG's constants are used for debugging purposes.
     * We use them as the first parameters in Log.x methods to indicate the class name.
     */
    private static final String TAG = "TasksActivity";
    private static final int RC_CREATE_TASK = 1;

    /**
     * These constant is used to pass data from the prev activity. It is used by the caller activity to pass the group key and group name.
     */
    public static final String GROUP_KEY_KEY = "_group_key_";
    public static final String GROUP_NAME_KEY = "_group_name_";

    // Views
    /* ------- Empty list of tasks ------- */
    private TextView noTasksTextView;
    /* -----                         ----- */

    /* ----- Non empty list of tasks ----- */
    private RecyclerView tasksRecyclerView;
    private ProgressBar tasksProgressBar;
    private FloatingActionButton addTaskButton;
    /* -----                         ----- */

    private TasksAdapter adapter;

    /**
     * The key for the current group.
     */
    private String groupKey;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        Intent intent = getIntent();
        if (intent.hasExtra(GROUP_KEY_KEY)) {
            if (intent.hasExtra(GROUP_NAME_KEY)) {
                setTitle(intent.getStringExtra(GROUP_NAME_KEY));
                groupName = intent.getStringExtra(GROUP_NAME_KEY);
            }
            groupKey = intent.getStringExtra(GROUP_KEY_KEY);
            setupViewsForTasks();
        } else
            Log.e(TAG, "The group key must be passed in.");
    }

    @Override
    int getLayoutResource() {
        return R.layout.activity_tasks;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CREATE_TASK) {
            if (resultCode == RESULT_OK)
                createTask(data.getStringExtra(CreateTaskActivity.TASK_NAME_KEY),
                        data.getStringExtra(CreateTaskActivity.TASK_DETAILS_KEY),
                        (Date) data.getSerializableExtra(CreateTaskActivity.TASK_DUE_DATE_KEY));
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_group_details:
                Intent intent = new Intent(this, ViewGroupActivity.class);
                intent.putExtra(ViewGroupActivity.GROUP_KEY_KEY, groupKey);
                intent.putExtra(ViewGroupActivity.GROUP_NAME_KEY, groupName);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A helper method that initializes all UI properties.
     */
    private void setupViews() {
        noTasksTextView = findViewById(R.id.tv_no_tasks);
        tasksRecyclerView = findViewById(R.id.rv_tasks);
        tasksProgressBar = findViewById(R.id.pb_tasks);
        addTaskButton = findViewById(R.id.fab_add_task);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreateTaskActivity();
            }
        });
    }

    /**
     * A helper method that displays the no tasks text view, and hides the list of tasks.
     */
    private void setupViewsForEmptyList() {
        tasksRecyclerView.setVisibility(View.GONE);
        tasksProgressBar.setVisibility(View.GONE);
        noTasksTextView.setVisibility(View.VISIBLE);
    }

    /**
     * A helper method that hides the no tasks text view, and displays the list of tasks.
     */
    private void setupViewsForNonEmptyList() {
        tasksRecyclerView.setVisibility(View.VISIBLE);
        noTasksTextView.setVisibility(View.GONE);
        tasksProgressBar.setVisibility(View.GONE);
    }

    /**
     * A helper method that hides the no tasks text view, and displays the list of tasks.
     * After that, this method loads all tasks that are in the group.
     */
    private void setupViewsForTasks() {
        noTasksTextView.setVisibility(View.GONE);
        tasksRecyclerView.setVisibility(View.VISIBLE);
        tasksProgressBar.setVisibility(View.VISIBLE);
        addTaskButton.show();
        loadTasks();
    }

    /**
     * A helper method for loading tasks into the recycler view.
     */
    private void loadTasks() {
        final DatabaseReference tasksRef = databaseRef.child(DBConstants.tasksPath);

        // Query for tasks that in the group
        final Query groupTasksQuery = databaseRef.child(DBConstants.groupTasksPath).child(groupKey).child(DBConstants.groupTasksTasksKey);

        groupTasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0)
                    TasksActivity.this.setupViewsForEmptyList();
                FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>().setIndexedQuery(groupTasksQuery, tasksRef, Task.class).build();
                adapter = new TasksAdapter(options, TasksActivity.this);
                adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        TasksActivity.this.setupViewsForNonEmptyList();
                        adapter.unregisterAdapterDataObserver(this);
                    }
                });
                tasksRecyclerView.setAdapter(adapter);
                tasksRecyclerView.setLayoutManager(new LinearLayoutManager(TasksActivity.this));
                adapter.startListening();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    /**
     * A helper method for handling 'create task' event.
     */
    private void startCreateTaskActivity() {
        Intent intent = new Intent(this, CreateTaskActivity.class);
        startActivityForResult(intent, RC_CREATE_TASK);
    }

    private void createTask(String name, String details, Date dueDate) {
        /* Two places for adding tasks
        1. /tasks/
        2. group_tasks/current_group_id/tasks
         */

        Task newTask = new Task(name, details, dueDate.getTime(), FirebaseAuth.getInstance().getCurrentUser().getUid());

        DatabaseReference tasksRef = databaseRef.child(DBConstants.tasksPath);
        String newTaskKey = tasksRef.push().getKey();

        // Paths
        String tasksPath = DBConstants.tasksPath + "/" + newTaskKey;
        String groupTasksPath = DBConstants.groupTasksPath + "/" + groupKey + "/" + DBConstants.groupTasksTasksKey + "/" + newTaskKey;

        // To push in all places atomically
        Map<String, Object> allInserts = new HashMap<>();
        allInserts.put(tasksPath, newTask);
        allInserts.put(groupTasksPath, true);

        databaseRef.updateChildren(allInserts, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null)
                    // There is an error
                    handleDatabaseError(databaseError);
                else
                    handleSuccessfullOperation(getString(R.string.success_task_creation_message));
            }
        });
    }

    private void handleSuccessfullOperation(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTaskItemClick(String taskKey, String taskName) {
        Intent intent = new Intent(this, ViewTaskActivity.class);
        intent.putExtra(ViewTaskActivity.TASK_KEY_KEY, taskKey);
        intent.putExtra(ViewTaskActivity.TASK_NAME_KEY, taskName);
        startActivity(intent);
    }
}
