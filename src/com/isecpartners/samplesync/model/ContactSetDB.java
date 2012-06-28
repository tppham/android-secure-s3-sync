package com.isecpartners.samplesync.model;

import java.util.List;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import android.content.OperationApplicationException;
import android.os.RemoteException;

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
    protected boolean mLast; // load the "last" set (otherwise "local")

    protected int mCIdx; // index of contact
    protected ArrayList<ContentProviderOperation> mOps;

    public ContactSetDB(Context ctx, String acctName, String acctType, boolean last) {
        super("local");
        mCtx = ctx;
        // XXX pick acctName and type based on mLast?
        mAcctName = acctName;
        mAcctType = acctType;
        mLast = last;

        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();
    }

    public void loadContacts() {
        // XXX use mLast to pick a filter for the last or local set.
        // for last, load up only matches to the right acct name and type
        // for local, load up anything from goog, exchange or no provider.

        Cursor c;
        if(mLast) {
            /* we want all entries matching our account */
            c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI,
                    new String[]{ RawContacts.CONTACT_ID },
                    RawContacts.ACCOUNT_TYPE + "=? AND " + RawContacts.ACCOUNT_NAME + "=?",
                    new String[]{ mAcctType, mAcctName },
                    null);
        } else {
            /* we want all entries with no account, or the google or exchange accounts */
            c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI,
                    new String[]{ RawContacts.CONTACT_ID },
                    RawContacts.ACCOUNT_TYPE + " is null OR " + RawContacts.ACCOUNT_TYPE + "=? OR " + RawContacts.ACCOUNT_TYPE + "=?",
                    // XXX is this right?  verify account names.
                    new String[]{ TYPE_EXCHANGE, TYPE_POP_IMAP },
                    null);
        }

        while(c.moveToNext())
            contacts.add(new Contact(mCtx, c.getLong(0)));
        c.close();
    }

    public Contact add() {
        Contact c = super.add();
        mCIdx = mOps.size(); // where the contact xref is in the list
        mOps.add(c.buildInsert(mAcctName, mAcctType).build());
        return c;
    }

    public void del(Contact c) {
        super.del(c);

        mOps.add(c.buildDelete().build());
        return;
    }
    
    public void addData(Contact c, Data data) {
        super.addData(c, data);

        mOps.add(data.buildInsert(c, mCIdx).build());
        return;
    }

    public void delData(Contact c, Data data) {
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
        try {
            mCtx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mOps);
            ok = true;
        } catch(RemoteException e) {
            Log.v(TAG + name, "commit error: " + e);
        } catch(OperationApplicationException e) {
            Log.v(TAG + name, "commit error: " + e);
        }
        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();
        return ok;
    }
}

