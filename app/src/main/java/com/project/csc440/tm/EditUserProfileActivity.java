package com.project.csc440.tm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditUserProfileActivity extends TMActivity {

    private static final String TAG = "EditUserProfileActivity";

    public static final String USERNAME = "_username_";

    @Override
    int getLayoutResource() {
        return R.layout.activity_edit_user_profile;
    }

    private EditText usernameEditText;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(USERNAME))
            username = getIntent().getStringExtra(USERNAME);
        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Intent intent = new Intent();
                intent.putExtra(USERNAME, usernameEditText.getText());
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        usernameEditText = findViewById(R.id.et_user_name);
        if (username != null)
            usernameEditText.setText(username);
    }

}
