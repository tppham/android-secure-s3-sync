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
 * A very simple Amazon S3 client.
 */
public class S3Client {

    private static final String LOG_TAG = "S3Client";

    private static final String PROTOCOL = "http",
                          HOSTNAME = "s3.amazon.com"
                          ;
    private static final int PORT = 80;


    /**
     * @param path The path component of the URL to connect to (e.g.
     * "/bucket/object/name").
     * @param date A timestamp ({@see #timestamp}).
     * @param method The HTTP method, e.g. "PUT" or "GET".
     * @param signature The signature ({@see #sign}).
     *
     * @return A connection.
     */
    private static HttpURLConnection connection(String path, String date, String method,
                                                String signature)
        throws MalformedURLException, IOException
    {
        URL url = new URL(PROTOCOL, HOSTNAME, PORT, path);
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
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     * @param bucketName The name of the bucket to create.
     *
     * @return The HTTP status code for the request.
     */
    public static int createBucket(String key, String keyId, String bucketName) {
        String path = "/" + bucketName;
        String date = timestamp();
        String method = "PUT";
        String [] toSign = { method, "", "", date, path };
        String signature = sign(key, keyId, toSign);

        int status = 400;

        try {
            HttpURLConnection conn = connection(path, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + status);
                String resp = conn.getResponseMessage();
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
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     * @param bucketName The name of the bucket that contains the object.
     * @param objectName The name of the object to retrieve.
     *
     * @return The HTTP status code for the request.
     */
    public static int getObject(String key, String keyId, String bucketName, String objectName) {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "GET";
        String [] toSign = { method, "", "", date, path };
        String signature = sign(key, keyId, toSign);

        int status = 400;

        try {
            HttpURLConnection conn = connection(path, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + status);
                String resp = conn.getResponseMessage();
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
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     * @param bucketName The name of the bucket in which to create the
     * object.
     * @param objectName The name of the object to create.
     * @param data The data to store in the object.
     *
     * @return The HTTP status code for the request.
     */
    public static int createObject(String key, String keyId, String bucketName, String objectName, byte [] data) {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "PUT";
        String contentType = "text/plain";
        String [] toSign = { method, "", contentType, date, path };
        String signature = sign(key, keyId, toSign);

        int status = 400;

        try {
            HttpURLConnection conn = connection(path, date, method, signature);

            String body = "data=" + readFile(objectName);

            conn.setRequestProperty("Content-Length", "" + body.getBytes("UTF-8").length);
            conn.setRequestProperty("Content-Type", contentType);
            status = conn.getResponseCode();

            if (200 != status) {
                Log.i(LOG_TAG, "Status is " + status);
                String resp = conn.getResponseMessage();
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
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     * @param items The data items that Amazon S3 requests require to be
     * signed. Should be an array of length 5: { method, MD5, content-type,
     * timestamp, path }.
     *
     * @return A signature in the format S3 requires.
     */
    private static String sign(String key, String keyId, String [] items) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < items.length; i++) {
            buf.append(items[i]);
            if (i < items.length - 1)
                buf.append("\n");
        }

        // TODO: Sign here!

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


    /**
     * @param pathname A relative or absolute filesystem pathname.
     *
     * @return The contents of the named file as a UTF-8 string.
     */
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


    /**
     * Logs a stack trace to the Android log.
     *
     * @param t The source of the stack trace.
     */
    private static void setStackTrace(Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        Log.d(LOG_TAG, result.toString());
    }
 
}

