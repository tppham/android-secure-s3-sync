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
    Context mCtx;
    boolean mPrefLocal;

    public Sync2(Context ctx) {
        mCtx = ctx;
        mPrefLocal = true;
    }

    // XXX use a blob store?

    public void run() {
        // XXX figure out account types
        ContactSet last = ContactSetDB.last(mCtx, "XXX", "XXX");
        ContactSet local = ContactSetDB.local(mCtx, null, null);

        ContactSetBS remote;
        if(false) {
            // XXX read it in from some buffer
            //remote = ContactSetBS.unmarshal(buf);
        } else {
            // create new one
            remote = new ContactSetBS();
        }

        // XXX load remote contacts into remote...
        //remote.loadContacts(); // XXX

        Synch s = new Synch(last, local, remote, mPrefLocal);
        s.sync();

        Log.v(TAG, "remote dirty: " + remote.dirty);
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024 * 1024);
            remote.marshal(buf);
            buf.flip();

            FileChannel ch = new FileOutputStream(new File("/sdcard/remote.bin")).getChannel();
            ch.write(buf);
            ch.close();
        } catch(Marsh.Error e) {
            Log.v(TAG, "error marshalling data: " + e);
        } catch(IOException e) {
            Log.v(TAG, "error saving data: " + e);
        }
    }
}

