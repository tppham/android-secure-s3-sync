package com.isecpartners.samplesync.model;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.content.ContentProviderOperation;

import java.util.List;

/*
 * Contact name data.
 * Uses data1 through data9, all strings for different names.
 * data1 = display, data2,data3 = first,last.
 */
public class Name extends Data {
    public static final String mimeType = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    public long sid;
    public String mime, d1, d2, d3, d4, d5, d6, d7, d8, d9;

    public Name(String first, String last) {
        mime = mimeType;
        d1 = first + " " + last;
        d2 = first;
        d3 = last;
    }

    public Name(Cursor c) {
        sid = c.getLong(0);
        mime = c.getString(1);
        d1 = c.getString(2);
        d2 = c.getString(3);
        d3 = c.getString(4);
        d4 = c.getString(5);
        d5 = c.getString(6);
        d6 = c.getString(7);
        d7 = c.getString(8);
        d8 = c.getString(9);
        d9 = c.getString(10);
    }

    public void put(List<ContentProviderOperation> ops, boolean back, int id) {
        ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        if(back)
            b.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, id);
        else
            b.withValue(ContactsContract.Data.RAW_CONTACT_ID, id);
        ops.add(b.withValue(ContactsContract.Data.MIMETYPE, mime)
                    .withValue(ContactsContract.Data.DATA1, d1)
                    .withValue(ContactsContract.Data.DATA2, d2)
                    .withValue(ContactsContract.Data.DATA3, d3)
                    .withValue(ContactsContract.Data.DATA4, d4)
                    .withValue(ContactsContract.Data.DATA5, d5)
                    .withValue(ContactsContract.Data.DATA6, d6)
                    .withValue(ContactsContract.Data.DATA7, d7)
                    .withValue(ContactsContract.Data.DATA8, d8)
                    .withValue(ContactsContract.Data.DATA9, d9)
                    .withValue(ContactsContract.Data.SYNC1, sid)
                    .build());
    }

    public String toString() {
        return "[Name: " + sid + " " + mime + " " + d1 + " " + d2 + " " + d3 + d4 + " " + d5 + " " + d6 + " " + d7 + " " + d8 + " " + d9 + "]";
    }

    public int hashCode() {
        return strhash(mime) +
            3 * strhash(d1) +
            5 * strhash(d2) +
            7 * strhash(d3) +
            9 * strhash(d4) +
            11 * strhash(d5) +
            13 * strhash(d6) +
            17 * strhash(d7) +
            19 * strhash(d8) +
            23 * strhash(d9);
    }

    public boolean equals(Object obj) {
        if(obj instanceof Name) {
            Name n = (Name)obj;
            return streq(n.mime, mime) &&
                streq(n.d1, d1) &&
                streq(n.d2, d2) &&
                streq(n.d3, d3) &&
                streq(n.d4, d4) &&
                streq(n.d5, d5) &&
                streq(n.d6, d6) &&
                streq(n.d7, d7) &&
                streq(n.d8, d8) &&
                streq(n.d9, d9);
        }
        return false;
    }
}

