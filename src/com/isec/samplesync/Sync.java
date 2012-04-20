package com.isecpartners.samplesync;

import android.content.Context;
import android.util.Log;
import android.net.Uri;
import android.content.ContentUris;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds;
import android.content.ContentProviderOperation;


import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
 * experimentation code for testing sync stuff...
 */
public class Sync {
    private static final String TAG = "Sync";
    private static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.sdcard";

    private Context mCtx;

    public Sync(Context ctx) {
        mCtx = ctx;
    }

    public void run() {
        //testDb();
        newShadows();
    }

    String allRows(Cursor c) {
        String msg = "";
        int cols = c.getColumnCount();
        for(int col = 0; col < cols; col++) {
            if(col > 0)
                msg += ", ";
            try {
                msg += c.getColumnName(col) + "=" + c.getString(col);
            } catch(final Exception e) {
                msg += c.getColumnName(col) + "=???";
            }
        }
        return msg;
    }

    List<Long> accountIds(String type, String field) {
        // This is retarded, why cant the api be smarter here?
        String sel = (type == null) ? " is null" : "=?";
        String[] args = (type == null) ? new String[]{} : new String[]{type};
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI, 
                new String[]{field},
                RawContacts.ACCOUNT_TYPE + sel, args, null);

        LinkedList<Long> as = new LinkedList<Long>();
        while(c.moveToNext())
            as.add(c.getLong(0));
        c.close();
        return as;
    }

    // could be more generic?
    public static boolean contains(List<Long> xs, long y) {
        for(long x : xs ) 
            if(x == y) return true;
        return false;
    }

    static abstract class CData {
        /* extract from getDatas() row */
        public abstract void _get(Cursor c);

        /* batch ops for putting into the data table */
        public abstract void put(List<ContentProviderOperation> ops, boolean back, int id);

        /* create the appropriate CData from a getDatas() row */
        static CData get(Cursor c) {
            CData d = null;
            String mime = c.getString(1);
            if(mime.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE) ||
               mime.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE))
                d = new CPhone();
            else
                Log.v(TAG, "unknown mime type - should never happen! " + mime);
            if(d != null)
                d._get(c);
            return d;
        }
    };

    // also used for email..
    // data1 - phnum, data2 - type, data3 - descr if data2 == 0
    static class CPhone extends CData {
        long sid;
        String mime, d1, d3;
        int d2;

        public CPhone() {}

        public void _get(Cursor c) {
            sid = c.getLong(0);
            mime = c.getString(1);
            d1 = c.getString(2);
            d2 = c.getInt(3);
            d3 = c.getString(4);
        }

        public void put(List<ContentProviderOperation> ops, boolean back, int id) {
            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            if(back)
                b.withValueBackReference(Data.RAW_CONTACT_ID, id);
            else
                b.withValue(Data.RAW_CONTACT_ID, id);
            ops.add(b.withValue(Data.MIMETYPE, mime)
                        .withValue(Data.DATA1, d1)
                        .withValue(Data.DATA1, d2)
                        .withValue(Data.DATA1, d3)
                        .withValue(Data.SYNC1, d3)
                        .build());
        }

        public String toString() {
            return "[Phone: " + sid + " " + mime + " " + d1 + " " + d2 + " " + d3 + "]";
        }
    }

    /* return data items matching a raw id for mimetypes we care about */
    private Cursor getDatas(long id) {
        return mCtx.getContentResolver().query(RawContactsEntity.CONTENT_URI,
            new String[]{
                RawContactsEntity.CONTACT_ID,
                RawContactsEntity.MIMETYPE,
                RawContactsEntity.DATA1,
                RawContactsEntity.DATA2,
                RawContactsEntity.DATA3
            },
            RawContactsEntity.CONTACT_ID + "=" + id + " AND " + 
                RawContactsEntity.MIMETYPE + " in (?, ?)",
            new String[] {
                CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                CommonDataKinds.Email.CONTENT_ITEM_TYPE
            }, null);
    }

    void newSyncAcct(List<ContentProviderOperation> ops, String aname, String atype, String s1) {
        ops.add(ContentProviderOperation
                    .newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_NAME, aname)
                    .withValue(RawContacts.ACCOUNT_TYPE, atype)
                    .withValue(RawContacts.SYNC1, s1)
                    .build());
    }

    /* shadow a local account into a sync account */
    void shadow(long id) {
        Log.v(TAG, "need to shadow: " + id);
        Cursor c = getDatas(id);
        if(c.getCount() == 0) {
            Log.v(TAG, "skipping uninteresting...");
            return;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        newSyncAcct(ops, "test", ACCOUNT_TYPE, "" + id);
        while(c.moveToNext()) {
            CData d = CData.get(c);
            Log.v(TAG, "sync: " + d);
            d.put(ops, true, 0);
        }
        c.close();

        try {
            mCtx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch(final Exception e) {
            Log.v(TAG, "apply batch failed: " + e);
        }
    }

    /* look for new accounts to shadow */
    void newShadows() {
        /* uses a naive uniqueness test that should be fine
         * in practice given the small number of items in a contact list.
         */
        List<Long> locals = accountIds(null, RawContacts.CONTACT_ID);
        List<Long> syncs = accountIds(ACCOUNT_TYPE, RawContacts.SYNC1);

        Log.v(TAG, "synching...");
        for(long id : locals) {
            if(!contains(syncs, id)) 
                shadow(id);
        }
    }

    void dumpContacts() {
        Cursor c = mCtx.getContentResolver().query(Data.CONTENT_URI, null, null, null, null);
        while(c.moveToNext()) {
            Log.v(TAG, "data: " + allRows(c));
        }
        c.close();
    }

    void testDb() {
        Log.v(TAG, "dumping...");
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI,
                null, null, null, null);
                //RawContacts.CONTACT_ID + "=?",
                //new String[]{String.valueOf(contactId)}, null);
        while(c.moveToNext()) {
            int id = c.getInt(3);
            Log.v(TAG, "contact: " + allRows(c));
        }
        c.close();
        dumpContacts();
    }
}

