package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;
import java.io.UnsupportedEncodingException;


/* 
 * some helpers for marshalling.
 */
public class Marsh {
    public static class Error extends Exception { 
        public Error(String msg) { super(msg); }
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

    public static void marshInt16(ByteBuffer buf, int x) throws Error, BufferOverflowException, ReadOnlyBufferException {
        if(x < 0 || x > 0xffff)
            throw new BadData("int16 is too large: " + x);
        buf.putShort((short)x);
    }

    public static int unmarshInt16(ByteBuffer buf) throws Error, BufferUnderflowException {
        int x = buf.getInt();
        if(x < 0) // treat it as unsigned
            x += 0x10000;
        return x;
    }

    public static void marshString(ByteBuffer buf, String s) throws Error, BufferOverflowException, ReadOnlyBufferException {
        try {
            byte[] bs = s.getBytes("UTF-8");
            if(bs.length > 0xffff)
                throw new BadData("string is too long");
            marshInt16(buf, bs.length);
            buf.put(bs);
        } catch(UnsupportedEncodingException e) {
            throw new Error("utf8 error1 should never happen");
        }
    }

    public static String unmarshString(ByteBuffer buf) throws Error, BufferUnderflowException {
        int len = unmarshInt16(buf);
        byte[] bs = new byte[len];
        buf.get(bs);
        try {
            return new String(bs, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new Error("utf8 error2 should never happen");
        }
    }
};

