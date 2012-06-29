package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;

/*
 * A contact set that can be read from or writtten to 
 * the remote as a sequence of bytes.
 * 
 * No synch operations are supported other than the simple
 * inherited ones.
 */
public class ContactSetBS extends ContactSet {
    public static final int VERSION = 1;

    public int version;
    public int id; // unique id, encodes the incept date

    public ContactSetBS() {
        super("remote");
        version = VERSION;
        id = (int)(System.currentTimeMillis() / 1000); // rollover is not an issue, we dont care about ordering.
    }

    public void marshal(ByteBuffer buf) throws BufferOverflowException, ReadOnlyBufferException, Marsh.Error {
        buf.putInt(VERSION); // a bit large, but might be useful slack space for future revs
        buf.putInt(id);
        Marsh.marshInt16(buf, contacts.size());
        for(Contact c : contacts)
            c.marshal(buf, version);
    }

    public static ContactSetBS unmarshal(String n, ByteBuffer buf) throws BufferUnderflowException, Marsh.Error {
        ContactSetBS cs = new ContactSetBS();
        cs.version = buf.getInt();
        if(cs.version == 1) {
            cs.id = buf.getInt();
            int cnt = Marsh.unmarshInt16(buf);
            for(int i = 0; i < cnt; i++) 
                cs.contacts.add(Contact.unmarshal(buf, cs.version));
        } else {
            throw new Marsh.BadVersion("unexpected version: " + cs.version);
        }
        return cs;
    }
}

