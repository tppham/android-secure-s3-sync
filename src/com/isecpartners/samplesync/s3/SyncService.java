package com.isecpartners.samplesync.s3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Glue that lets android find and use our sync adapter.
 * A lot of boilerplate for such a simple purpose...
 */
public class SyncService extends Service {
    private static final String TAG = "s3.SyncService";
    private static final Object sLock = new Object();
    private static SyncAdapter sAdapter = null;

    @Override
    public void onCreate() {
        Log.v(TAG, "Sync Service started");
        synchronized (sLock) {
            if (sAdapter == null)
                sAdapter = new SyncAdapter(this);
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Sync Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sAdapter.getSyncAdapterBinder();
    }
}
