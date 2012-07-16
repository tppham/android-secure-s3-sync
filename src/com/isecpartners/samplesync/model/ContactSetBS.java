package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;

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

    public ContactSetBS(String name) {
        super(name);
        version = VERSION;
        id = (int)(System.currentTimeMillis() / 1000); // rollover is not an issue, we dont care about ordering.
    }

    public void marshal(ByteBuffer buf) throws Marsh.Error {
        Marsh.marshInt32(buf, version); // a bit large, but might be useful slack space for future revs
        if(version == 1) {
            Marsh.marshInt32(buf, id);
            Marsh.marshInt16(buf, contacts.size());
            for(Contact c : contacts)
                c.marshal(buf, version);
        } else {
            throw new Marsh.BadVersion("unexpected version: " + version);
        }
    }

    public static ContactSetBS unmarshal(String n, ByteBuffer buf) throws Marsh.Error {
        ContactSetBS cs = new ContactSetBS(n);
        cs.version = Marsh.unmarshInt32(buf);
        if(cs.version == 1) {
            cs.id = Marsh.unmarshInt32(buf);
            int cnt = Marsh.unmarshInt16(buf);
            for(int i = 0; i < cnt; i++) 
                cs.contacts.add(Contact.unmarshal(buf, cs.version));
        } else {
            throw new Marsh.BadVersion("unexpected version: " + cs.version);
        }
        return cs;
    }

    public String toString() {
        String s = "[ContactSetBS " + name 
                + " ver=" + version
                + " id=" + id;
        for(Contact c : contacts)
            s += " " + c;
        s += "]";
        return s;
    }
}

