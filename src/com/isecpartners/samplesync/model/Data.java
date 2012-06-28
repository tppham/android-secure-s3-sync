package com.isecpartners.samplesync.model;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import java.util.List;


/* 
 * Base of all contact model items, including some common code.
 * We support a limited number of data types.  Each class
 * abstracts one of the types and can read the data from the
 * content provider, create the data in the content provider 
 * or perform updates to existing fields in the content provider.
 *
 * XXX in the near future they should also be able to serialize
 * to some blob that we can store.
 */
abstract class Data {
    private static final String TAG = "model.Data";
    public static final long UNKNOWN_ID = -1;
    public String mime;
    public long id; // not considered during equality test

    /* add field valus to a db batch operation */
    public abstract void buildFields(ContentProviderOperation.Builder b);

    public Data() {
        id = UNKNOWN_ID;
    }    

    public Data(Cursor c) {
        id = c.getLong(0);
    }


    /* 
     * Fetch data from the content provider for a raw contact id.
     * Only pull the fields we care about from the content types
     * we know how to handle.
     */
    public static Cursor getDatas(Context ctx, long cid) {
        Cursor c = 
        ctx.getContentResolver().query(RawContactsEntity.CONTENT_URI,
            new String[]{
                RawContactsEntity._ID,
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
            RawContactsEntity.CONTACT_ID + "=" + cid + " AND " + 
                RawContactsEntity.MIMETYPE + " in (?, ?, ?)",
            new String[] {
                Phone.MIMETYPE,
                Email.MIMETYPE,
                Name.MIMETYPE,
            }, null);
        Log.v("XXX", "getDatas: " + c.getCount());
        return c;
    }

    /* build a model object from a single getDatas() row */
    public static Data get(Cursor c) {
        Data d = null;
        String mime = c.getString(1);
        if(mime.equals(Phone.MIMETYPE))
            d = new Phone(c);
        else if(mime.equals(Email.MIMETYPE))
            d = new Email(c);
        else if(mime.equals(Name.MIMETYPE))
            d = new Name(c);
        else
            Log.v(TAG, "unknown mime type - should never happen! " + mime);
        return d;
    }

    // return a builder for a data insertion that references the contact
    public ContentProviderOperation.Builder buildInsert(Contact c, int defIdx) {
        ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        c.buildRef(b, defIdx);
        return b;
    }

    // return a builder for a data deletion
    public ContentProviderOperation.Builder buildDelete() {
        assert(id != UNKNOWN_ID);
        return ContentProviderOperation
                    .newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data._ID + "=?", new String[]{ String.valueOf(id) });
    }


    /* some helpers for equals/hashcode ... */
    public static boolean streq(String a, String b) {
        if(a == null )
            return b == null;
        return a.equals(b);
    }
    public static int strhash(String a) {
        if (a == null)
            return 0;
        return a.hashCode();
    }
};

