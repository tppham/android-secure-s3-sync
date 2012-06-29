package com.isecpartners.samplesync.model;

import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;

/*
 * Email data.
 * Same format as phone data.
 */
public class Email extends Phone {
    public static final String MIMETYPE = CommonDataKinds.Email.CONTENT_ITEM_TYPE;
    public static final byte KIND = 2;

    public Email() {
        super();
        mime = MIMETYPE;
        kind = KIND;
    }

    public Email(String addr, int ty, String descr) {
        super(addr, ty, descr);
        mime = MIMETYPE;
        kind = KIND;
    }
    public Email(Cursor c) { super(c); }

    public String toString() {
        return "[Email: " + mime + " " + d1 + " " + d2 + " " + d3 + "]";
    }
}

