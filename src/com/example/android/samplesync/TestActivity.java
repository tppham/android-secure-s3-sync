package com.example.android.samplesync;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//import org.xml.sax.Attributes;
//import org.xml.sax.helpers.DefaultHandler;


public class TestActivity extends Activity {

    public static String LOG_TAG = "Goat Activity";

    /*private String errorTrace;
    private Handler mHandler;*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void createBucket() throws Exception {
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
            String bucket = "/onjava";

            // Generate signature
            StringBuffer buf = new StringBuffer();
            buf.append(method).append("\n");
            buf.append(contentMD5).append("\n");
            buf.append(contentType).append("\n");
            buf.append(date).append("\n");
            buf.append(bucket);
            String signature = sign(buf.toString());

            // Connection to s3.amazonaws.com
            HttpURLConnection httpConn = null;
            URL url = new URL("https", "s3.amazonaws.com", 443, bucket);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);
            httpConn.setDefaultUseCaches(false);
            httpConn.setAllowUserInteraction(true);
            httpConn.setRequestMethod(method);
            httpConn.setRequestProperty("Date", date);
            httpConn.setRequestProperty("Content-Length", "0");
            String AWSAuth = "AWS " + keyId + ":" + signature;
            httpConn.setRequestProperty("Authorization", AWSAuth);
            // Send the HTTP PUT request.
            int statusCode = httpConn.getResponseCode();
            
            Log.i(LOG_TAG, "Bucket created successfully, probably");
        }
        catch (Exception e) {
            setStackTrace(e);
        }
    }  

    // This method creates S3 signature for a given String.
    public String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte [] keyBytes = AWSSecretKey.getBytes("UTF8");
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        mac.init(signingKey);

        byte[] signBytes = mac.doFinal(data.getBytes("UTF8"));
        return encodeBase64(signBytes);
    }

    public String encodeBase64(byte[] data)
    {
        return Base64.encodeToString(data, 0);
    }
}

