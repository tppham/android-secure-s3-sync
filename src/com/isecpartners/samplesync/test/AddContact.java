package com.isecpartners.samplesync.test;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/*
 * Test adding a contact to the database.
 */
public class AddContact extends Activity {
    static String TAG = "test.addContact";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onStart() {
        super.onStart();
        run();
    }

    protected void run() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder b;

        b = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .withValue(RawContacts.ACCOUNT_TYPE, null);
        ops.add(b.build());
        b = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(Data.DATA1, "Test Contact")
                .withValue(Data.DATA2, "Test")
                .withValue(Data.DATA3, "Contact");
        ops.add(b.build());

        ContentProviderResult[] r;
        try {
            r = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            for(int i = 0; i < r.length; i++)
                Log.v(TAG, "result " + i + ": " + r[i]);
        } catch(Exception e) {
            Log.v(TAG, "apply batch failed: " + e);
        }
    }
}

