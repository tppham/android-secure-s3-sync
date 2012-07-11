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

    static ContactSetBS load(String setName, IBlobStore store, String bucket) throws Marsh.Error, IBlobStore.Error {
        // XXX should be trivial to refactor the IBlobStore
        // interface and implementations to return a ByteBuffer.
        byte[] bs = store.get(bucket, "synch");
        ByteBuffer buf = ByteBuffer.allocate(bs.length);
        buf.put(bs);
        buf.flip();

        ContactSetBS cs = ContactSetBS.unmarshal(setName, buf);
        Marsh.unmarshEof(buf);
        return cs;
    }

    static void save(IBlobStore s, String bucket, ContactSetBS cs) throws Marsh.Error, IBlobStore.Error {
        Log.v(TAG, "save: " + cs.name + " dirty: " + cs.dirty);
        if(!cs.dirty)
            return;

        ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
        cs.marshal(buf);
        buf.flip();

        byte[] bs = new byte[buf.remaining()];
        buf.get(bs);
        s.put(bucket, "synch", bs);
    }

    /*
     * An error occurred saving the remote data.  Make a last ditch
     * effort to save a local copy in hopes that we can push it later.
     */
    public static void saveBackup(IBlobStore store, ContactSetBS cs) {
        try {
            save(store, "backup", cs);
        } catch(final Exception e) {
            Log.e(TAG, "save backup failed!"); // nothign to do - just log it
        }
    }
    
    /*
     * If there's a previous backup to push, load it, and then
     * save it to the remote.
     */
    public static boolean pushBackup(IBlobStore lastStore, IBlobStore remStore, String name) {
        // XXX todo, read in backup, if it doesnt exist, return
        // if it does, save it to remStore, and gracefully handle
        // all errors.
        return true;
    }

    // XXX update syncResult!
    // XXX we should have some handle on the account so we can
    // record some per-account info like last synch time...
    // XXX we need a handle on preferences, like prefLocal!
    // XXX re-evaluate exception list
    private static void _onPerformSync(Context ctx, String name, IBlobStore remStore, SyncResult res) throws Exception {
        // XXX figure out account types to create new contacts as!

        name = name.toLowerCase(); // XXX might destroy uniqueness! fixme!
                                    // XXX the issue here is that s3 wants lowercase bucket names in its API
    	Log.v(TAG, "_onPerformSync " + name);

        String lastPath = ctx.getDir("last", Context.MODE_PRIVATE).getPath();
        IBlobStore lastStore = new FileStore(lastPath);
        ContactSetBS last, remote;

        if(!pushBackup(lastStore, remStore, name))
            return;

        // load last
        try {
            last = load("last", lastStore, name);
        } catch(final Exception e) { // Marsh.Error, IBlobStore.Error
            // XXX notify: delete acct?
            Log.e(TAG, "corrupt account state!");
            return;
        }

        // load remote
        try {
            remote = load("remote", remStore, name);
        } catch(final Marsh.BadVersion e) {
            // XXX notify: update your software
            Log.e(TAG, "synch data has bad version!");
            return;
        } catch(final Marsh.Error e) {
            // XXX notify: corrupt, wipe?
            Log.e(TAG, "synch data corrupt!");
            return;
        } catch(final IBlobStore.AuthError e) {
            // XXX invalidate creds
            // XXX notify: update creds
            Log.e(TAG, "synch auth failed!");
            return;
        } catch(final IBlobStore.Error e) {
            // XXX notify: retry?
            Log.e(TAG, "synch load failed!");
            return;
        }

        // load local
        ContactSet local = new ContactSetDB("localdb", ctx, null, null);

        if(last.id != remote.id) {
            if(last.contacts.isEmpty()) { // lock on to the remote's ID the first time we run
                last.id = remote.id;
            } else { // disallow any changes in remote ID after the first run
                // XXX notify: synch data changed.  wipe?
                Log.e(TAG, "synch data was replaced by someone else!");
                return;
            }
        }

        Synch s = new Synch(last, local, remote, mPrefLocal);
        if(!s.sync()) {
            Log.v(TAG, "no changes!");
            return;
        }

        Log.v(TAG, "saving changes");
        try {
            save(remStore, name, remote);
        } catch(final IBlobStore.AuthError e) {
            // XXX invalidate creds
            // XXX notify: auth error saving, update creds
            saveBackup(lastStore, remote);
            Log.e(TAG, "auth error saving synch data");
            /* keep going.. we cant back out now... */
        } catch(final IBlobStore.Error e) {
            // XXX notify: error saving, try again?
            saveBackup(lastStore, remote);
            Log.e(TAG, "error saving synch data");
            /* keep going.. we cant back out now... */
        } catch(final Marsh.Error e) { // should never happen
            Log.e(TAG, "internal error saving synch data!");
        } 

        try {
            save(lastStore, name, last);
        } catch(final Exception e) { // Marsh.Error, IBlobStore.Error
            // XXX notify: delete acct?
            Log.e(TAG, "couldn't update account state!");
            return;
        }

        // XXX update the last synch time for the account
    }

    /* a synch is requested. */
    public static void onPerformSync(Context ctx, String name, String acctType, String token, IBlobStore store, SyncResult res) {
        Log.v(TAG, "onPerformSync");

        try {
            _onPerformSync(ctx, name, store, res);

        // XXX re-evaluate which of these are needed...
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "onPerformSync", e);
        } catch (final IOException e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numIoExceptions++;
        //} catch (final AuthenticationException e) {
        //    Log.e(TAG, "onPerformSync", e);
        //    mgr.invalidateAuthToken(acctType, token);
        //    res.stats.numAuthExceptions++;
        //} catch (final ParseException e) {
        //    Log.e(TAG, "onPerformSync", e);
        //    res.stats.numParseExceptions++;
        } catch (final Exception e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numIoExceptions++; // XXX?
        }
    }

    /* return a token or null, updating the result status */
    // XXX dead code
    public static String getToken(Context ctx, Account acct, String tokenType, SyncResult res) {
        Log.v(TAG, "getToken");
        AccountManager mgr = AccountManager.get(ctx);

        try {
            return mgr.blockingGetAuthToken(acct, tokenType, true);

        } catch (final AuthenticatorException e) {
            Log.e(TAG, "getToken", e);
            res.stats.numParseExceptions++;
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "getToken", e);
        } catch (final IOException e) {
            Log.e(TAG, "getToken", e);
            res.stats.numIoExceptions++;
        }
        return null;
    }
}

