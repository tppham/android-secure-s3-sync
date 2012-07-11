package com.isecpartners.samplesync;

import java.nio.ByteBuffer;

import android.content.Context;
import android.util.Log;

import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Marsh;

/*
 * Some helpers for account management.
 * XXX perhaps later this will morph into an account preferences gui.
 */
public class AccountHelper {
    private static final String TAG = "AccountHelper";
    private static final int MAXBUFSIZE = 1024 * 1024;
    Context mCtx;
    String mName;

    public AccountHelper(Context ctx, String name) {
        mCtx = ctx;
        mName = name;
    }

    public String getDir() {
        return mCtx.getDir("state", Context.MODE_PRIVATE).getPath();
    }

    public FileStore getLocalStore() {
        return new FileStore(getDir());
    }

    /* load a contact set from this account's bucket */
    public ContactSetBS load(String setName, IBlobStore store, String key) throws Marsh.Error, IBlobStore.Error {
        ByteBuffer buf = store.get(mName, key);
        ContactSetBS cs = ContactSetBS.unmarshal(setName, buf);
        Marsh.unmarshEof(buf);
        return cs;
    }

    /* save a contact set to this account's bucket */
    public void save(IBlobStore s, String key, ContactSetBS cs) throws Marsh.Error, IBlobStore.Error {
        Log.v(TAG, "save: " + cs.name + " dirty: " + cs.dirty);
        if(!cs.dirty)
            return;

        ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
        cs.marshal(buf);
        buf.flip();
        s.put(mName, key, buf);
    }

    public void initStore(IBlobStore s, String key) throws IBlobStore.Error {
        ContactSetBS empty = new ContactSetBS("empty");
        try {
            save(s, "synch", empty);
        } catch(final Marsh.Error e) {
            // should never happen
            Log.e(TAG, "marshal empty set failed! should never happen!");
        }
    }
}

