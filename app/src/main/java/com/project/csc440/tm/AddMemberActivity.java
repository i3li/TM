package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.regex.Pattern;

public class AddMemberActivity extends TMActivity {

    private static final String TAG = "AddMemberActivity";

    private final static String EMAIL_REGEX = "^[a-zA-Z]([a-zA-Z0-9_.+-]?)+@[a-zA-Z]([a-zA-Z0-9-]?)+\\.[a-zA-Z]([a-zA-Z0-9-.]?)+$";

    public static final String MEMBER_EMAIL_KEY = "_member_email_";

    private EditText emailEditText;

    @Override
    int getLayoutResource() {
        return R.layout.activity_add_member;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_member:
                onAddMemberClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        emailEditText = findViewById(R.id.et_member_email);
    }

    private static boolean isValiedEmailAddress(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }

    private boolean validateFields() {
        String email = emailEditText.getText().toString();
        Boolean flag = true;
        if (email.trim().length() == 0) {
            emailEditText.setError(getString(R.string.empty_filed_error));
            flag = false;
        } else if (!isValiedEmailAddress(email.trim())) {
            emailEditText.setError(getString(R.string.invalid_email));
            flag = false;
        }
        return flag;
    }

    private void onAddMemberClick() {
        if (validateFields()) {
            Intent intent = new Intent();
            intent.putExtra(MEMBER_EMAIL_KEY, emailEditText.getText().toString().trim());
            setResult(RESULT_OK, intent);
            finish();
        }
    }



}
