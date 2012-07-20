package com.isecpartners.samplesync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import android.util.Log;

/**
 * A simplistic blob-store using local files.
 */
public class FileStore implements IBlobStore {
    private static final String LOG_TAG = "FileStore";
    private String mDir;

    public static boolean checkStore(String dir) {
        File d = new File(dir);
        return !d.exists() || d.isDirectory();
    }

    /**
     * @param dir The directory for storing files.
     */
    public FileStore(String dir) {
        mDir = dir;
    }

    /**
     * @param store The name of the store to create.
     */
    public void create(String store) throws IBlobStore.Error {
        File d = new File(mDir, store);
        if(!d.exists() && !d.mkdir()) 
            throw new IBlobStore.IOError("Can't create bucket directory: " + d.getPath());
        if(!d.exists() || !d.isDirectory()) 
            throw new IBlobStore.IOError("Bucket directory isn't a directory: " + d.getPath());
    }

    /**
     * @param store The name of the store to test.
     */
    public boolean storeExists(String store) {
        File d = new File(mDir, store);
        return d.exists() && d.isDirectory();
    }

    /**
     * @param store The name of the store that contains the object.
     * @param name The name of the object to retrieve.
     *
     * @return The data fetched, or null.
     */
    public ByteBuffer get(String store, String name) throws IBlobStore.Error {
        try {
            File d = new File(mDir, store);
            File f = new File(d, name);
            FileInputStream r = new FileInputStream(f);
            ByteBuffer buf = ByteBuffer.allocate(r.available());
            r.getChannel().read(buf);
            r.close();
            buf.flip();
            return buf;
        } catch(final FileNotFoundException e) {
            throw new IBlobStore.NotFoundError("" + e);
        } catch (IOException e) {
            throw new IBlobStore.IOError("" + e);
        }
    }


    /**
     * @param store The name of the store in which to create the
     * object.
     * @param name The name of the object to create.
     * @param data The data to store in the object.
     */
    public void put(String store, String name, ByteBuffer data) throws IBlobStore.Error {
        try {
            File d = new File(mDir, store);
            File f = new File(d, name);
            FileOutputStream w = new FileOutputStream(f);
            try {
                w.getChannel().write(data);
            } finally {
                w.close();
            }
        } catch(final IOException e) {
            throw new IBlobStore.IOError("" + e);
        }
    }
}

