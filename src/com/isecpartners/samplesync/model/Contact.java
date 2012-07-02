package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import android.util.Log; //XXX

/*
 * A model of a contact.  This correspondes to a RawContact stored in
 * the contacts content provider.
 * Each contact has a set of data associated with it.  We only
 * track several types of data (ie: name, ph# and email addr).
 */
public class Contact {
    public static final long UNKNOWN_ID = -1;
    public long id; // row id (not contact id!).  not marshalled
    public List<CData> data;

    /* xref to merged contacts, used by Synch class only */
    public Contact local, remote, last, master;  // not marshalled

    public Contact() {
        data = new LinkedList<CData>();
        id = UNKNOWN_ID;
    }

    public Contact(long _id) {
        this();
        id = _id;
    }

    /* build a contact with rows from c */
    public Contact(long id, Cursor c) {
        this(id);
        while(c.moveToNext())
            data.add(CData.get(c));
        c.close();
    }

    /* Fetch a contact from the content provider */
    public Contact(Context ctx, long id) {
        this(id, CData.getDatas(ctx, id));
    }

    public void add(CData d) { data.add(d); }


    // return a builder for inserting a contact into the db
    public ContentProviderOperation.Builder buildInsert(String aname, String atype) {
        return ContentProviderOperation
                    .newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_NAME, aname)
                    .withValue(RawContacts.ACCOUNT_TYPE, atype);
    }

    // return a builder for deleting the contact from the db
    public ContentProviderOperation.Builder buildDelete() {
        assert(id != UNKNOWN_ID);
        Log.v("XXX", "delete contact: " + id);
        return ContentProviderOperation
                    .newDelete(RawContacts.CONTENT_URI)
                    .withSelection(RawContacts._ID + "=?", new String[]{ String.valueOf(id) });
    }

    // add a crossreference to the contact.  If the contact id is unknown
    // use the relative defIdx instead.
    // XXX this is for a data entry and should prob be in the data class
    public void buildRef(ContentProviderOperation.Builder b, int defIdx) {
        if(id != UNKNOWN_ID) {
            Log.v("XXX", "contact ref: " + id);
            b.withValue(Data.RAW_CONTACT_ID, id);
        } else  {
            assert(defIdx != UNKNOWN_ID);
            Log.v("XXX", "contact ref relative: " + defIdx);
            b.withValueBackReference(Data.RAW_CONTACT_ID, defIdx);
        }
    }

    public String toString() {
        String s = "";
        s += "[Contact " + id + ": ";
        for(CData d : data)
            s += d + " ";
        s += "]";
        return s;
    }

    public void marshal(ByteBuffer buf, int vers) throws Marsh.Error {
        Marsh.marshInt16(buf, data.size());
        for(CData d : data)
            d.marshal(buf, vers);
    }

    public static Contact unmarshal(ByteBuffer buf, int vers) throws Marsh.Error {
        Contact c = new Contact();
        int cnt = Marsh.unmarshInt16(buf);
        for(int i = 0; i < cnt; i++)
            c.data.add(CData.unmarshal(buf, vers));
        return c;
    }
}
