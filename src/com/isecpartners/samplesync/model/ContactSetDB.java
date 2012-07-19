package com.isecpartners.samplesync.model;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;


/*
 * A contact set for interacting with the contact provider DB.
 *
 * We build up operations into mOps and then commit them all at once.
 */
public class ContactSetDB extends ContactSet {
    // XXX move elsewhere?
    static final String TYPE_EXCHANGE = "com.android.exchange";
    static final String TYPE_POP_IMAP = "com.android.email";

    protected Context mCtx;
    protected String mAcctName;
    protected String mAcctType;

    protected int mCIdx; // index of last contact we created
    protected ArrayList<ContentProviderOperation> mOps;
    protected List<Fixup> mFixups;

    public ContactSetDB(String name, Context ctx, String acctName, String acctType) {
        super(name);
        mCtx = ctx;
        mAcctName = acctName;
        mAcctType = acctType;

        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();
        mFixups = new LinkedList<Fixup>();

        loadContacts();
    }

    protected void loadContacts() {
        /* we want all entries with no account, or the google or exchange accounts */
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI,
                    new String[]{ RawContacts._ID, 
                                RawContacts.ACCOUNT_TYPE,
                                RawContacts.ACCOUNT_NAME },
                    RawContacts.DELETED + " = 0 AND "
                        + "(" + RawContacts.ACCOUNT_TYPE + " is null OR " 
                        + RawContacts.ACCOUNT_TYPE + "=? OR " 
                        + RawContacts.ACCOUNT_TYPE + "=?)",
                    // XXX is this right?  verify account names.
                    new String[]{ TYPE_EXCHANGE, TYPE_POP_IMAP },
                    null);

        while(c.moveToNext())
            contacts.add(new Contact(mCtx, c.getLong(0), c.getString(1), c.getString(2)));
        c.close();
    }

    public Contact add() {
        Contact c = super.add();
        c.acctType = mAcctType;
        c.acctName = mAcctName;

        mCIdx = mOps.size(); // where the contact xref is in the list
        mFixups.add(new Fixup(c, null, mCIdx));
        mOps.add(c.buildInsert().build());
        return c;
    }

    public void del(Contact c) {
        super.del(c);

        mOps.add(c.buildDelete().build());
        return;
    }
    
    public void addData(Contact c, CData data) {
        super.addData(c, data);

        mFixups.add(new Fixup(null, data, mOps.size()));
        mOps.add(data.buildInsert(c, mCIdx).build());
        return;
    }

    public void delData(Contact c, CData data) {
        super.delData(c, data);

        mOps.add(data.buildDelete().build());
        return;
    }

    public boolean commit() {
        if(!super.commit())
            return false;

        if(mOps.isEmpty())
            return true;

        boolean ok = false;
        ContentProviderResult[] r;
        try {
            r = mCtx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mOps);
            for(Fixup f : mFixups) /* capture the results of any insertions */
                f.apply(r);
            ok = true;
        } catch(final Exception e) {
            Log.v(TAG + name, "commit error: " + e);
        }

        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();
        mFixups = new LinkedList<Fixup>();
        return ok;
    }

    // Capture the result of an insertion so we can record the
    // resulting _ID.
    class Fixup {
        Contact mContact;
        CData mData;
        int mIdx;

        public Fixup(Contact c, CData d, int idx) {
            mContact = c;
            mData = d;
            mIdx = idx;
        }

        // The ID is the last component of the URI path.  fetch and parse it.
        int getId(Uri uri) throws IllegalArgumentException {
            String path = uri.getEncodedPath();
            int pos = path.lastIndexOf('/');
            if(pos == -1)
                throw new IllegalArgumentException("unexpected uri format: " + uri);
            String sn = path.substring(pos+1);
            return Integer.parseInt(sn); // throws NumberFormatException
        }

        // apply the fixup -- fetch the id and save it into the model
        public void apply(ContentProviderResult[] rs) throws IllegalArgumentException {
            int id = getId(rs[mIdx].uri);
            if(mContact != null) {
                mContact.locid = id;
                Log.v(TAG+name, "insert id " + id + " for contact " + mContact);
            } else {
                mData.locid = id;
                Log.v(TAG+name, "insert id " + id + " for data " + mData);
            }
        }
    }
}

