package com.isecpartners.samplesync.model;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import java.util.List;
import java.util.LinkedList;

import android.util.Log; //XXX

/*
 * A model of a contact.  This correspondes to a RawContact stored in
 * the contacts content provider.
 * Each contact has a set of data associated with it.  We only
 * track several types of data (ie: name, ph# and email addr).
 */
public class Contact {
    public static final long UNKNOWN_ID = -1;
    public long id;
    public List<Data> data;

    /* xref to merged contacts, used by Synch class only */
    public Contact local, remote, last, master; 

    public Contact() {
        data = new LinkedList<Data>();
        id = UNKNOWN_ID;
    }

    public Contact(long _id) {
        this();
        id = _id;
    }

    /* build a contact with rows from c */
    public Contact(long id, Cursor c) {
        this(id);
        Log.v("XXX", "start contact: " + id + " " + c.getCount());
        while(c.moveToNext()) {
            Log.v("XXX", "add data");
            data.add(Data.get(c));
        }
        c.close();
    }

    /* Fetch a contact from the content provider */
    public Contact(Context ctx, long id) {
        this(id, Data.getDatas(ctx, id));
    }

    public void add(Data d) { data.add(d); }


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
        return ContentProviderOperation
                    .newDelete(RawContacts.CONTENT_URI)
                    .withSelection(RawContacts._ID + "=?", new String[]{ String.valueOf(id) });
    }

    // add a crossreference to the contact.  If the contact id is unknown
    // use the relative defIdx instead.
    public void buildRef(ContentProviderOperation.Builder b, int defIdx) {
        if(id != UNKNOWN_ID) {
            b.withValue(ContactsContract.Data.RAW_CONTACT_ID, id);
        } else  {
            assert(defIdx != UNKNOWN_ID);
            b.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, defIdx);
        }
    }

    public String toString() {
        String s = "";
        s += "[Contact " + id + ": ";
        for(Data d : data)
            s += d + " ";
        s += "]";
        return s;
    }
}
