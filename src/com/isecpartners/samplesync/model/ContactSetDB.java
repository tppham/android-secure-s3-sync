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

    protected int mCIdx; // index of last contact we created
    protected ArrayList<ContentProviderOperation> mOps;

    public ContactSetDB(String name, Context ctx, String acctName, String acctType) {
        super(name);
        mCtx = ctx;
        mAcctName = acctName;
        mAcctType = acctType;

        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();

        loadContacts();
    }

    protected void loadContacts() {
        /* we want all entries with no account, or the google or exchange accounts */
        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI,
                    new String[]{ RawContacts._ID },
                    RawContacts.ACCOUNT_TYPE + " is null OR " + RawContacts.ACCOUNT_TYPE + "=? OR " + RawContacts.ACCOUNT_TYPE + "=?",
                    // XXX is this right?  verify account names.
                    new String[]{ TYPE_EXCHANGE, TYPE_POP_IMAP },
                    null);

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
    
    public void addData(Contact c, CData data) {
        super.addData(c, data);

        ContentProviderOperation.Builder b = data.buildInsert(c, mCIdx);
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
        try {
            mCtx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, mOps);
            ok = true;
        } catch(final RemoteException e) {
            Log.v(TAG + name, "commit error: " + e);
        } catch(final OperationApplicationException e) {
            Log.v(TAG + name, "commit error: " + e);
        }
        mCIdx = (int)Contact.UNKNOWN_ID;
        mOps = new ArrayList<ContentProviderOperation>();
        return ok;
    }
}

