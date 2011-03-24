package com.example.android.samplesync;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Utility methods to generate passphrases.
 */
public class Passphrase {

    static final int RandomSize = 32;
    static final String RandomPathname = "/dev/urandom";


    static SecureRandom getSecureRandom() throws GeneralSecurityException, IOException {
        SecureRandom r = new SecureRandom();

        // We cannot trust SecureRandom's default behavior; at least one
        // implementation simply does SHA1 on the current time in
        // milliseconds. Truly, the Earthlings live in darkness. So we have
        // to do manually what the implementation should do itself:
        byte [] seed = new byte [RandomSize];
        FileInputStream fis = new FileInputStream(RandomPathname);

        if (RandomSize != fis.read(seed))
            throw new IOException("Could not read " + RandomPathname);

        r.setSeed(seed);

        fis.close();
        return r;
    }


    static ArrayList<String> getShuffledWordList(InputStream input)
        throws IOException, GeneralSecurityException
    {
        ArrayList<String> words = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String s = reader.readLine();
            if (null == s)
                break;

            words.add(s);
        }
        reader.close();

        Collections.shuffle(words, getSecureRandom());

        return words;
    }


    /**
     * @param input A reader for a dictionary from which to pull words.
     * @param wordCount The number of words to select from the dictionary.
     *
     * @return A space-separated sequence of wordCount words randomly
     * selected from the dictionary.  The selection is made using
     * SecureRandom seeded with data from the operating system's random byte
     * device.
     */
    public static String dictionaryPassphrase(InputStream input, int wordCount)
        throws IOException, GeneralSecurityException
    {
        ArrayList<String> words = getShuffledWordList(input);
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < wordCount; i++) {
            sb.append(words.get(i));
            if (i < wordCount - 1)
                sb.append(" ");
        }

        return sb.toString();
    }

}

