package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;

/*
 * The data blob we store remotely and locally.  It contains
 * a ContactSet and is protected with authenticated encryption.
 */
public class Blob {
    private static final String TAG = "model.Blob";
    public static final int SALTLEN = 8;
    public static final int IVLEN = 16; // XXX right?
    public static final int ITERCOUNT = 1000; // XXX whats a good value here?
    public static final int MAGIC = 0x1badd00d;

    public String passphrase;
    public ContactSetBS set;
    public byte[] salt, iv;
    public int iterCount;
    public int magic;

    public static Blob unmarshal(String pw, String name, ByteBuffer buf) throws Marsh.Error {

        /* plaintext header */
        salt = Marsh.unmarshBytes(buf, SALTLEN);
        Blob x = new Blob(name, pw, salt);
        x.iterCount = Marsh.unmarshInt32(buf);
        x.iv = Marsh.unmarshBytes(buf, IVLEN);

        /* decrypt remaining */
        int csz = buf.remaining();
        byte[] cipher = Marsh.unmarshBytes(buf, csz);
        byte[] key = Crypto.genKey(pw, x.salt, x.iterCount);
        byte[] plain = Crypto.decrypt(key, x.iv, cipher);
        ByteBuffer buf2 = ByteBuffer.wrap(plain);

        /* decode encrypted body */
        x.magic = Marsh.unmarshInt32(buf2);
        if(x.magic != MAGIC)
            throw new Marsh.BadFormat("bad magic value: " + x.magic);
        x.set = ContactSetBS.unmarshal(name, buf2);
        Marsh.unmarshEof(buf2);
        return x;
    }

    public Blob(String pw, byte[] salt, String name) {
        passphrase = pw;
        salt = _salt;
        set = new ContactSetBS(name);
        iv = null;
        iterCount = ITERCOUNT;
        magic = MAGIC;
    }

    public void marshal(ByteBuffer buf) throws Marsh.Error {
        /* plaintext header */
        Marsh.marshBytes(buf, salt, SALTLEN);
        Marsh.marshInt32(buf, iterCount);
        Marsh.marshBytes(buf, iv, IVLEN);

        /* encode encrypted body */
        ByteBuffer buf2 = ByteBuffer.allocate(buf.remaining());
        Marsh.marshInt32(buf2, magic);
        set.marshal(buf2);

        /* encrypt */
        buf2.flip();
        byte[] plain = Marsh.unmarshBytes(buf2, (int)buf2.remaining());
        byte[] key = Crypto.genKey(passphrase, salt, iterCount);
        byte[] cipher = Crypto.encrypt(key, iv, plain);
        Marsh.marshBytes(buf, cipher, cipher.length);
    }

    public String toString() {
        return "[Blob magic=" + magic 
                + " salt=" + Crypto.hex(salt) 
                + " iv=" + Crypto.hex(iv)
                + " iterCount=" + iterCount
                + " set=" + set
                + "]";
    }
}

