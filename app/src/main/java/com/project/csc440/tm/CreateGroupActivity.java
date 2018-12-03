package com.project.csc440.tm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class CreateGroupActivity extends TMActivity {

    private static final int MAX_LENGTH_GROUP_NAME = 50;
    private static final int MAX_LENGTH_GROUP_DESC = 250;

    private EditText nameEditText;
    private EditText descEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
    }

    @Override
    int getLayoutResource() {
        return R.layout.activity_create_group;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create_group:
                // TODO: Implementation
                onCreateGroupClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        nameEditText = findViewById(R.id.et_group_name);
        descEditText = findViewById(R.id.et_group_desc);
    }

    private boolean validateTextFields() {
        String name = nameEditText.getText().toString();
        String desc = descEditText.getText().toString();
        Boolean flag = true;
        if (name.trim().length() == 0) {
            nameEditText.setError(getString(R.string.empty_filed_error));
            flag = false;
        } else if (name.trim().length() > MAX_LENGTH_GROUP_NAME) {
            nameEditText.setError(getString(R.string.max_char_limit_error) + " " + MAX_LENGTH_GROUP_NAME);
            flag = false;
        }
        if (desc.trim().length() == 0) {
            descEditText.setError(getString(R.string.max_char_limit_error) + " " + MAX_LENGTH_GROUP_DESC);
            flag = false;
        } else if (desc.trim().length() > MAX_LENGTH_GROUP_DESC) {
            descEditText.setError(getString(R.string.empty_filed_error));
            flag = false;
        }
        return flag;
    }

    private void onCreateGroupClick() {
        if (validateTextFields())
            finish();
    }

}
