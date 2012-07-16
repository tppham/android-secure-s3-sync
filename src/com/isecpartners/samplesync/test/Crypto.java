package com.isecpartners.samplesync.test;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;

import com.isecpartners.samplesync.model.Blob;
import com.isecpartners.samplesync.model.Contact;
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
        Blob sendb = new Blob("sent", pw);
        Contact c = sendb.set.add();
        sendb.set.addData(c, new Name("Don", "King"));
        sendb.set.addData(c, new Phone("888-555-1212", 0, null));
        sendb.set.commit();

        /* encodes it and sends it */
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            sendb.marshal(buf);
            Log.v(TAG, "sending: " + sendb);
            Log.v(TAG, "send data: " + C.hex(buf));
        } catch(final Exception e) {
            Log.e(TAG, "error marshalling blob: " + e);
            return;
        }

        /* receiver decodes it using nothing but the password */
        buf.flip();
        try {
            Blob recvb = Blob.unmarshal(pw, "received", buf);
            Log.v(TAG, "received: " + recvb);
        } catch(final Exception e) {
            Log.e(TAG, "error unmarshalling blob: " + e);
            return;
        }
    }

    public void onStart() {
        super.onStart();
        testPrims();
        testProto();
    }
}

