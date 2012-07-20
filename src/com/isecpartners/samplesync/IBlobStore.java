package com.isecpartners.samplesync;

import java.nio.ByteBuffer;

/*
 * Blob store interface lets you create stores, and use
 * them to store and retrieve data.
 */
public interface IBlobStore {
    public abstract static class Error extends com.isecpartners.samplesync.Error {
        public Error(String msg) { super(msg); }
    }
    public static class AuthError extends Error {
        public AuthError(String msg) { super(msg); }
        public String descr() { return "incorrect storage credentials"; }
    }
    public static class NotFoundError extends Error {
        public NotFoundError(String msg) { super(msg); }
        public String descr() { return "data not found"; }
    }
    public static class IOError extends Error {
        public IOError(String msg) { super(msg); }
        public String descr() { return "an IO error occured"; }
    }

    public void create(String store) throws Error;
    public boolean storeExists(String store) throws Error;
    public ByteBuffer get(String store, String name) throws Error;
    public void put(String store, String name, ByteBuffer data) throws Error;

}

