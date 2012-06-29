package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.util.List;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;


/* 
 * Base of all contact model items, including some common code.
 * We support a limited number of data types.  Each class
 * abstracts one of the types and can read the data from the
 * content provider, create the data in the content provider 
 * or perform updates to existing fields in the content provider.
 *
 * Important to know: the data elements are shared between
 * contacts during synch operations.  Some fields such as the
 * "id" field, are only relevant to the original contact they
 * were read under.  These fields are not considered in equality
 * testing, and are not serialized out to the database diretly
 * (during serialization they're passed as parameters, not used
 * from the instance fields).
 */
abstract class Data {
    private static final String TAG = "model.Data";
    public static final long UNKNOWN_ID = -1;
    public String mime; // indirectly marshalled -- see kind
    public int kind; // proxy for mime during marshalling
    public long id; // not considered during equality test, not marshalled

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

    public abstract void marshal(ByteBuffer buf, int version) throws Marsh.Error;
    public abstract void _unmarshal(ByteBuffer buf, int version) throws Marsh.Error;

    public static Data unmarshal(ByteBuffer buf, int version) throws Marsh.Error {
        Data d;
        int kind = Marsh.unmarshInt8(buf);
        if(kind == Phone.KIND)
            d = new Phone();
        else if(kind == Email.KIND)
            d = new Email();
        else if(kind == Name.KIND)
            d = new Name();
        else
            throw new Marsh.BadFormat("unknown kind " + kind + " encountered");
        d._unmarshal(buf, version);
        return d;
    }
};

