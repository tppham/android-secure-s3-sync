package com.isecpartners.samplesync.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import java.util.List;
import java.util.LinkedList;

/*
 * A list of contacts.
 */
public class Contacts {
    public List<Contact> contacts;

    public Contacts() {
        contacts = new LinkedList<Contact>();
    }

    /* Fetch all contacts from the content provider */
    public Contacts(Context ctx) {
        this();

        Cursor c = ctx.getContentResolver().query(RawContacts.CONTENT_URI,
                new String[]{RawContacts.CONTACT_ID},
                null, null, null);
        while(c.moveToNext())
            contacts.add(new Contact(ctx, c.getLong(0)));
        c.close();
    }

    public String toString() {
        String s = "";
        s += "[Contacts ";
        for(Contact c : contacts)
            s += c + " ";
        s += "]";
        return s;
    }
}
