package com.isecpartners.samplesync.s3;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


import com.isecpartners.samplesync.GenericSync;
import com.isecpartners.samplesync.IBlobStore;

/**
 * Android calls through this interface to request a sync.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "s3.SyncAdapter";
    private static final String TOKEN_TYPE = "com.isecpartners.samplesync.s3"; // XXX?
    private final Context mCtx;

    public SyncAdapter(Context context) {
        super(context, true);
        mCtx = context;
    }

    /* a synch is requested. */
    @Override
    public void onPerformSync(Account acct, Bundle extras, String authority, ContentProviderClient provider, SyncResult res) {
        AccountManager mgr = AccountManager.get(mCtx);
        String keyid = mgr.getUserData(acct, "keyID");
        String key = mgr.getPassword(acct);
        Log.v(TAG, "sync here with s3 store: " + acct.name + " " +  keyid + " " + key);
        IBlobStore store = new Store(keyid, key);
        new GenericSync(mCtx, acct, TOKEN_TYPE, store, extras, res).onPerformSync();
    }
}
