package com.isecpartners.samplesync.model;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import android.content.OperationApplicationException;
import android.os.RemoteException;

import android.util.Log; //XXX

/*
 * A model of a contact.  This correspondes to a RawContact stored in
 * the contacts content provider.
 * Each contact has a set of data associated with it.  We only
 * track several types of data (ie: name, ph# and email addr).
 */
public class Contact {
    public long id;
    public List<Data> data;

    public Contact() {
        data = new LinkedList<Data>();
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

    public void put(Context ctx, String aname, String atype) 
        throws OperationApplicationException, RemoteException 
    {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                    .newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_NAME, aname)
                    .withValue(RawContacts.ACCOUNT_TYPE, atype)
                    // XXX a sync field?
                    .build());


        for(Data d : data)
            d.put(ops, true, 0);

        ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        // XXX update id based on what we got?
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
