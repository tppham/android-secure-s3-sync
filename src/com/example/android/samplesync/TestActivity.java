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

    private static String keyId = "1V8B0Z36A2D2M4Q89Z02";
    private static String secretKey = "u66fKmyC0fOcappL6qLapfvMOxe8ZwrNzoj29bRL";

    /*private String errorTrace;
      private Handler mHandler;*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{ 
            //createBucket("onblitzen");
            makeTestFile("mynewfile.txt");
            createObject("onblitzen", "mynewfile.txt");
            getObject("onblitzen", "mynewfile.txt");
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
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

    protected void setStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        Log.i(LOG_TAG, result.toString());
    }

    public void createBucket(String bucket) throws Exception {
        try {
            // S3 timestamp pattern.
            String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
            SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            // Data needed for signature
            String method = "PUT";
            String contentMD5 = "";
            String contentType = "";
            String date = df.format(new Date()) + "GMT";

            // Generate signature
            StringBuffer buf = new StringBuffer();
            buf.append(method).append("\n");
            buf.append(contentMD5).append("\n");
            buf.append(contentType).append("\n");
            buf.append(date).append("\n");
            buf.append("/" + bucket);
            String signature = sign(buf.toString());
            String AWSAuth = "AWS " + keyId + ":" + signature;

            // Connection to s3.amazonaws.com
            HttpURLConnection httpConn = null;
            URL url = new URL("http", "s3.amazonaws.com", 80, "/" + bucket);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);
            httpConn.setDefaultUseCaches(false);
            httpConn.setAllowUserInteraction(true);
            httpConn.setRequestMethod(method);
            httpConn.setRequestProperty("Date", date);
            httpConn.setRequestProperty("Content-Length", "0");
            httpConn.setRequestProperty("Authorization", AWSAuth);
            // Send the HTTP PUT request.
            int statusCode = httpConn.getResponseCode();

            if ((statusCode/100) != 2)
            {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } 
            else 
            {
                Log.i(LOG_TAG, "Bucket created successfully, probably");
            }
        }

        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public void createObject(String bucket, String object) throws Exception {
        try{
            // S3 timestamp pattern.
            String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
            SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            // Data needed for signature
            String method = "PUT";
            String contentMD5 = "";
            String contentType = "text/plain";
            String date = df.format(new Date()) + "GMT";

            // Generate signature
            StringBuffer buf = new StringBuffer();
            buf.append(method).append("\n");
            buf.append(contentMD5).append("\n");
            buf.append(contentType).append("\n");
            buf.append(date).append("\n");
            buf.append("/" + bucket + "/" + object);
            String signature = sign(buf.toString());
            String AWSAuth = "AWS " + keyId + ":" + signature;

            // Read in the file we're going to put
            String body = "data=" + readFile(object);

            // Connection to s3.amazonaws.com
            HttpURLConnection httpConn = null;
            URL url = new URL("http", "s3.amazonaws.com", 80, "/" + bucket + "/" + object);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);
            httpConn.setDefaultUseCaches(false);
            httpConn.setAllowUserInteraction(true);
            httpConn.setRequestMethod(method);
            httpConn.setRequestProperty("Date", date);
            httpConn.setRequestProperty("Authorization", AWSAuth);
            httpConn.setRequestProperty("Content-Type", "text/plain");
            httpConn.setRequestProperty("Expect", "100-continue");
            // TODO: Integrity check
            // Tack on the object itself
            byte bytes[] = body.getBytes();
            httpConn.setRequestProperty("Content-length", "" + bytes.length);
            httpConn.connect();
            OutputStream out = httpConn.getOutputStream();  
            out.write(bytes);  
            out.flush();  
            
            // Send the HTTP PUT request.
            int statusCode = httpConn.getResponseCode();

            if ((statusCode/100) != 2)
            {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } 
            else 
            {
                Log.i(LOG_TAG, "Object created successfully, probably");
            }
        }

        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public void getObject(String bucket, String object) throws Exception {
        try{
            // S3 timestamp pattern.
            String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
            SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            // Data needed for signature
            String method = "GET";
            String contentMD5 = "";
            String contentType = "";
            String date = df.format(new Date()) + "GMT";

            // Generate signature
            StringBuffer buf = new StringBuffer();
            buf.append(method).append("\n");
            buf.append(contentMD5).append("\n");
            buf.append(contentType).append("\n");
            buf.append(date).append("\n");
            buf.append("/" + bucket + "/" + object);
            String signature = sign(buf.toString());
            String AWSAuth = "AWS " + keyId + ":" + signature;

            // Connection to s3.amazonaws.com
            HttpURLConnection httpConn = null;
            URL url = new URL("http", "s3.amazonaws.com", 80, "/" + bucket + "/" + object);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);
            httpConn.setDefaultUseCaches(false);
            httpConn.setAllowUserInteraction(true);
            httpConn.setRequestMethod(method);
            httpConn.setRequestProperty("Date", date);
            httpConn.setRequestProperty("Authorization", AWSAuth);
            int statusCode = httpConn.getResponseCode();

            if ((statusCode/100) != 2)
            {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } 
            else 
            {
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, "We read an object and didn't get an error:" + resp);
            }
        }

        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    // This method creates S3 signature for a given String.
    public String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        byte [] keyBytes = secretKey.getBytes("UTF8");
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        mac.init(signingKey);

        byte[] signBytes = mac.doFinal(data.getBytes("UTF8"));
        return encodeBase64(signBytes);
    }

    public String encodeBase64(byte[] data)
    {
        return Base64.encodeToString(data, 2);
    }

    public String readFile(String filename) throws Exception {
        try {
            FileInputStream fin = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fin);
            char[] inputBuffer = new char[128];
            isr.read(inputBuffer);

            Log.i(LOG_TAG, "hooray");

            return new String(inputBuffer);
        }

        catch (IOException ioe) {
            setStackTrace(ioe);
            Log.e(LOG_TAG, ioe.getMessage());
            return new String("darn.");
        }
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
