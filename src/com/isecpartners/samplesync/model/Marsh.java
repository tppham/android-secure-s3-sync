package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;
import java.io.UnsupportedEncodingException;

/* 
 * Some helpers for marshalling.
 *
 * The ByteBuffer methods are mostly sane except for their
 * unstructured exceptions, so we wrap all the marshallers here
 * to provide a simpler interface.  All exceptions thrown are
 * a subclass of the Error class here.
 */
public class Marsh {
    public static class Error extends com.isecpartners.samplesync.Error {
        public Error(String msg) { super(msg); }
        public String descr() { return "internal error processing data"; }
    }
    public static class BadFormat extends Error { 
        public BadFormat(String msg) { super(msg); }
        public String descr() { return "bad data format"; }
    }
    public static class BadData extends Error { 
        public BadData(String msg) { super(msg); }
        public String descr() { return "internal error encoding bad data"; }
    }
    public static class BadVersion extends Error { 
        public BadVersion(String msg) { super(msg); }
        public String descr() { return "unsupported version number in data"; }
    }
    public static class BadKey extends Error { 
        public BadKey(String msg) { super(msg); }
        public String descr() { return "incorrect passpharse"; }
    }

    public static void unmarshEof(ByteBuffer buf) throws Error {
        if(buf.hasRemaining())
            throw new Marsh.BadFormat("" + buf.remaining() + "extra bytes found");
    }

    public static void marshBytes(ByteBuffer buf, byte[] bs, int len) throws Error {
        if(bs.length != len)
            throw new Marsh.BadData("buflen " + bs.length + " != " + len);
        buf.put(bs);
    }

    public static byte[] unmarshBytes(ByteBuffer buf, int len) throws Error {
        byte[] bs = new byte[len];
        buf.get(bs);
        return bs;
    }

    public static void marshInt8(ByteBuffer buf, int x) throws Error {
        if(x < 0 || x > 0xff)
            throw new BadData("int8 is too large: " + x);
        try {
            buf.put((byte)x);
        } catch(final BufferOverflowException e) {
            throw new BadFormat("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static int unmarshInt8(ByteBuffer buf) throws Error, BufferUnderflowException {
        try {
            int x = buf.get();
            if(x < 0) // treat it as unsigned
                x += 0x100;
            return x;
        } catch(final BufferUnderflowException e) {
            throw new BadFormat("not enough data");
        }
    }

    public static void marshInt16(ByteBuffer buf, int x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        if(x < 0 || x > 0xffff)
            throw new BadData("int16 is too large: " + x);
        try {
            buf.putShort((short)x);
        } catch(final BufferOverflowException e) {
            throw new BadFormat("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static int unmarshInt16(ByteBuffer buf) throws Error, BufferUnderflowException {
        try {
            int x = buf.getShort();
            if(x < 0) // treat it as unsigned
                x += 0x10000;
            return x;
        } catch(final BufferUnderflowException e) {
            throw new BadFormat("not enough data");
        }
    }

    public static void marshInt32(ByteBuffer buf, int x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            buf.putInt(x);
        } catch(final BufferOverflowException e) {
            throw new BadFormat("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static int unmarshInt32(ByteBuffer buf) throws Error, BufferUnderflowException {
        try {
            return buf.getInt();
        } catch(final BufferUnderflowException e) {
            throw new BadFormat("not enough data");
        }
    }

    public static void marshInt64(ByteBuffer buf, long x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            buf.putLong(x);
        } catch(final BufferOverflowException e) {
            throw new BadFormat("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static long unmarshInt64(ByteBuffer buf) throws Error, BufferUnderflowException {
        try {
            return buf.getLong();
        } catch(final BufferUnderflowException e) {
            throw new BadFormat("not enough data");
        }
    }

    public static void marshString(ByteBuffer buf, String s) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            if(s == null) {
                marshInt16(buf, 0xffff);
                return;
            }
            byte[] bs = s.getBytes("UTF-8");
            if(bs.length > 0xfffe)
                throw new BadData("string is too long");
            marshInt16(buf, bs.length);
            buf.put(bs);
        } catch(final UnsupportedEncodingException e) {
            throw new Error("utf8 error1 should never happen");
        } catch(final BufferOverflowException e) {
            throw new BadFormat("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static String unmarshString(ByteBuffer buf) throws Error, BufferUnderflowException {
        int len = unmarshInt16(buf);
        if(len == 0xffff)
            return null;
        if(len == 0)
            return new String("");
        byte[] bs = new byte[len];
        buf.get(bs);
        try {
            return new String(bs, "UTF-8");
        } catch(final UnsupportedEncodingException e) {
            throw new Error("utf8 error2 should never happen");
        } catch(final BufferUnderflowException e) {
            throw new BadFormat("not enough data");
        }
    }
};

