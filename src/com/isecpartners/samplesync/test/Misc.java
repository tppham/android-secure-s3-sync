package com.isecpartners.samplesync.test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.Passphrase;
import com.isecpartners.samplesync.FileStore;


/**
 * Just for testing out our infrastructure code. Will not be used in the
 * real application.
 */
public class Misc extends Activity {

    static String LogTag = "test.Misc";

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
    }

    public void onStart() {
        super.onStart();

        IBlobStore store = null;
        try {
            //store = new com.isecpartners.samplesync.s3.Store(SecretKey, KeyId);
            store = new FileStore("/sdcard/dir");
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

            try {
                store.create(bucket);
            } catch(IBlobStore.Error e) {
                toast("Could not create bucket " + bucket);
                return;
            }

            makeTestFile(pathname);

            try {
                store.put(bucket, pathname, readFile(pathname));
            } catch(IBlobStore.Error e) {
                toast("Could not create object " + pathname);
                return;
            }

            ByteBuffer dat;
            try {
                dat = store.get(bucket, pathname);
            } catch(IBlobStore.Error e) {
                toast("Could not get object " + pathname);
                return;
            }

            String data = new String(dat.array(), 0, dat.limit());
            toast("/" + bucket + "/" + pathname + ": '" + data + "'");
        }
        catch (Exception e) {
            Log.e(LogTag, "onCreate", e);
        }
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    ByteBuffer readFile(String filename) throws IOException {
        FileInputStream fin = openFileInput(filename);
        try {
            ByteBuffer buf = ByteBuffer.allocate(fin.available());
            fin.getChannel().read(buf);
            return buf;
        } finally {
            fin.close();
        }
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

