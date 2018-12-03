package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.Date;

public class CreateTaskActivity extends TMActivity {

    private static final int MAX_LENGTH_TASK_NAME = 50;

    public static final String TASK_NAME_KEY = "_task_name_";
    public static final String TASK_DETAILS_KEY = "_group_details_";
    public static final String TASK_DUE_DATE_KEY = "_task_due_date_";

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create_task:
                onCreateTaskClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        dateAndTimePicker = findViewById(R.id.sdtp_due_date);
        Date now = new Date();
        dateAndTimePicker.setDefaultDate(now);
        dateAndTimePicker.setMinDate(now);
        dateAndTimePicker.setTextColor(R.color.colorPrimaryDark);

        nameEditText = findViewById(R.id.et_task_name);
        detailsEditText = findViewById(R.id.et_task_details);
    }

    private boolean validateFields() {
        String name = nameEditText.getText().toString();
        String details = detailsEditText.getText().toString();
        Date dueDate = dateAndTimePicker.getDate();
        Boolean flag = true;
        if (name.trim().length() == 0) {
            nameEditText.setError(getString(R.string.empty_filed_error));
            flag = false;
        } else if (name.trim().length() > MAX_LENGTH_TASK_NAME) {
            nameEditText.setError(getString(R.string.max_char_limit_error) + " " + MAX_LENGTH_TASK_NAME);
            flag = false;
        }
        if (dueDate.before(new Date())) {
            Toast.makeText(this, getString(R.string.past_due_date_error), Toast.LENGTH_SHORT).show();
            flag = false;
        }
        return flag;
    }

    private void onCreateTaskClick() {
        if (validateFields()) {
            long dueDate = dateAndTimePicker.getDate().getTime();
            Intent intent = new Intent();
            intent.putExtra(TASK_NAME_KEY, nameEditText.getText().toString().trim());
            intent.putExtra(TASK_DETAILS_KEY, detailsEditText.getText().toString().trim());
            intent.putExtra(TASK_DUE_DATE_KEY, dueDate);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
