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
import android.content.ContentValues;


import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
 * experimentation code for testing sync stuff...
 */
public class Sync {
    private static final String TAG = "Sync";
    private static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.sdcard";
    private static final String ACCOUNT_NAME = "/sdcard/test";

    private Context mCtx;

    public Sync(Context ctx) {
        mCtx = ctx;
    }

    public void run() {
        Log.v(TAG, "run sync tests");
        createTestAcct();
        //clean();
        //newShadows();
        dumpDb();
        Log.v(TAG, "done sync tests");
    }

    /* make a string repr of all columns of the current cursor row */
    String allColumns(Cursor c) {
        String msg = "";
        int cols = c.getColumnCount();
        for(int col = 0; col < cols; col++) {
            if(col > 0)
                msg += ", ";
            try {
                String val = "" + c.getString(col);
                msg += c.getColumnName(col) + "=" + val.replace('\n', '_');
            } catch(final Exception e) {
                msg += c.getColumnName(col) + "=???";
            }
        }
        return msg;
    }

    /* return a list of all contact ids for a given account.
     * if account is null, return those not belonging to a sync provider
     */
    List<Long> contactIds(String type, String field) {
        // This is retarded, why cant the api be smarter here?
        String sel = (type == null) ? " is null" : "=?";
        String[] args = (type == null) ? new String[]{} : new String[]{type};
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI, 
                new String[]{field},
                RawContacts.ACCOUNT_TYPE + sel + " AND " + RawContacts.DELETED + "=0",
                args, null);

        LinkedList<Long> as = new LinkedList<Long>();
        while(c.moveToNext())
            as.add(c.getLong(0));
        c.close();
        return as;
    }

    /* return true if y is in xs */
    // could be more generic?
    public static boolean contains(List<Long> xs, long y) {
        for(long x : xs ) 
            if(x == y) return true;
        return false;
    }

    /* erase all of our db entries in contact and data table.  for testing... */
    public void clean() {
        for(Long id : contactIds(ACCOUNT_TYPE, RawContacts._ID)) {
            Log.v(TAG, "delete " + id);
            mCtx.getContentResolver().delete(Data.CONTENT_URI,
                Data.RAW_CONTACT_ID + "=" + id, null);

            Uri uri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
            mCtx.getContentResolver().delete(uri, 
                RawContacts._ID + "=" + id, null);
        }

    }

    /* abstract data element for a contact */
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
            else if(mime.equals(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                d = new CName();
            else
                Log.v(TAG, "unknown mime type - should never happen! " + mime);
            if(d != null)
                d._get(c);
            return d;
        }
    };

    /* contact phone number or email data. */
    // also used for email..
    // data1 - phnum, data2 - type, data3 - descr if data2 == 0
    static class CPhone extends CData {
        public long sid;
        public String mime, d1, d3;
        public int d2;

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
                        .withValue(Data.DATA2, d2)
                        .withValue(Data.DATA3, d3)
                        .withValue(Data.SYNC1, sid)
                        .build());
        }

        public String toString() {
            return "[Phone: " + sid + " " + mime + " " + d1 + " " + d2 + " " + d3 + "]";
        }
    }

    /* contact name data. */
    // data1 through data9 are all strings 
    static class CName extends CData {
        public long sid;
        public String mime, d1, d2, d3, d4, d5, d6, d7, d8, d9;

        public CName() {}

        public void _get(Cursor c) {
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
            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            if(back)
                b.withValueBackReference(Data.RAW_CONTACT_ID, id);
            else
                b.withValue(Data.RAW_CONTACT_ID, id);
            ops.add(b.withValue(Data.MIMETYPE, mime)
                        .withValue(Data.DATA1, d1)
                        .withValue(Data.DATA2, d2)
                        .withValue(Data.DATA3, d3)
                        .withValue(Data.DATA4, d4)
                        .withValue(Data.DATA5, d5)
                        .withValue(Data.DATA6, d6)
                        .withValue(Data.DATA7, d7)
                        .withValue(Data.DATA8, d8)
                        .withValue(Data.DATA9, d9)
                        .withValue(Data.SYNC1, sid)
                        .build());
        }

        public String toString() {
            return "[Name: " + sid + " " + mime + " " + d1 + " " + d2 + " " + d3 + d4 + " " + d5 + " " + d6 + " " + d7 + " " + d8 + " " + d9 + "]";
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
                RawContactsEntity.DATA3,
                RawContactsEntity.DATA4,
                RawContactsEntity.DATA5,
                RawContactsEntity.DATA6,
                RawContactsEntity.DATA7,
                RawContactsEntity.DATA8,
                RawContactsEntity.DATA9,
            },
            RawContactsEntity.CONTACT_ID + "=" + id + " AND " + 
                RawContactsEntity.MIMETYPE + " in (?, ?, ?)",
            new String[] {
                CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            }, null);
    }

    /* batch up the creation of a raw contact */
    void newSyncAcct(List<ContentProviderOperation> ops, String aname, String atype, String s1) {
        ops.add(ContentProviderOperation
                    .newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_NAME, aname)
                    .withValue(RawContacts.ACCOUNT_TYPE, atype)
                    .withValue(RawContacts.SYNC1, s1)
                    .build());
    }

    void createTestAcct() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        newSyncAcct(ops, ACCOUNT_NAME, ACCOUNT_TYPE, "xxx");

        CName n = new CName();
        n.mime = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
        n.d1 = "Youbie Nine"; // display
        n.d2 = "Youbie"; // first
        n.d3 = "Nine"; // last
        n.put(ops, true, 0);

        CPhone p = new CPhone();
        p.mime = CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        p.d1 = "808-596-7873";
        p.d2 = CommonDataKinds.Phone.TYPE_HOME;
        p.put(ops, true, 0);

        p.mime = CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        p.d1 = "808-555-1212";
        p.d2 = CommonDataKinds.Phone.TYPE_MOBILE;
        p.put(ops, true, 0);

        p.mime = CommonDataKinds.Email.CONTENT_ITEM_TYPE;
        p.d1 = "youbie@gmail.com";
        p.d2 = CommonDataKinds.Email.TYPE_HOME;
        p.put(ops, true, 0);

        try {
            mCtx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch(final Exception e) {
            Log.v(TAG, "apply batch failed: " + e);
        }
    }

    /* shadow a local contact into a sync contact */
    void shadow(long id) {
        Log.v(TAG, "need to shadow: " + id);
        Cursor c = getDatas(id);
        if(c.getCount() == 0) {
            Log.v(TAG, "skipping uninteresting...");
            return;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        newSyncAcct(ops, ACCOUNT_NAME, ACCOUNT_TYPE, "" + id);
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

    /* look for new contacts to shadow */
    void newShadows() {
        /* uses a naive uniqueness test that should be fine
         * in practice given the small number of items in a contact list.
         */
        List<Long> locals = contactIds(null, RawContacts.CONTACT_ID);
        List<Long> syncs = contactIds(ACCOUNT_TYPE, RawContacts.SYNC1);

        Log.v(TAG, "synching...");
        for(long id : locals) {
            if(!contains(syncs, id)) 
                shadow(id);
        }
    }

    /* dump the data table */
    void dumpData() {
        Cursor c = mCtx.getContentResolver().query(Data.CONTENT_URI, null, null, null, null);
        while(c.moveToNext()) {
            Log.v(TAG, "data: " + allColumns(c));
        }
        c.close();
    }

    /* dump the raw contacts table and the data table */
    void dumpDb() {
        Log.v(TAG, "dumping...");
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI, null, null, null, null);
        while(c.moveToNext()) {
            int id = c.getInt(3);
            Log.v(TAG, "contact: " + allColumns(c));
        }
        c.close();
        dumpData();
    }
}

