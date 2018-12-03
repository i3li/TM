package com.project.csc440.tm;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CreateGroupActivity extends TMActivity {

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
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
