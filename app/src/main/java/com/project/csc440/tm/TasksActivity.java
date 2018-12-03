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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TasksActivity extends TMActivity {

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

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private TasksAdapter adapter;

    /**
     * The key for the current group.
     */
    private String groupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        Intent intent = getIntent();
        if (intent.hasExtra(GROUP_KEY_KEY)) {
            if (intent.hasExtra(GROUP_NAME_KEY))
                setTitle(intent.getStringExtra(GROUP_NAME_KEY));
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
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
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
        addTaskButton.hide();
        noTasksTextView.setVisibility(View.VISIBLE);
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
        // Query for tasks that in the group
        Query TasksQuery = database.getReference().child(DBConstants.groupTasksPath).child(groupKey).child(DBConstants.groupTasksTasksKey);
        DatabaseReference tasksRef = database.getReference().child(DBConstants.tasksPath);
        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>().setIndexedQuery(TasksQuery, tasksRef, Task.class).build();
        adapter = new TasksAdapter(options);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                tasksProgressBar.setVisibility(View.GONE);
                adapter.unregisterAdapterDataObserver(this);
            }
        });
        tasksRecyclerView.setAdapter(adapter);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
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

        Task newTask = new Task(name, details, dueDate);

        DatabaseReference tasksRef = database.getReference().child(DBConstants.tasksPath);
        String newTaskKey = tasksRef.push().getKey();

        // Paths
        String tasksPath = DBConstants.tasksPath + "/" + newTaskKey;
        String groupTasksPath = DBConstants.groupTasksPath + "/" + groupKey + "/" + DBConstants.groupTasksTasksKey + "/" + newTaskKey;

        // To push in all places atomically
        Map<String, Object> allInserts = new HashMap<>();
        allInserts.put(tasksPath, newTask);
        allInserts.put(groupTasksPath, true);

        database.getReference().updateChildren(allInserts, new DatabaseReference.CompletionListener() {
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

    private void handleDatabaseError(DatabaseError error) {
        int code = error.getCode();
        @StringRes int userErrorMessageId = R.string.general_error;
        switch (code) {
            case DatabaseError.DISCONNECTED:
            case DatabaseError.NETWORK_ERROR:
                userErrorMessageId = R.string.connection_error;
        }
        String userErrorMessage = getString(userErrorMessageId);
        Toast.makeText(this, userErrorMessage, Toast.LENGTH_LONG).show();
    }

}
