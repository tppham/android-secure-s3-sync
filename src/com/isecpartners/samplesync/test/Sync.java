package com.isecpartners.samplesync.test;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;

import com.isecpartners.samplesync.*;
import com.isecpartners.samplesync.model.ContactSet;
import com.isecpartners.samplesync.model.ContactSetDB;
import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Synch;
import com.isecpartners.samplesync.model.Marsh;

/*
 * Test out the synch process.
 * To run this, you need to create /sdcard/secrets.txt
 * with two lines, one with your s3 key id, and one with your s3 key.
 */
public class Sync extends Activity {
    public static final String TAG = "test.Sync";
    public static final int MAXBUFSIZE  = 1024 * 1024;
    Context mCtx;
    boolean mPrefLocal;
    String keyId, keyVal;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = this;
        mPrefLocal = true;
    }

    boolean readSecrets() { // XXX hack, get them from elsewhere
        try {
            String fn = "/sdcard/secrets.txt";
            BufferedReader in = new BufferedReader(new FileReader(fn));
            keyId = in.readLine();
            keyVal = in.readLine();
            Log.v(TAG, "creds - id: " + keyId + ", key: " + keyVal);
            return true;
        } catch(final IOException e) {
            Log.v(TAG, "error loading secrets: " + e);
            return false;
        }
    }

    ContactSetBS load(String name, ByteBuffer buf) {
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

    ContactSetBS loadFromStore(String name, IBlobStore store) {
        // XXX should be trivial to refactor the IBlobStore
        // interface and implementations to return a ByteBuffer.
        byte[] bs = store.get("synchtest", "synch");
        if(bs == null)
            return load(name, null);
        ByteBuffer buf = ByteBuffer.allocate(bs.length);
        buf.put(bs);
        buf.flip();
        return load(name, buf);
    }

    ContactSetBS loadFromDisk(String name, String fn) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
            FileChannel ch = new FileInputStream(new File(fn)).getChannel();
            ch.read(buf);
            buf.flip();
            return load(name, buf);
        } catch(final IOException e) {
            Log.v(TAG, "error loading " + name + " data from " + fn + ": " + e);
            return load(name, null);
        }
    }

    ByteBuffer save(ContactSetBS cs) {
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

    void saveToStore(IBlobStore s, ContactSetBS cs) {
        ByteBuffer buf = save(cs);
        if(buf != null) {
            // XXX create bucket?
            // XXX blob store should take bytebuffer
            byte[] bs = new byte[buf.remaining()];
            buf.get(bs);
            s.put("synchtest", "synch", bs);
        }
    }

    void saveToDisk(String fn, ContactSetBS cs) {
        try {
            ByteBuffer buf = save(cs);
            if(buf != null) {
                FileChannel ch = new FileOutputStream(new File(fn)).getChannel();
                ch.write(buf);
                ch.close();
            }
        } catch(final IOException e) {
            Log.v(TAG, "error saving " + cs.name + " data: " + e);
        }
    }

    IBlobStore getStore() {
        try {
            if(true) {
                if(readSecrets())
                    return new com.isecpartners.samplesync.s3.Store(keyVal, keyId);
            } else {
                return new com.isecpartners.samplesync.sdcard.Store("/sdcard/dir");
            }
        } catch (Exception e) {
            Log.e(TAG, "error building blob store: " + e);
        }
        return null;
    }

    protected void onStart() {
        super.onStart();

        IBlobStore store = getStore();
        if(store == null)
            return;

        // XXX we need to differentiate failure to parse
        // vs. failure to fetch, to give user appropriate errors

        // XXX figure out account types to create new contacts as!
        ContactSet local = new ContactSetDB("localdb", mCtx, null, null);
        ContactSetBS last = loadFromDisk("last", "/sdcard/last.bin");
        //ContactSetBS remote = loadFromDisk("remote", "/sdcard/remote.bin");
        ContactSetBS remote = loadFromStore("remote", store);

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
        s.sync();

        saveToDisk("/sdcard/last.bin", last);
        //saveToDisk("/sdcard/remote.bin", remote);
        saveToStore(store, remote);

        // XXX try out the unmarshalling
        loadFromDisk("xxx", "/sdcard/last.bin");
    }
}

