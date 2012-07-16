package com.isecpartners.samplesync;

import java.nio.ByteBuffer;

import android.content.Context;
import android.util.Log;

import com.isecpartners.samplesync.model.Crypto;
import com.isecpartners.samplesync.model.Blob;
import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Marsh;

/*
 * Some helpers for account management.
 * XXX perhaps later this will morph into an account preferences gui.
 *
 * This class allows us to load and store contact sets remotely
 * and on the local disk.  It also managed the encryption protocol.
 * 
 * During key generation we strive to keep reusing the same salt
 * (no sense in letting an attacker pick his favorite salt!).  When
 * we start a new store we make a new salt.  On every successful
 * load (which means decryption was successful and no attacker is
 * present unless he has the secret key) we lock on to the salt used.
 */
public class AccountHelper {
    private static final String TAG = "AccountHelper";
    private static final int MAXBUFSIZE = 1024 * 1024;

    Context mCtx;
    String mName;
    String mPw;
    byte[] mSalt;

    public AccountHelper(Context ctx, String name, String pw) {
        mCtx = ctx;
        // XXX force name to lower.
        // would be better to force user to enter lower case only!
        mName = name.toLowerCase();
        mPw = pw;
        mSalt = null;
    }

    public String getDir() {
        return mCtx.getDir("state", Context.MODE_PRIVATE).getPath();
    }

    public FileStore getStateStore() {
        return new FileStore(getDir());
    }

    public boolean storeExists(IBlobStore s) throws IBlobStore.Error {
        return s.storeExists(mName);
    }

    public boolean stateStoreExists() {
        FileStore f = getStateStore();
        return f.storeExists(mName);
    }

    /* load a contact set from this account's bucket */
    public ContactSetBS load(String setName, IBlobStore store, String key) throws Marsh.Error, IBlobStore.Error {
        Log.v(TAG, "load: " + mName + " " + key);
        ByteBuffer buf = store.get(mName, key);
        Blob blob = Blob.unmarshal(mPw, setName, buf);
        if(mSalt == null)
            mSalt = blob.salt;
        return blob.set;
    }

    /* save a contact set to this account's bucket */
    public void save(IBlobStore s, String key, ContactSetBS cs) throws Marsh.Error, IBlobStore.Error {
        Log.v(TAG, "save: " + cs.name + " key " + key + " dirty: " + cs.dirty);
        if(!cs.dirty)
            return;

        Blob blob = new Blob(mPw, mSalt, Crypto.genIV(), cs.name);
        blob.set = cs;
        if(mSalt == null)
            throw new Marsh.Error("internal error. shouldnt happen.  salt == null");

        ByteBuffer buf = ByteBuffer.allocate(MAXBUFSIZE);
        blob.marshal(buf);
        buf.flip();
        s.put(mName, key, buf);
    }

    public void initStore(IBlobStore s, String key) throws IBlobStore.Error {
        ContactSetBS empty = new ContactSetBS("empty");
        empty.dirty = true;

        if(mSalt == null)
            mSalt = Crypto.genSalt();

        try {
            s.create(mName);
            save(s, key, empty);
        } catch(final Marsh.Error e) {
            // should never happen
            Log.e(TAG, "marshal empty set failed! should never happen!");
        }
    }
    public void initStore(IBlobStore s) throws IBlobStore.Error {
        initStore(s, "synch");
    }
}

