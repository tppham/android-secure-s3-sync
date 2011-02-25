package com.example.android.samplesync;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.util.Log;
import android.util.Base64;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
                                HOSTNAME = "s3.amazonaws.com"
                                ;
    private static final int PORT = 80;


    /**
     * A tuple returned by {@see #getObject} containing both the HTTP
     * response code and the Stream of the response body.
     */
    public static class ObjectResponse {
        public int responseCode;
        public InputStream stream;

        public ObjectResponse(int responseCode, InputStream stream) {
            this.responseCode = responseCode;
            this.stream = stream;
        }
    }


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
    public static int createBucket(String key, String keyId, String bucketName)
        throws GeneralSecurityException, UnsupportedEncodingException
    {
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

            if (200 != status)
                logProblem(status, conn);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "createBucket", e);
        }

        return status;
    }


    /**
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     * @param bucketName The name of the bucket that contains the object.
     * @param objectName The name of the object to retrieve.
     * @param data A sink for the response data. getObject will only populate data if the request
     * was successful (return value 200).
     *
     * @return The HTTP status code for the request and the data stream. The
     * stream will be null on non-200 responses. See {@see #ObjectResponse}.
     */
    public static ObjectResponse getObject(String key, String keyId, String bucketName,
                                           String objectName)
        throws GeneralSecurityException, UnsupportedEncodingException
    {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "GET";
        String [] toSign = { method, "", "", date, path };
        String signature = sign(key, keyId, toSign);

        int status = 0;
        InputStream stream = null;

        try {
            HttpURLConnection conn = connection(path, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status)
                logProblem(status, conn);

            stream = 200 == status ? conn.getInputStream() : null;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "getObject", e);
        }

        return new ObjectResponse(status, stream);
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
    public static int createObject(String key, String keyId, String bucketName,
                                   String objectName, byte [] data)
        throws GeneralSecurityException, UnsupportedEncodingException
    {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "PUT";
        String contentType = "text/plain";
        String [] toSign = { method, "", contentType, date, path };
        String signature = sign(key, keyId, toSign);

        int status = 400;

        try {
            HttpURLConnection conn = connection(path, date, method, signature);

            conn.setRequestProperty("Content-Length", "" + data.length);
            conn.setRequestProperty("Content-Type", contentType);

            conn.connect();
            OutputStream out = conn.getOutputStream();
            out.write(data);
            out.flush();

            status = conn.getResponseCode();

            if (200 != status)
                logProblem(status, conn);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "createObject", e);
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
    private static String sign(String key, String keyId, String [] items)
        throws GeneralSecurityException, UnsupportedEncodingException
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < items.length; i++) {
            buf.append(items[i]);
            if (i < items.length - 1)
                buf.append("\n");
        }

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF8"), "HmacSHA1");
        mac.init(signingKey);
        byte [] sig = mac.doFinal(buf.toString().getBytes("UTF8"));

        return "AWS " + keyId + ":" + Base64.encodeToString(sig, Base64.NO_WRAP);
    }


    /**
     * @return A timestamp in the format S3 requires.
     */
    private static String timestamp() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date()) + "GMT";
    }


    static class Chunk {
        int count;
        byte [] data;

        Chunk(int c, byte [] d) {
            count = c;
            data = d;
        }
    }


    /**
     * With this code I have defiled my family's honor. My shame is eternal and
     * incalculable.
     *
     * @param stream An input stream to read all the bytes from.
     *
     * @return All the bytes read from the input stream.
     */
    public static byte [] slurpStream(InputStream stream) throws IOException {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();

        while (true) {
            byte [] d = new byte [4096];
            int c = stream.read(d);

            if (-1 == c)
                break;

            chunks.add(new Chunk(c, d));
        }

        int sz = chunks.size(),
            ttl = 0;
        for (int i = 0; i < sz; i++)
            ttl += chunks.get(i).count;

        int offst = 0;
        byte [] d = new byte [ttl];
        for (int i = 0; i < sz; i++) {
            Chunk c = chunks.get(i);
            System.arraycopy(c.data, 0, d, offst, c.count);
        }

        return d;
    }


    private static logProblem(int status, HttpURLConnection connection) {
          Log.e(LOG_TAG, "" + status + connection.getResponseMessage());
          //Log.d(LOG_TAG, new String(slurpStream(connection.getErrorStream())));
    }
 
}

