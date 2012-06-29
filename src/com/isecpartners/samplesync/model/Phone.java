package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.util.List;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;

/*
 * Phone number data.
 * format: data1 - phnum, data2 - type, data3 - descr if data2 == 0
 */
public class Phone extends Data {
    public static final String MIMETYPE = CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    public static final int KIND = 1;
    public String d1, d3; // mime indirectly marshalled
    public int d2;

    public Phone() {
        super();
        mime = MIMETYPE;
        kind = KIND;
    }
        
    public Phone(String phnum, int ty, String descr) {
        this();
        d1 = phnum;
        d2 = ty;
        if(ty == 0)
            d3 = descr;
    }

    public Phone(Cursor c) {
        super(c); // zero
        mime = c.getString(1);
        d1 = c.getString(2);
        d2 = c.getInt(3);
        d3 = c.getString(4);
    }

    public void buildFields(ContentProviderOperation.Builder b) {
        b.withValue(ContactsContract.Data.MIMETYPE, mime)
            .withValue(ContactsContract.Data.DATA1, d1)
            .withValue(ContactsContract.Data.DATA2, d2)
            .withValue(ContactsContract.Data.DATA3, d3);
    }

    public String toString() {
        return "[Phone: " + mime + " " + d1 + " " + d2 + " " + d3 + "]";
    }

    public int hashCode() {
        return strhash(mime) +
            3 * strhash(d1) +
            5 * d2 +
            7 * strhash(d3);
    }

    public boolean equals(Object obj) {
        if(obj instanceof Phone) {
            Phone n = (Phone)obj;
            return streq(n.mime, mime) &&
                streq(n.d1, d1) &&
                n.d2 == d2 &&
                streq(n.d3, d3);
        }
        return false;
    }

    public void marshal(ByteBuffer buf, int version) throws Marsh.Error {
        Marsh.marshInt8(buf, kind);
        Marsh.marshString(buf, d1);
        Marsh.marshInt32(buf, d2);
        Marsh.marshString(buf, d3);
    }

    public void _unmarshal(ByteBuffer buf, int version) throws Marsh.Error {
        // kind already consumed
        d1 = Marsh.unmarshString(buf);
        d2 = Marsh.unmarshInt32(buf);
        d3 = Marsh.unmarshString(buf);
    }
}

