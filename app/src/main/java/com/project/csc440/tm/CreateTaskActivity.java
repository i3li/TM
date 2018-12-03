package com.project.csc440.tm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.Date;

public class CreateTaskActivity extends TMActivity {

    private EditText nameEditText, detailsEditText;
    private SingleDateAndTimePicker dateAndTimePicker;

    @Override
    int getLayoutResource() {
        return R.layout.activity_create_task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        dateAndTimePicker = findViewById(R.id.sdtp_due_date);
        Date now = new Date();
        dateAndTimePicker.setDefaultDate(now);
        dateAndTimePicker.setMinDate(now);
        nameEditText = findViewById(R.id.et_task_name);
        detailsEditText = findViewById(R.id.et_task_details);
    }

}
