package com.isecpartners.samplesync;

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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * A very simple Amazon S3 client.
 */
public class S3Client implements IBlobStore {

    private static final String LOG_TAG = "S3Client";

    private static final String PROTOCOL = "http",
                                HOSTNAME = "s3.amazonaws.com"
                                ;
    private static final int PORT = 80;

    private String mKey, mKeyId;

    /*
     * @param key The secret S3 key.
     * @param keyId The secret S3 key ID.
     */
    public S3Client(String key, String keyId) {
        mKey = key;
        mKeyId = keyId;
    }


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
     * @param bucketName The name of the bucket to create.
     *
     * @return True for success.
     */
    public boolean create(String bucketName)
    {
        int status = 400;
        String path = "/" + bucketName;
        String date = timestamp();
        String method = "PUT";
        try {
            String [] toSign = { method, "", "", date, path };
            String signature = sign(toSign);

            HttpURLConnection conn = connection(path, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            status = conn.getResponseCode();

            if (200 != status)
                logProblem(status, conn);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "create", e);
        }

        return status == 200;
    }


    /**
     * @param bucketName The name of the bucket that contains the object.
     * @param objectName The name of the object to retrieve.
     *
     * @return The data fetched, or null.
     */
    public byte[] get(String bucketName, String objectName)
    {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "GET";
        byte[] dat = null;

        try {
            String [] toSign = { method, "", "", date, path };
            String signature = sign(toSign);

            HttpURLConnection conn = connection(path, date, method, signature);
            conn.setRequestProperty("Content-Length", "0");
            int status = conn.getResponseCode();

            if (200 != status)
                logProblem(status, conn);

            if (200 == status) {
                InputStream stream = conn.getInputStream();
                dat = Utils.readAll(stream);
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "get", e);
        }
        return dat;
    }


    /**
     * @param bucketName The name of the bucket in which to create the
     * object.
     * @param objectName The name of the object to create.
     * @param data The data to store in the object.
     *
     * @return True on success.
     */
    public boolean put(String bucketName, String objectName, byte [] data)
    {
        String path = "/" + bucketName + "/" + objectName;
        String date = timestamp();
        String method = "PUT";
        String contentType = "text/plain";
        int status = 400;

        try {
            String [] toSign = { method, "", contentType, date, path };
            String signature = sign(toSign);

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
        return 200 == status;
    }


    /**
     * @param items The data items that Amazon S3 requests require to be
     * signed. Should be an array of length 5: { method, MD5, content-type,
     * timestamp, path }.
     *
     * @return A signature in the format S3 requires.
     */
    private String sign(String [] items)
        throws GeneralSecurityException, UnsupportedEncodingException
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < items.length; i++) {
            buf.append(items[i]);
            if (i < items.length - 1)
                buf.append("\n");
        }

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec signingKey = new SecretKeySpec(mKey.getBytes("UTF8"), "HmacSHA1");
        mac.init(signingKey);
        byte [] sig = mac.doFinal(buf.toString().getBytes("UTF8"));

        return "AWS " + mKeyId + ":" + Base64.encodeToString(sig, Base64.NO_WRAP);
    }

    /**
     * @return A timestamp in the format S3 requires.
     */
    private static String timestamp() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date()) + "GMT";
    }


    private void logProblem(int status, HttpURLConnection connection) throws IOException {
          Log.e(LOG_TAG, "" + status + connection.getResponseMessage());
          //Log.d(LOG_TAG, new String(slurpStream(connection.getErrorStream())));
    }
}

