package com.isecpartners.samplesync;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException; // XXX review?
import android.accounts.OperationCanceledException; // XXX review?
import android.content.Context;
import android.content.SyncResult;
import android.util.Log;

import com.isecpartners.samplesync.model.ContactSet;
import com.isecpartners.samplesync.model.ContactSetDB;
import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Synch;
import com.isecpartners.samplesync.model.Marsh;

/**
 * Generic sync handler that doesn't care which type of
 * accounts or backends we're synchronizing through.
 */
public class GenericSync {
    private static final String TAG = "GenericSync";
    private static final boolean mPrefLocal = true; // XXX config!
    public static final int MAXBUFSIZE  = 1024 * 1024;

    Context mCtx;
    Account mAcct;
    String mTokenType;
    IBlobStore mRemStore;
    SyncResult mRes;

    // XXX can we get tokenType from acct and eliminate that arg and member?
    public GenericSync(Context ctx, Account acct, String tokenType, IBlobStore store, SyncResult res) {
        mCtx = ctx;
        mAcct = acct;
        mTokenType = tokenType;
        mRemStore = store;
        mRes = res;
    }

    /*
     * An error occurred saving the remote data.  Make a last ditch
     * effort to save a local copy in hopes that we can push it later.
     */
    public static void saveBackup(AccountHelper h, IBlobStore store, ContactSetBS cs) {
        try {
            h.save(store, "remotebackup", cs);
        } catch(final Exception e) {
            Log.e(TAG, "save backup failed!"); // nothign to do - just log it
        }
    }
    
    /*
     * If there's a previous backup to push, load it, and then
     * save it to the remote.
     */
    public static boolean pushBackup(AccountHelper h, IBlobStore lastStore, IBlobStore remStore) {
        // XXX todo, read in backup, if it doesnt exist, return
        // if it does, save it to remStore, and gracefully handle
        // all errors.
        return true;
    }

    /* a synch is requested. */
    // XXX update mRes syncResult!
    // XXX we need a handle on preferences, like prefLocal!
    public void onPerformSync() { 
        // XXX figure out account types to create new contacts as!

    	Log.v(TAG, "_onPerformSync " + mAcct.name);
        AccountManager mgr = AccountManager.get(mCtx);
        String passphrase = mgr.getUserData(mAcct, "passphrase");
        AccountHelper h = new AccountHelper(mCtx, mAcct.name, passphrase);
        IBlobStore lastStore = h.getStateStore();
        ContactSetBS last, remote;

        if(!pushBackup(h, lastStore, mRemStore))
            return;

        // load last
        try {
            last = h.load("last", lastStore, "synch");
        } catch(final Exception e) { // Marsh.Error, IBlobStore.Error
            // XXX notify: missing or corrupted state, delete acct?
            mRes.stats.numParseExceptions++;
            mRes.databaseError = true;
            Log.e(TAG, "corrupt account state! " + e);
            return;
        }

        // load remote
        try {
            remote = h.load("remote", mRemStore, "synch");
        } catch(final Marsh.BadVersion e) {
            // XXX notify: update your software
            mRes.stats.numParseExceptions++;
            mRes.databaseError = true;
            Log.e(TAG, "synch data has bad version! " + e);
            return;
        } catch(final Marsh.Error e) {
            // XXX notify: corrupt, wipe?
            mRes.stats.numParseExceptions++;
            mRes.databaseError = true;
            Log.e(TAG, "synch data corrupt! " + e);
            return;
        } catch(final IBlobStore.AuthError e) {
            /* note: we're not using an auth token right now.  If we were, we should:
            mgr.invalidateAuthToken(TOKENTYPE, token);
             */
            // XXX notify: update creds
            mRes.stats.numAuthExceptions++;
            Log.e(TAG, "synch auth failed! " + e);
            return;
        } catch(final IBlobStore.NotFoundError e) {
            // XXX notify: synch data not found, wipe?
            mRes.stats.numParseExceptions++;
            mRes.databaseError = true;
            Log.e(TAG, "synch load failed! " + e);
            return;
        } catch(final IBlobStore.Error e) { // probably intermittent failure
            // XXX notify: retry?
            mRes.stats.numIoExceptions++;
            Log.e(TAG, "synch load failed! " + e);
            return;
        }

        // load local
        ContactSet local = new ContactSetDB("localdb", mCtx, null, null);

        if(last.id != remote.id) {
            if(last.contacts.isEmpty()) { // lock on to the remote's ID the first time we run
                last.id = remote.id;
            } else { // disallow any changes in remote ID after the first run
                // XXX notify: synch data changed.  wipe?
                mRes.stats.numParseExceptions++;
                mRes.databaseError = true;
                Log.e(TAG, "synch data was replaced by someone else!");
                return;
            }
        }

        Synch s = new Synch(last, local, remote, mPrefLocal, mRes.stats);
        if(!s.sync()) {
            Log.v(TAG, "no changes!");
            return;
        }

        Log.v(TAG, "saving changes");
        try {
            h.save(mRemStore, "synch", remote);
        } catch(final IBlobStore.AuthError e) {
            /* invalidate auth tokens here if we were using them */
            // XXX notify: auth error saving, update creds
            saveBackup(h, lastStore, remote);
            mRes.stats.numAuthExceptions++;
            Log.e(TAG, "auth error saving synch data: " + e);
            /* keep going.. we cant back out now... */
        } catch(final IBlobStore.Error e) { // probably intermittent
            // XXX notify: error saving, try again?
            saveBackup(h, lastStore, remote);
            mRes.stats.numIoExceptions++;
            Log.e(TAG, "error saving synch data: " + e);
            /* keep going.. we cant back out now... */
        } catch(final Marsh.Error e) { // should never happen
            Log.e(TAG, "internal error saving synch data! " + e);
        } 

        try {
            h.save(lastStore, "synch", last);
        } catch(final Exception e) { // Marsh.Error, IBlobStore.Error
            // XXX notify: delete acct?
            mRes.stats.numParseExceptions++; // close enough...
            mRes.databaseError = true;
            Log.e(TAG, "couldn't update account state! " + e);
            return;
        }

        // XXX update the last synch time for the account
        mgr.setUserData(mAcct, "lastSync", "" + System.currentTimeMillis());
    }
}

