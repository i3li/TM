package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class CreateTaskActivity extends TMActivity {

    private static final String TAG = "CreateTaskActivity";

    private static final int MAX_LENGTH_TASK_NAME = 50;

    public static final String TASK_NAME_KEY = "_task_name_";
    public static final String TASK_DETAILS_KEY = "_group_details_";
    public static final String TASK_DUE_DATE_KEY = "_task_due_date_";

    private EditText nameEditText, detailsEditText;
    private SingleDateAndTimePicker dateAndTimePicker;

    /**
     * Thi timer is for updating the min time of the due date picker
     */
    private Timer timer;

    @Override
    int getLayoutResource() {
        return R.layout.activity_create_task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        setupTimer();
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

    private void updateMinDateForDueDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        Date afterMinute = cal.getTime();
        dateAndTimePicker.setMinDate(afterMinute);
    }

    private void setupDueDatePicker() {
        dateAndTimePicker = findViewById(R.id.sdtp_due_date);
        updateMinDateForDueDatePicker();
        dateAndTimePicker.setDefaultDate(dateAndTimePicker.getMinDate());
        dateAndTimePicker.setTextColor(R.color.colorPrimaryDark);
        dateAndTimePicker.setStepMinutes(1);
    }

    private void setupViews() {
        setupDueDatePicker();
        nameEditText = findViewById(R.id.et_task_name);
        detailsEditText = findViewById(R.id.et_task_details);
    }

    private void setupTimer() {
        timer = new Timer();
        final Calendar cal = Calendar.getInstance();
        int remainingMillisUntilNextSec = 1000 - cal.get(Calendar.MILLISECOND);
        int remainingSecsUntilNextMin = 60 - cal.get(Calendar.SECOND) + 1;
        int remainingMillisUntilNextMin = remainingMillisUntilNextSec + remainingSecsUntilNextMin * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateMinDateForDueDatePicker();
            }
        }, remainingMillisUntilNextMin, 1000*60);
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
            Date dueDate = dateAndTimePicker.getDate();
            Intent intent = new Intent();
            intent.putExtra(TASK_NAME_KEY, nameEditText.getText().toString().trim());
            intent.putExtra(TASK_DETAILS_KEY, detailsEditText.getText().toString().trim());
            intent.putExtra(TASK_DUE_DATE_KEY, dueDate);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
