package com.isecpartners.samplesync.sdcard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import android.accounts.OperationCanceledException;
import java.io.IOException;

import com.isecpartners.samplesync.GenericSync;
import com.isecpartners.samplesync.IBlobStore;

/**
 * Android calls through this interface to request a sync.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "sdcard.SyncAdapter";
    private static final String TOKEN_TYPE = "com.isecpartners.samplesync.sdcard"; // XXX?
    private final Context mCtx;

    public SyncAdapter(Context context) {
        super(context, true);
        mCtx = context;
    }

    /* a synch is requested. */
    @Override
    public void onPerformSync(Account acct, Bundle extras, String authority, ContentProviderClient provider, SyncResult res) {
        AccountManager mgr = AccountManager.get(mCtx);
        Log.v(TAG, "XXX should sync here with sdcard store: " + acct.name);
        IBlobStore store = new Store(acct.name);
        GenericSync.onPerformSync(mCtx, TOKEN_TYPE, "", store, res);
    }
}
