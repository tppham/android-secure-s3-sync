package com.isecpartners.samplesync.sdcard;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.isecpartners.samplesync.AccountHelper;
import com.isecpartners.samplesync.GenericSync;
import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.FileStore;

/**
 * Android calls through this interface to request a sync.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "sdcard.SyncAdapter";
    private final Context mCtx;

    public SyncAdapter(Context context) {
        super(context, true);
        mCtx = context;
    }

    /* a synch is requested. */
    @Override
    public void onPerformSync(Account acct, Bundle extras, String authority, ContentProviderClient provider, SyncResult res) {
        AccountHelper h = new AccountHelper(mCtx, acct);
        String path = h.getAcctPref("path", null);
        Log.v(TAG, "sync with sdcard store: " + acct.name);
        IBlobStore store = new FileStore(path);
        new GenericSync(mCtx, acct, store, extras, res).onPerformSync();
    }
}
