package com.example.android.samplesync;

/*
 * Blob store interface lets you create stores, and use
 * them to store and retrieve data.
 */
public interface IBlobStore {
    public boolean create(String store);
    public byte[] get(String store, String name);
    public boolean put(String store, String name, byte[] data);
}

