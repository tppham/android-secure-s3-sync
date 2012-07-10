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

    static ContactSetBS load(String name, ByteBuffer buf) {
        if(buf != null) {
            try {
                ContactSetBS cs = ContactSetBS.unmarshal(name, buf);
                Marsh.unmarshEof(buf);
                return cs;
            } catch(final Marsh.Error e) {
                Log.v(TAG, "error unmarshalling " + name + " data: " + e);
            } 
        }

        // XXX in the real program this should involve user
        // interaction..  returning null might be best
        Log.v(TAG, "making new empty contact set for " + name);
        return new ContactSetBS(name);
    }

    static ContactSetBS loadFromStore(String name, IBlobStore store, String bucket) {
        // XXX should be trivial to refactor the IBlobStore
        // interface and implementations to return a ByteBuffer.
        byte[] bs = store.get(bucket, "synch");
        if(bs == null)
            return load(name, null);
        ByteBuffer buf = ByteBuffer.allocate(bs.length);
        buf.put(bs);
        buf.flip();
        return load(name, buf);
    }

    static ContactSetBS loadFromDisk(String name, String fn) {
        try {
            FileInputStream is = new FileInputStream(new File(fn));
            try {
                FileChannel ch = is.getChannel();
                ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
                ch.read(buf);
                buf.flip();
                return load(name, buf);
            } finally {
                is.close();
            }
        } catch(final IOException e) {
            Log.v(TAG, "error loading " + name + " data from " + fn + ": " + e);
            return load(name, null);
        } 
    }

    static ByteBuffer save(ContactSetBS cs) {
        Log.v(TAG, "" + cs.name + " dirty: " + cs.dirty);
        if(!cs.dirty)
            return null;
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
            cs.marshal(buf);
            buf.flip();
            return buf;
        } catch(final Marsh.Error e) {
            Log.v(TAG, "error marshalling " + cs.name + " data");
            return null;
        } 
    }

    static void saveToStore(IBlobStore s, String bucket, ContactSetBS cs) {
        ByteBuffer buf = save(cs);
        if(buf != null) {
            // XXX create bucket?
            // XXX blob store should take bytebuffer
            byte[] bs = new byte[buf.remaining()];
            buf.get(bs);
            s.put(bucket, "synch", bs);
        }
    }

    static void saveToDisk(String fn, ContactSetBS cs) {
        try {
            ByteBuffer buf = save(cs);
            if(buf != null) {
                FileOutputStream os = new FileOutputStream(new File(fn));
                try {
                    FileChannel ch = os.getChannel();
                    ch.write(buf);
                } finally {
                    os.close();
                }
            }
        } catch(final IOException e) {
            Log.v(TAG, "error saving " + cs.name + " data: " + e);
        } 
    }

    // XXX update syncResult!
    // XXX we should have some handle on the account so we can
    // record some per-account info like last synch time...
    // XXX we need a handle on preferences, like prefLocal!
    // XXX re-evaluate exception list
    private static void _onPerformSync(Context ctx, String name, IBlobStore store, SyncResult res) throws Exception {
    	Log.v(TAG, "_onPerformSync");
        String lastPath = "/sdcard/" + name + ".bin"; // XXX! use private location!

        // XXX figure out account types to create new contacts as!
        ContactSet local = new ContactSetDB("localdb", ctx, null, null);
        // XXX last should be stored elsewhere!
        ContactSetBS last = loadFromDisk("last", lastPath);
        ContactSetBS remote = loadFromStore("remote", store, name);

        if(last.id != remote.id) {
            if(last.contacts.isEmpty()) {
                // lock on to the remote's ID the first time we run
                last.id = remote.id;
            } else {
                // disallow any changes in remote ID after the first run
                Log.v(TAG, "Remote has different ID!  we can't synch to it!");
                // XXX synch failed, notify user
                return;
            }
        }

        Synch s = new Synch(last, local, remote, mPrefLocal);
        if(s.sync()) {
            // XXX notify user of synch
            // update last synch time
            saveToDisk(lastPath, last);
            saveToStore(store, name, remote);
        }
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

