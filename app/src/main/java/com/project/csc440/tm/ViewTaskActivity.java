package com.project.csc440.tm;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewTaskActivity extends TMFBActivity {

    @Override
    int getLayoutResource() {
        return R.layout.activity_view_task;
    }

    private TextView dueDateTextView, assignedToTextView, detailsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
