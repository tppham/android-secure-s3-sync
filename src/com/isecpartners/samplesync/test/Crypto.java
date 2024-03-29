package com.isecpartners.samplesync.test;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import com.isecpartners.samplesync.AccountHelper;
import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.model.Blob;
import com.isecpartners.samplesync.model.Contact;
import com.isecpartners.samplesync.model.ContactSetBS;
import com.isecpartners.samplesync.model.Name;
import com.isecpartners.samplesync.model.Phone;


/**
 * Test crypto code.
 * Assumes someone else has added the spongy castle provider.
 */
public class Crypto extends Activity {
    static String TAG = "test.Crypto";

    /* 
     * For convenience, to avoid name clash on "Crypto". 
     * see model/Crypto.java for details.
     */
    class C extends com.isecpartners.samplesync.model.Crypto {};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void testPrims() {
        /*
         * a user will randomly generate a list of words, or enter
         * a list generated previously.  With this we'll generate
         * a key using PBKDF2, using a random salt and a fixed
         * iteration count.
         */
        String pw = "This is a test passphrase I just thought up!";
        byte[] salt = Hex.decode("233952DEE4D5ED5F9B9C6D6FF80FF478");
        byte[] K = C.genKey(pw, salt, 100);
        Log.v(TAG, "genkey: " + C.hex(K));

        /* we'll generate a random IV */
        byte[] IV = Hex.decode("62EC67F9C3A4A407FCB2A8C49031A8B3");
        byte[] pt = "testing".getBytes();

        /* we'll encrypt the message and send the (IV, salt, iter count, ciphertext) */
        byte[] ct = C.encrypt(K, IV, pt);
        Log.v(TAG, "encrypted: " + C.hex(ct));

        /* The receiver will generate the same key and use it to decrypt */
        byte[] pt2 = C.decrypt(K, IV, ct);
        Log.v(TAG, "decrypted: " + new String(pt2));
    }

    void testProto() {
        String pw = "This is a test passphrase I just thought up!";

        /* sender makes some data */
        Blob sendb = new Blob(pw, C.genSalt(), C.genIV(), "sent");
        Contact c = sendb.set.add();
        sendb.set.addData(c, new Name("Don", "King"));
        sendb.set.addData(c, new Phone("888-555-1212", 0, null));
        sendb.set.commit();

        /* encodes it and sends it */
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            sendb.marshal(buf);
            Log.v(TAG, "sending: " + sendb);
        } catch(final Exception e) {
            Log.e(TAG, "error marshalling blob: " + e);
            return;
        }

        buf.flip();
        Log.v(TAG, "send data: " + C.hex(buf));

        /* receiver decodes it using nothing but the password */
        try {
            Blob recvb = Blob.unmarshal(pw, "received", buf);
            Log.v(TAG, "received: " + recvb);
        } catch(final Exception e) {
            Log.e(TAG, "error unmarshalling blob: " + e);
            return;
        }

        /* now try it with bad data */
        buf.array()[0] ++;
        buf.flip();
        Log.v(TAG, "bad data: " + C.hex(buf));

        /* receiver decodes it using nothing but the password */
        try {
            Blob recvb = Blob.unmarshal(pw, "received", buf);
            Log.v(TAG, "received: " + recvb);
        } catch(final Exception e) {
            Log.e(TAG, "error unmarshalling blob: " + e);
            return;
        }
    }

    /* use the account helper to save and load an encrypted contact set */
    void testAccountHelper() {
        String acct = "testaccount";
        String pw = "this is a test passphrase";
        AccountHelper h1 = new AccountHelper(this, acct, pw);
        try {
            IBlobStore s = h1.getStateStore();
            h1.initStore(s);
            Log.v(TAG, "initialized test store");
        } catch(final Exception e) {
            Log.e(TAG, "error initializing test store");
        }

        AccountHelper h2 = new AccountHelper(this, acct, pw);
        try {
            IBlobStore s = h2.getStateStore();
            ContactSetBS cs = h2.load("local", s, "synch");
            Log.v(TAG, "loaded: " + cs);
        } catch(final Exception e) {
            Log.e(TAG, "error loading test store");
        }
    }
    
    public void onStart() {
        super.onStart();

       new Thread(new Runnable() {
            public void run() {
                try {
                    testPrims();
                    testProto();
                    testAccountHelper();
                } catch(Exception e) {
                    Log.v(TAG, "exception in test cases: " + e);
                } finally {
                    Log.v(TAG, "tests done");
                }
            }
        }).start();
        Log.v(TAG, "started...");
    }
}

