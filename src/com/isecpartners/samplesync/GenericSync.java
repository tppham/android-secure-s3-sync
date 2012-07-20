package com.isecpartners.samplesync;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.isecpartners.samplesync.model.Contact;
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
    public static final int MAXBUFSIZE  = 1024 * 1024;

    Context mCtx;
    Account mAcct;
    IBlobStore mRemStore;
    SyncResult mRes;
    Bundle mExtras;

    public GenericSync(Context ctx, Account acct, IBlobStore store, Bundle extras, SyncResult res) {
        mCtx = ctx;
        mAcct = acct;
        mRemStore = store;
        mRes = res;
        mExtras = extras;
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
        // XXX todo, read in backup, if it doesnt exist, return.
        // if it does, save it to remStore, and gracefully handle
        // all errors.
        return true;
    }

    static boolean contactIsOwned(Contact c, Account[] accts) {
        /* "null" account always "exists" */
        if(c.acctType == null || c.acctName == null)
            return true;
        for(int i = 0; i < accts.length; i++) {
            if(accts[i].name.equals(c.acctName) 
            && accts[i].type.equals(c.acctType))
                return true;
        }
        return false;
    }

    /*
     * If a local synch provider was deleted we will want to
     * handle it specially.  The accounts will all have been
     * deleted in the local database, but the user may want to 
     * keep all of those contacts.  Without any action we will 
     * see them all as account deletions.
     *
     * Return true if we handled this ourselves, or false if user
     * intervention is required.
     */
    public boolean handleAccountDeleted(ContactSet cs, boolean forceDelete, boolean keepLocals) {
        /* deletions will be forced without any intervention */
        if(forceDelete)
            return true;

        /*
         * Figure out if there are any contacts belonging to an
         * account that no longer exists.  We do this by creating
         * a list of contacts for accounts that do exist (we'll need it later).
         */
        AccountManager mgr = AccountManager.get(mCtx);
        Account[] accts = mgr.getAccounts();

        List<Contact> owned = new LinkedList<Contact>();
        for(Contact c : cs.contacts) {
            if(contactIsOwned(c, accts))
                owned.add(c);
            else Log.v(TAG, "orphaned contact: " + c); // debug
        }

        /* all are owned, nothing to do. */
        int numDeletes = cs.contacts.size() - owned.size();
        if(numDeletes == 0)
            return true;

        /* user needs to make the choice */
        if(!keepLocals) {
            mRes.stats.numDeletes = numDeletes;
            return false;
        }

        /* 
         * To prevent deletions we remove all the unowned
         * items from our last set so they look like new remote 
         * additions during the synch.
         */
        Log.v(TAG, "removing all orphaned contacts from last set");
        cs.contacts = owned;
        return true;
    }

    /* a synch is requested. */
    public void onPerformSync() { 
        // XXX let the user choose which account to create new contacts as?

    	Log.v(TAG, "_onPerformSync " + mAcct.name);
        AccountHelper h = new AccountHelper(mCtx, mAcct);
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

        /* 
         * Determine if any accounts that are used in local went away.
         * If so, we need the user's consent first.
         * We use the SyncResult "tooManyDeletions" mechanism to handle this.
         * The synch framework will prompt the user and set the 
         * appropriate flag in the mExtras with the users choice.
         */
        boolean forceDelete = mExtras.getBoolean(ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS);
        boolean keepLocals = mExtras.getBoolean(ContentResolver.SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS);
        /* XXX In testing in android 10 this properly reports a synch
         * error and gives me the notification with the choice to keep or 
         * delete, but it never calls back the synch when I select one
         * of the choices!  So for now we're forcing the policy 
         * to keep locals.  We'll address the user choice later.
         */
        //if(!handleAccountDeleted(last, forceDelete, keepLocals)) {
        if(!handleAccountDeleted(last, false, true)) { // XXX force keep local
            mRes.tooManyDeletions = true;
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

        boolean prefLocal = h.getAcctPrefBool("prefLocal", true);
        Synch s = new Synch(last, local, remote, prefLocal, mRes.stats);
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

        h.setAcctPrefLong("lastSync", System.currentTimeMillis());
    }
}

