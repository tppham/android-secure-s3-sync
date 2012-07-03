package com.isecpartners.samplesync.s3;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


import com.amazonaws.services.s3.AmazonS3Client;
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
        String pw = mgr.getPassword(acct);
        
        Log.v(TAG, "XXX should sync here with s3 store: " + acct.name + " " + pw);
        IBlobStore store = new Store(acct.name, pw);
        GenericSync.onPerformSync(mCtx, TOKEN_TYPE, pw, store, res);
    }
}
