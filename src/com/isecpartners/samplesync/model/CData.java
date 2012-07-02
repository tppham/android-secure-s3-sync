package com.isecpartners.samplesync.model;

import java.nio.ByteBuffer;
import java.util.List;

import android.content.Context;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
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
abstract class CData {
    private static final String TAG = "model.CData";
    public static final long UNKNOWN_ID = -1;
    public String mime; // indirectly marshalled -- see kind
    public int kind; // proxy for mime during marshalling
    public long id; // not considered during equality test, not marshalled

    /* add field valus to a db batch operation */
    public abstract void buildFields(ContentProviderOperation.Builder b);

    public CData() {
        id = UNKNOWN_ID;
    }    

    public CData(Cursor c) {
        id = c.getLong(0);
    }

    public abstract int getMatchScore();


    /* 
     * Fetch data from the content provider for a raw contact id.
     * Only pull the fields we care about from the content types
     * we know how to handle.
     */
    public static Cursor getDatas(Context ctx, long cid) {
        Cursor c = 
        ctx.getContentResolver().query(Data.CONTENT_URI,
            new String[]{
                Data._ID,
                Data.MIMETYPE,
                Data.DATA1,
                Data.DATA2,
                Data.DATA3,
                Data.DATA4,
                Data.DATA5,
                Data.DATA6,
                Data.DATA7,
                Data.DATA8,
                Data.DATA9,
            },
            Data.RAW_CONTACT_ID + "=" + cid + " AND " + 
                Data.MIMETYPE + " in (?, ?, ?)",
            new String[] {
                Phone.MIMETYPE,
                Email.MIMETYPE,
                Name.MIMETYPE,
            }, null);
        return c;
    }

    /* build a model object from a single getDatas() row */
    public static CData get(Cursor c) {
        CData d = null;
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

    // add a crossreference to the contact.  If the contact id is unknown
    // use the relative defIdx instead.
    static void buildRef(ContentProviderOperation.Builder b, Contact c, int defIdx) {
        if(c.id != c.UNKNOWN_ID) {
            b.withValue(Data.RAW_CONTACT_ID, c.id);
        } else  {
            assert(defIdx != UNKNOWN_ID);
            b.withValueBackReference(Data.RAW_CONTACT_ID, defIdx);
        }
    }

    // return a builder for a data insertion that references the contact
    public ContentProviderOperation.Builder buildInsert(Contact c, int defIdx) {
        ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        buildRef(b, c, defIdx);
        buildFields(b);
        return b;
    }

    // return a builder for a data deletion
    public ContentProviderOperation.Builder buildDelete() {
        assert(id != UNKNOWN_ID);
        return ContentProviderOperation
                    .newDelete(Data.CONTENT_URI)
                    .withSelection(Data._ID + "=?", new String[]{ String.valueOf(id) });
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

    public static String quote(String s) {
        if(s == null)
            return "null";
        return "\"" + s + "\"";
    }

    public abstract void marshal(ByteBuffer buf, int version) throws Marsh.Error;
    public abstract void _unmarshal(ByteBuffer buf, int version) throws Marsh.Error;

    public static CData unmarshal(ByteBuffer buf, int version) throws Marsh.Error {
        CData d;
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

