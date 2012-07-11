package com.isecpartners.samplesync;

/*
 * Blob store interface lets you create stores, and use
 * them to store and retrieve data.
 */
public interface IBlobStore {
    public static class Error extends Exception {
        public Error(String msg) { super(msg); }
    }
    public static class AuthError extends Error {
        public AuthError(String msg) { super(msg); }
    }
    public static class IOError extends Error {
        public IOError(String msg) { super(msg); }
    }

    public boolean create(String store) throws Error;
    public byte[] get(String store, String name) throws Error;
    public boolean put(String store, String name, byte[] data) throws Error;
}

