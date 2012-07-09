package com.isecpartners.samplesync.sdcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.util.Log;

import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.Utils;


/**
 * A simplistic blob-store using local files.
 */
public class Store implements IBlobStore {
    private static final String LOG_TAG = "FileBlobStore";
    private String mDir;

    public static boolean checkStore(String dir) {
        File d = new File(dir);
        return !d.exists() || d.isDirectory();
    }

    /**
     * @param dir The directory for storing files.
     */
    public Store(String dir) {
        mDir = dir;
    };

    /**
     * @param store The name of the store to create.
     *
     * @return True for success.
     */
    public boolean create(String store)
    {
        try {
            File d = new File(mDir, store);
            if(!d.exists() && !d.mkdir()) {
                Log.e(LOG_TAG, "mkdir failed " + store);
                return false;
            }
            if(!d.exists() || !d.isDirectory()) {
                Log.e(LOG_TAG, "not a directory " + store);
                return false;
            }
            return true;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "create " + store, e);
            return false;
        }
    }

    /**
     * @param store The name of the store that contains the object.
     * @param name The name of the object to retrieve.
     *
     * @return The data fetched, or null.
     */
    public byte[] get(String store, String name)
    {
        try {
            File d = new File(mDir, store);
            File f = new File(d, name);
            FileInputStream r = new FileInputStream(f);
            byte[] dat = Utils.readAll(r);
            r.close();
            return dat;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "get " + name, e);
            return null;
        }
    }


    /**
     * @param store The name of the store in which to create the
     * object.
     * @param name The name of the object to create.
     * @param data The data to store in the object.
     *
     * @return True on success.
     */
    public boolean put(String store, String name, byte [] data)
    {
        FileOutputStream w = null;
        try {
            File d = new File(mDir, store);
            File f = new File(d, name);
            w = new FileOutputStream(f);
            w.write(data);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "put " + name, e);
            return false;
        } finally {
            if(w != null) try { w.close(); } catch(final Exception e) {};
        }
    }
}

