package com.isecpartners.samplesync;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.util.Log;

import com.isecpartners.samplesync.model.ContactSet;
import com.isecpartners.samplesync.model.ContactSetDB;
import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Synch;
import com.isecpartners.samplesync.model.Marsh;

public class Sync2 {
    public static final String TAG = "Sync2";
    public static final int MAXBUFSIZE  = 1024 * 1024;
    Context mCtx;
    boolean mPrefLocal;

    public Sync2(Context ctx) {
        mCtx = ctx;
        mPrefLocal = true;
    }

    // XXX use a blob store?

    ContactSetBS loadFromDisk(String name, String fn) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
            FileChannel ch = new FileInputStream(new File(fn)).getChannel();
            ch.read(buf);
            buf.flip();

            ContactSetBS cs = ContactSetBS.unmarshal(name, buf);
            Marsh.unmarshEof(buf);
            return cs;
        } catch(final Marsh.Error e) {
            Log.v(TAG, "error unmarshalling " + name + " data: " + e);
        } catch(final IOException e) {
            Log.v(TAG, "error loading " + name + " data from " + fn + ": " + e);
        }
        // XXX in the real program this should involve user
        // interaction..  returning null might be best
        return new ContactSetBS(name);
    }

    void storeToDisk(String fn, ContactSetBS cs) {
        Log.v(TAG, "" + cs.name + " dirty: " + cs.dirty);
        if(!cs.dirty)
            return;
        try {
            ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
            cs.marshal(buf);
            buf.flip();

            FileChannel ch = new FileOutputStream(new File(fn)).getChannel();
            ch.write(buf);
            ch.close();
        } catch(final Marsh.Error e) {
            Log.v(TAG, "error marshalling " + cs.name + " data to " + fn + ": " + e);
        } catch(final IOException e) {
            Log.v(TAG, "error saving " + cs.name + " data: " + e);
        }
    }

    public void run() {
        // XXX figure out account types to create new contacts as!
        ContactSet local = new ContactSetDB("localdb", mCtx, null, null);
        ContactSetBS last = loadFromDisk("last", "/sdcard/last.bin");
        ContactSetBS remote = loadFromDisk("remote", "/sdcard/remote.bin");

        Synch s = new Synch(last, local, remote, mPrefLocal);
        s.sync();

        storeToDisk("/sdcard/last.bin", last);
        storeToDisk("/sdcard/remote.bin", remote);

        // XXX try out the unmarshalling
        loadFromDisk("xxx", "/sdcard/last.bin");
    }
}

