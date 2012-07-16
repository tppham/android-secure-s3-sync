package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

import android.util.Log;

import org.spongycastle.util.encoders.Hex;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;

/**
 * Encryption primtives.
 * Assumes the spongycastle provider has already been installed.
 */
public class Crypto {
    static String TAG = "model.Crypto";

    public static final SecureRandom rand = new SecureRandom();

    /* generate a key from passwd */
    public static byte[] genKey(String passwd, byte[] salt, int iters) {
        PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
        byte[] pwb = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(passwd.toCharArray());
        g.init(pwb, salt, iters);
        KeyParameter ps = (KeyParameter)g.generateDerivedParameters(256);
        return ps.getKey();
    }

    /* return an array filled with random bytes */
    public static byte[] randArray(int cnt) {
        byte bs[] = new byte[cnt];
        rand.nextBytes(bs);
        return bs;
    }

    /* generate a random salt */
    public static byte[] genSalt() {
        return randArray(8); // 64 bits
    }

    /* generate a random IV */
    public static byte[] genIV() {
        return randArray(16); // 128 bits
    }

    /* internal helper - encrypt or decrypt */
    protected static byte[] crypt(int opmode, byte[] K, byte[] IV, byte[] x) {
        Key k = new SecretKeySpec(K, "AES");
        IvParameterSpec iv = new IvParameterSpec(IV);
        try {
            Cipher c = Cipher.getInstance("AES/EAX/NoPadding", "SC");
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

    /* Return a hex string from byte array */
    public static String hex(byte[] xs) {
        return new String(Hex.encode(xs));
    }

    /* return a hex string from byte buffer */
    public static String hex(ByteBuffer buf) {
        return new String(Hex.encode(buf.array(), 0, buf.limit()));
    }
}

