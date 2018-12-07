package com.project.csc440.tm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public abstract class TMActivity extends AppCompatActivity {

    private static final String TAG = "TMActivity";

    @LayoutRes abstract int getLayoutResource();

    private Toolbar toolbar;

    private Timer connectionCheckerTimer;
    private Timer disconnectionThreeDotTimer;

    private String origTitle;
    private String waitingDots = "";
    private boolean isConnected = false;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        origTitle = getTitle().toString();
        toolbar = findViewById(R.id.tb_main);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupConnectionCheckerTimer();
        isConnected = isConnected();
        if (!isConnected)
            onDisconnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionCheckerTimer.cancel();
        connectionCheckerTimer = null;
        if (disconnectionThreeDotTimer != null) {
            disconnectionThreeDotTimer.cancel();
            disconnectionThreeDotTimer = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void setupConnectionCheckerTimer() {
        connectionCheckerTimer = new Timer();
        connectionCheckerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean _connected = isConnected();
                if (_connected != isConnected) {
                    isConnected = _connected;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isConnected)
                                onConnection();
                            else
                                onDisconnection();
                        }
                    });
                }
            }
        }, 0, 5 * 1000);
    }

    private void setupDisconnectionThreeDotTimer() {
        disconnectionThreeDotTimer = new Timer();
        disconnectionThreeDotTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String newTitle = getString(R.string.no_connection_tilte);
                        if (waitingDots.length() == 3)
                            waitingDots = "";
                        else
                            waitingDots += ".";
                        newTitle += waitingDots;
                        setTitle(newTitle);
                    }
                });
            }
        }, 0, 1 * 1000);
    }

    protected void onConnection() {
        setTitle(origTitle);
        if (disconnectionThreeDotTimer != null) {
            disconnectionThreeDotTimer.cancel();
            disconnectionThreeDotTimer = null;
        }
    }

    protected void onDisconnection() {
        setTitle(R.string.no_connection_tilte);
        if (disconnectionThreeDotTimer == null)
            setupDisconnectionThreeDotTimer();
    }

}
