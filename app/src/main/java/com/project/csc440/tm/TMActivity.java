package com.project.csc440.tm;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class TMActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    protected void setupToolbar(@IdRes int id) {
        toolbar = findViewById(id);
        setSupportActionBar(toolbar);
    }

}
