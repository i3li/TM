package com.project.csc440.tm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

    // Views
    /* ------- Empty list of tasks ------- */
    private TextView noTasksTextView;
    /* -----                         ----- */

    /* ----- Not empty list of tasks ----- */
    private RecyclerView tasksRecyclerView;
    /* -----                         ----- */

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private TasksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
    }

    /**
     * A helper method that initializes all UI properties.
     */
    private void setupViews() {
        noTasksTextView = findViewById(R.id.tv_no_tasks);
        tasksRecyclerView = findViewById(R.id.rv_tasks);
    }

    /**
     * A helper method that displays the no tasks text view, and hides the list of tasks.
     */
    private void setupViewsForEmptyList() {
        tasksRecyclerView.setVisibility(View.GONE);
        noTasksTextView.setVisibility(View.VISIBLE);
    }

    /**
     * A helper method that hides the no tasks text view, and displays the list of tasks.
     * After that, this method loads all tasks that are in the group.
     */
    private void setupViewsForTasks() {
        noTasksTextView.setVisibility(View.GONE);
        tasksRecyclerView.setVisibility(View.VISIBLE);
        loadTasks();
    }

    /**
     * A helper method for loading tasks into the recycler view.
     */
    private void loadTasks() {
        // Query for tasks that in the group

    }

}
