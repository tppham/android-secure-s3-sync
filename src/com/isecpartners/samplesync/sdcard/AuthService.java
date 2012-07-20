package com.isecpartners.samplesync.sdcard;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Glue that lets android find and use our authenticator.
 * A lot of boilerplate for such a simple purpose...
 */
public class AuthService extends Service {
    private static final String TAG = "sdcard.AuthService";
    private AuthAdapter mAuth;

    @Override
    public void onCreate() {
        Log.v(TAG, "started");
        mAuth = new AuthAdapter(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "stopped");
    }

    /* called by android to get a handle on our auth adapter... */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind, intent: " + intent);
        return mAuth.getIBinder();
    }
}
