package com.example.android.samplesync;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
import android.os.Bundle;
//import android.os.Handler;
import android.util.Log;
import android.util.Base64;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TestActivity extends Activity {

    public static String LOG_TAG = "Goat Activity";

    private static String keyId = "AKIAJEHY7QJUAWU2PDNA";
    private static String secretKey = "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";

    /*private String errorTrace;
      private Handler mHandler;*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.i(LOG_TAG, "start");

            S3Client.createBucket(secretKey, keyId, "blibble");
            Log.i(LOG_TAG, "createBucket");

            makeTestFile("mynewfile.txt");
            Log.i(LOG_TAG, "makeTestFile");

            S3Client.createObject(secretKey, keyId, "blibble", "mynewfile.txt",
                                  readFile("mynewfile.txt").getBytes("UTF-8"));
            Log.i(LOG_TAG, "createObject");

            S3Client.ObjectResponse r = S3Client.getObject(secretKey, keyId, "blibble", "mynewfile.txt");
            Log.i(LOG_TAG, new String(S3Client.slurpStream(r.stream)));
            Log.i(LOG_TAG, "getObject");
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "onCreate", e);
        }

        //mHandler = new Handler();
    }

    /*
       protected Runnable displayError = new Runnable() {
       public void run(){
       Log.e(LOG_TAG, errorTrace);
       AlertDialog.Builder confirm = new AlertDialog.Builder( AlertActivity.this );
       confirm.setTitle( "A Connection Error Occured!");
       confirm.setMessage( "Please Review the README\n" + errorTrace );
       confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
       public void onClick( DialogInterface dialog, int which ) {
       AlertActivity.this.finish();
       }
       } );
       confirm.show().show();   
       }
       };*/


    public String readFile(String filename) throws IOException {
        FileInputStream fin = openFileInput(filename);
        InputStreamReader isr = new InputStreamReader(fin);
        char[] inputBuffer = new char[128];
        isr.read(inputBuffer);

        Log.i(LOG_TAG, "hooray");

        return new String(inputBuffer);
    }


    // Replace this with the logic for pulling out the contacts
    public void makeTestFile(String filename) throws Exception {
        try { 
            final String TESTSTRING = new String("Hello Android");

            FileOutputStream fos = openFileOutput(filename, MODE_WORLD_READABLE);
            OutputStreamWriter osw = new OutputStreamWriter(fos); 

            osw.write(TESTSTRING);
            osw.flush();
            osw.close();
        }

        catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}

