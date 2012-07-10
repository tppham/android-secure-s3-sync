package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

/*
 * A model of a contact.  This correspondes to a RawContact stored in
 * the contacts content provider.
 * Each contact has a set of data associated with it.  We only
 * track several types of data (ie: name, ph# and email addr).
 */
public class Contact {
    public static final String TAG = "model.Contact";
    public static final long UNKNOWN_ID = -1;
    public String acctName, acctType;
    public long locid, remid; // row id (not contact id!).
    public List<CData> data;

    public Contact() {
        data = new LinkedList<CData>();
        acctName = null;
        acctType = null;
        locid = UNKNOWN_ID;
        remid = UNKNOWN_ID;
    }

    /* Fetch a contact from the content provider */
    public Contact(Context ctx, long id, String atype, String aname) {
        this();
        locid = id;
        acctType = atype;
        acctName = aname;

        Cursor c = CData.getDatas(ctx, id);
        while(c.moveToNext())
            data.add(CData.get(c));
        c.close();
    }

    public void add(CData d) { data.add(d); }


    // return a builder for inserting a contact into the db
    public ContentProviderOperation.Builder buildInsert() {
        return ContentProviderOperation
                    .newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_NAME, acctName)
                    .withValue(RawContacts.ACCOUNT_TYPE, acctType);
    }

    // return a builder for deleting the contact from the db.
    public ContentProviderOperation.Builder buildDelete() {
        if(!(locid != UNKNOWN_ID))
            Log.e(TAG, "assert locid != UNKNOWN_ID");
        if(acctType != null) {
            /* mark it as deleted, let the provider clean it up */
            return ContentProviderOperation
                        .newUpdate(RawContacts.CONTENT_URI)
                        .withSelection(RawContacts._ID + "=?", new String[]{ String.valueOf(locid) })
                        .withValue(RawContacts.DELETED, "1");
        } else {
            /* just delete it now, nobody owns it */
            return ContentProviderOperation
                        .newDelete(RawContacts.CONTENT_URI)
                        .withSelection(RawContacts._ID + "=?", new String[]{ String.valueOf(locid) });
        }
    }

    public String toString() {
        String s = "";
        s += "[Contact " + locid + "/" + remid + " acct " + acctType + "/" + acctName + ": ";
        for(CData d : data)
            s += d + " ";
        s += "]";
        return s;
    }

    public void marshal(ByteBuffer buf, int vers) throws Marsh.Error {
        // XXX we dont really need both fields for the "remote" store,
        // only for the "last" store.. perhaps a switch for this.
        Marsh.marshString(buf, acctType);
        Marsh.marshString(buf, acctName);
        Marsh.marshInt64(buf, locid);
        Marsh.marshInt64(buf, remid);
        Marsh.marshInt16(buf, data.size());
        for(CData d : data)
            d.marshal(buf, vers);
    }

    public static Contact unmarshal(ByteBuffer buf, int vers) throws Marsh.Error {
        Contact c = new Contact();
        // XXX we dont really need both fields for the "remote" store,
        // only for the "last" store.. perhaps a switch for this.
        c.acctType = Marsh.unmarshString(buf);
        c.acctName = Marsh.unmarshString(buf);
        c.locid = Marsh.unmarshInt64(buf);
        c.remid = Marsh.unmarshInt64(buf);
        int cnt = Marsh.unmarshInt16(buf);
        for(int i = 0; i < cnt; i++)
            c.data.add(CData.unmarshal(buf, vers));
        return c;
    }
}
