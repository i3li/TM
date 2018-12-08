package com.project.csc440.tm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public abstract class TMFBActivity extends TMActivity {

    private Timer connectionCheckerTimer;
    private Timer disconnectionThreeDotTimer;

    private String origTitle;
    private String waitingDots = "";
    private boolean isConnected = true; // The default is true just for changing the title
    private ConnectivityManager connectivityManager;

    protected DatabaseReference databaseRef;

    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        origTitle = getTitle().toString();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean _connected = isConnected();
        if (_connected != isConnected) {
            isConnected = _connected;
            if (isConnected)
                onConnection();
            else
                onDisconnection();
        } else if (!isConnected)
            setupDisconnectionThreeDotTimer();
        setupConnectionCheckerTimer();
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
        origTitle = getTitle().toString();
        setTitle(R.string.no_connection_tilte);
        if (disconnectionThreeDotTimer == null)
            setupDisconnectionThreeDotTimer();
    }

    protected void handleDatabaseError(DatabaseError error) {
        int code = error.getCode();
        @StringRes int userErrorMessageId = R.string.general_error;
        switch (code) {
            case DatabaseError.DISCONNECTED:
            case DatabaseError.NETWORK_ERROR:
                userErrorMessageId = R.string.connection_error;
        }
        String userErrorMessage = getString(userErrorMessageId);
        Toast.makeText(this, userErrorMessage, Toast.LENGTH_LONG).show();
    }

}
