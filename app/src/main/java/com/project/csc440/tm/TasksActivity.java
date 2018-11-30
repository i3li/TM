package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class TasksActivity extends AppCompatActivity {

    // Constants
    /**
     * TAG's constants are used for debugging purposes.
     * We use them as the first parameters in Log.x methods to indicate the class name.
     */
    private static final String TAG = TasksActivity.class.getName();

    /**
     * These constant is used to pass data from the prev activity. It is used by the caller activity to pass the group key and group name.
     */
    public static final String GROUP_KEY_KEY = "_group_key_";
    public static final String GROUP_NAME_KEY = "_group_name_";

    // Views
    /* ------- Empty list of tasks ------- */
    private TextView noTasksTextView;
    /* -----                         ----- */

    /* ----- Not empty list of tasks ----- */
    private RecyclerView tasksRecyclerView;
    private ProgressBar tasksProgressBar;
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
        setContentView(R.layout.activity_tasks);
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

    /**
     * A helper method that initializes all UI properties.
     */
    private void setupViews() {
        noTasksTextView = findViewById(R.id.tv_no_tasks);
        tasksRecyclerView = findViewById(R.id.rv_tasks);
        tasksProgressBar = findViewById(R.id.pb_tasks);
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
     * After that, this method loads all tasks that are in the group.
     */
    private void setupViewsForTasks() {
        noTasksTextView.setVisibility(View.GONE);
        tasksRecyclerView.setVisibility(View.VISIBLE);
        tasksProgressBar.setVisibility(View.VISIBLE);
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

}
