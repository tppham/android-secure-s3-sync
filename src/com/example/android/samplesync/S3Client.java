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
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * TODO Javadoc
 */
public class S3Client {

    private static final String LOG_TAG = "S3Client";

    private static final String PROTOCOL = "http",
                          HOSTNAME = "s3.amazon.com"
                          ;
    private static final int PORT = 80;


    /**
     * @param uri
     * @param date
     * @param method
     * @param signature
     *
     * @return A connection.
     */
    private static HttpURLConnection connection(String uri, String date, String method,
                           String signature)
        throws MalformedURLException, IOException
    {
        URL url = new URL(PROTOCOL, HOSTNAME, PORT, uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);
        conn.setAllowUserInteraction(true);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Date", date);
        conn.setRequestProperty("Authorization", signature);

        return conn;
    }


    /**
     * TODO Javadoc
     */
    public static int createBucket(String key, String keyId, String bucketName) {
        String uri = "/" + bucketName;
        String date = timestamp();
        String method = "PUT";
        String signature = sign(keyId, (String []) { method, "", "", date, uri });

        int status;

        try {
            HttpURLConnection = connection(uri, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } else
                Log.i(LOG_TAG, "Bucket created successfully, probably");
        }
        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }

        return status;
    }


    /**
     * TODO Javadoc
     */
    public static int getObject(String key, String keyId, String bucketName, String objectName) {
        String uri = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "GET";
        String signature = sign(keyId, (String []) { method, "", "", date, uri });

        int status;

        try {
            HttpURLConnection = connection(uri, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } else
                Log.i(LOG_TAG, "Here is your object, probably");
        }
        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }

        return status;
    }


    /**
     * TODO Javadoc
     */
    public static int createObject(String key, String keyId, String bucketName, String objectName, byte [] data) {
        String uri = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "PUT";
        String contentType = "text/plain";
        String signature = sign(keyId, (String []) { method, "", contentType, date, uri });

        int status;

        try {
            HttpURLConnection = connection(uri, date, method, signature);

            String body = "data=" + readFile(objectName);

            conn.setRequestProperty("Content-Length", body.getBytes("UTF-8").length);
            conn.setRequestProperty("Content-Type", contentType);
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + statusCode);
                String resp = httpConn.getResponseMessage();
                Log.i(LOG_TAG, resp);
            } else
                Log.i(LOG_TAG, "Object stored, probably");
        }
        catch (Exception e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
        }

        return status;
    }


    /**
     * @param items TODO explain
     *
     * @return A signature in the format S3 requires.
     */
    private static String sign(String keyId, String [] items) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < items.length; i++) {
            buf.append(items[i]);
            if (i < items.length - 1)
                buf.append("\n");
        }

        return "AWS " + keyId + ":" + buf.toString();
    }


    /**
     * @return A timestamp in the format S3 requires.
     */
    private static String timestamp() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date() + "GMT");
    }


    private static String readFile(String filename) throws Exception {
        try {
            FileInputStream fin = new FileInputStream(filename);
            byte [] buf = new byte [4096];
            fin.read(buf);
            fin.close();

            Log.d(LOG_TAG, "hooray");

            return new String(buf, "UTF-8");
        }
        catch (IOException e) {
            setStackTrace(e);
            Log.e(LOG_TAG, e.getMessage());
            return new String("darn.");
        }
    }

    private static void setStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        Log.i(LOG_TAG, result.toString());
    }
    
 
}

