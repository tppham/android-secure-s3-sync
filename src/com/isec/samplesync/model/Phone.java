package com.isecpartners.samplesync.model;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import java.util.List;

/*
 * Phone number data.
 * format: data1 - phnum, data2 - type, data3 - descr if data2 == 0
 */
public class Phone extends Data {
    public long sid;
    public String mime, d1, d3;
    public int d2;

    public Phone(String phnum, int ty, String descr) {
        mime = CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        d1 = phnum;
        d2 = ty;
        if(ty == 0)
            d3 = descr;
    }

    public Phone(Cursor c) {
        sid = c.getLong(0);
        mime = c.getString(1);
        d1 = c.getString(2);
        d2 = c.getInt(3);
        d3 = c.getString(4);
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
                    .withValue(ContactsContract.Data.SYNC1, sid)
                    .build());
    }

    public String toString() {
        return "[Phone: " + sid + " " + mime + " " + d1 + " " + d2 + " " + d3 + "]";
    }
}

