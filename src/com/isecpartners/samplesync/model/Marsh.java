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
    public static class Error extends Exception { 
        public Error(String msg) { super(msg); }
    }
    public static class BufferError extends Error {
        public BufferError(String msg) { super(msg); }
    }
    public static class BadFormat extends Error { 
        public BadFormat(String msg) { super(msg); }
    }
    public static class BadData extends Error { 
        public BadData(String msg) { super(msg); }
    }
    public static class BadVersion extends Error { 
        public BadVersion(String msg) { super(msg); }
    }

    public static void unmarshEof(ByteBuffer buf) throws Error {
        if(buf.hasRemaining())
            throw new Marsh.BadFormat("" + buf.remaining() + "extra bytes found");
    }

    public static void marshInt8(ByteBuffer buf, int x) throws Error {
        if(x < 0 || x > 0xff)
            throw new BadData("int8 is too large: " + x);
        try {
            buf.put((byte)x);
        } catch(final BufferOverflowException e) {
            throw new BufferError("not enough room");
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
            throw new BufferError("not enough data");
        }
    }

    public static void marshInt16(ByteBuffer buf, int x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        if(x < 0 || x > 0xffff)
            throw new BadData("int16 is too large: " + x);
        try {
            buf.putShort((short)x);
        } catch(final BufferOverflowException e) {
            throw new BufferError("not enough room");
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
            throw new BufferError("not enough data");
        }
    }

    public static void marshInt32(ByteBuffer buf, int x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            buf.putInt(x);
        } catch(final BufferOverflowException e) {
            throw new BufferError("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static int unmarshInt32(ByteBuffer buf) throws Error, BufferUnderflowException {
        try {
            return buf.getInt();
        } catch(final BufferUnderflowException e) {
            throw new BufferError("not enough data");
        }
    }

    public static void marshString(ByteBuffer buf, String s) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            if(s == null) {
                marshInt16(buf, 0);
                return;
            }
            byte[] bs = s.getBytes("UTF-8");
            if(bs.length > 0xffff)
                throw new BadData("string is too long");
            marshInt16(buf, bs.length);
            buf.put(bs);
        } catch(final UnsupportedEncodingException e) {
            throw new Error("utf8 error1 should never happen");
        } catch(final BufferOverflowException e) {
            throw new BufferError("not enough room");
        } catch(final ReadOnlyBufferException e) {
            throw new Error("" + e);
        }
    }

    public static String unmarshString(ByteBuffer buf) throws Error, BufferUnderflowException {
        int len = unmarshInt16(buf);
        if(len == 0) // XXX this worries me.  we have no way to distinguish null and empty string right now.  this might bite us at some point.  I'm hoping that we never have empty non-null strings in our data sets and we can ignore this.
            return null;
        byte[] bs = new byte[len];
        buf.get(bs);
        try {
            return new String(bs, "UTF-8");
        } catch(final UnsupportedEncodingException e) {
            throw new Error("utf8 error2 should never happen");
        } catch(final BufferUnderflowException e) {
            throw new BufferError("not enough data");
        }
    }
};
