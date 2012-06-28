package com.isecpartners.samplesync;

import android.content.Context;

import com.isecpartners.samplesync.model.ContactSet;
import com.isecpartners.samplesync.model.ContactSetDB;
import com.isecpartners.samplesync.model.Synch;

public class Sync2 {
    Context mCtx;
    boolean mPrefLocal;

    public Sync2(Context ctx) {
        mCtx = ctx;
        mPrefLocal = true;
    }

    // XXX use a blob store?

    public void run() {
        // XXX figure out account types
        ContactSetDB last = new ContactSetDB("last", mCtx, "", "", true);
        ContactSetDB local = new ContactSetDB("local", mCtx, null, null, false);
        ContactSet remote = new ContactSet("remote");

        // XXX perhaps this should be implicit...
        last.loadContacts();
        local.loadContacts();

        // XXX load remote contacts into remote...
        //remote.loadContacts(); // XXX

        Synch s = new Synch(last, local, remote, mPrefLocal);
        s.sync();

        // XXX save remote contacts back out to remote...
    }
}

