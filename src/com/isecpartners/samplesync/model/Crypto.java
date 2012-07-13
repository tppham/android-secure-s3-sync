package com.isecpartners.samplesync.model;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import android.util.Log;

import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;

/**
 * Encryption primtives.
 * Assumes the spongycastle provider has already been installed.
 */
public class Crypto {
    static String TAG = "model.Crypto";

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
}

