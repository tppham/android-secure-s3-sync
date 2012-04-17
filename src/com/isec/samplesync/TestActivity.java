package com.isecpartners.samplesync;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Just for testing out our infrastructure code. Will not be used in the
 * real application.
 */
public class TestActivity extends Activity {

    static String LogTag = "TestActivity";

    // TODO XXX: Get these dynamically, probably from a Service that
    // remembers them after asking the user in a KeyInputActivity.
    static String KeyId = "Your S3 access key ID here",
                  SecretKey = "Your S3 secret access key here";


    /**
     * TODO: Make this return a dictionary appropriate for the locale.
     *
     * @return The dictionary filename, relative to the assets path.
     */
    String dictionaryFile() {
        return "basic-words-en.txt";
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        IBlobStore store = null;
        try {
            //store = new S3Client(SecretKey, KeyId);
            store = new FileBlobStore("/sdcard/dir");
        } catch (Exception e) {
            Log.e(LogTag, "error building blob store: ", e);
            return;
        }

        // Demo code to generate a random key protector passphrase.
        try {
            InputStream r = getResources().getAssets().open(dictionaryFile());
            String passphrase = Passphrase.dictionaryPassphrase(r, 5);
            r.close();

            toast(passphrase);
        }
        catch (Exception e) {
            Log.e(LogTag, "passphrase", e);
        }

        try {
            String bucket = Passphrase.hexadecimalKey(8),
                   pathname = "test-for-fun.txt";

            if (!store.create(bucket)) {
                toast("Could not create bucket " + bucket);
                return;
            }

            makeTestFile(pathname);

            if (!store.put(bucket, pathname, readFile(pathname).getBytes("UTF-8")))
            {
                toast("Could not create object " + pathname);
                return;
            }

            byte[] dat = store.get(bucket, pathname);
            if (dat == null) {
                toast("Could not get object " + pathname);
                return;
            }

            String data = new String(dat);
            toast("/" + bucket + "/" + pathname + ": '" + data + "'");
        }
        catch (Exception e) {
            Log.e(LogTag, "onCreate", e);
        }
    }


    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    String readFile(String filename) throws IOException {
        FileInputStream fin = openFileInput(filename);
        InputStreamReader isr = new InputStreamReader(fin);
        char[] inputBuffer = new char[128];
        int c = isr.read(inputBuffer);

        return new String(inputBuffer, 0, c);
    }


    // TODO: Replace this with the logic for pulling out the contacts
    void makeTestFile(String filename) throws Exception {
        try { 
            FileOutputStream fos = openFileOutput(filename, MODE_WORLD_READABLE);
            OutputStreamWriter osw = new OutputStreamWriter(fos); 

            osw.write("Hello Android");
            osw.flush();
            osw.close();
        }
        catch (IOException e) {
            Log.e(LogTag, "makeTestFile", e);
        }
    }

}

