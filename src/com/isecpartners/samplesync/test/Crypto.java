package com.isecpartners.samplesync.test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.spongycastle.util.encoders.Hex;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;

/**
 * Test crypto code.
 */
public class Crypto extends Activity {
    static String TAG = "test.Crypto";

    static {
        //Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* generate a key from passwd */
    public static byte[] genKey(String passwd, byte[] salt, int iters) {
        PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
        byte[] pwb = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(passwd.toCharArray());
        g.init(pwb, salt, iters);
        KeyParameter ps = (KeyParameter)g.generateDerivedParameters(256);
        return ps.getKey();
    }

    static byte[] crypt(int opmode, byte[] K, byte[] IV, byte[] x) {
        Key k = new SecretKeySpec(K, "AES");
        IvParameterSpec iv = new IvParameterSpec(IV);
        try {
            //Cipher c = Cipher.getInstance("AES/EAX/NoPadding", "SC");
            Cipher c = Cipher.getInstance("AES/EAX/NoPadding");
            c.init(opmode, k, iv);
            return c.doFinal(x);
        } catch(final Exception e) { /* should never happen! */
            Log.e(TAG, "cipher error: " + e);
            return null;
        }
    }

    /* encrypt plain using key K and IV IV */
    public static byte[] encrypt(byte[] K, byte[] IV, byte[] plain) {
        return crypt(Cipher.ENCRYPT_MODE, K, IV, plain);
    }

    /* decrypt cipher using key K and IV IV */
    public static byte[] decrypt(byte[] K, byte[] IV, byte[] cipher) {
        return crypt(Cipher.DECRYPT_MODE, K, IV, cipher);
    }

    static String showHex(byte[] xs) {
        return new String(Hex.encode(xs));
    }

    public void onStart() {
        super.onStart();

        /*
         * a user will randomly generate a list of words, or enter
         * a list generated previously.  With this we'll generate
         * a key using PBKDF2, using a random salt and a fixed
         * iteration count.
         */
        String pw = "This is a test passpharse I just thought up!";
        byte[] salt = Hex.decode("233952DEE4D5ED5F9B9C6D6FF80FF478");
        byte[] K = genKey(pw, salt, 100);
        Log.v(TAG, "genkey: " + showHex(K));

        /* we'll generate a random IV */
        //byte[] K = Hex.decode("233952DEE4D5ED5F9B9C6D6FF80FF478");
        byte[] IV = Hex.decode("62EC67F9C3A4A407FCB2A8C49031A8B3");
        byte[] pt = "testing".getBytes();

        /* we'll encrypt the message and send the (IV, salt, iter count, plaintext) */
        byte[] ct = encrypt(K, IV, pt);
        Log.v(TAG, "encrypted: " + showHex(ct));

        /* The receiver will generate the same key and use it to decrypt */
        byte[] pt2 = decrypt(K, IV, ct);
        Log.v(TAG, "decrypted: " + new String(pt2));
    }
}

