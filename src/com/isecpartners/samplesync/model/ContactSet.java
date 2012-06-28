package com.isecpartners.samplesync.model;

import java.util.List;
import java.util.LinkedList;
import android.util.Log;

/*
 * A contact set represents a collection of contacts and their
 * source. It has the ability to fetch contacts and push updates
 * to them.
 */
public class ContactSet {
    protected final String TAG = "ContactSet_";
    protected String name;
    public List<Contact> contacts;

    public ContactSet(String n) {
        name = n;
        contacts = new LinkedList<Contact>();
    }

    /*
     * Apply changes d to contact c, returning the contact.
     */
    public Contact push(Contact c, Synch.Changes ch) {
        if(c == null) 
            c = add();
        for(Data d : ch.addData)
            addData(c, d);
        for(Data d : ch.delData)
            delData(c, d);
        if(ch.delContact) {
            del(c);
            c = null;
        }
        commit();
        return c;
    }

    public Contact add() {
        Log.v(TAG + name, "adding contact");
        Contact c = new Contact();
        contacts.add(c);
        return c;
    }

    public void del(Contact c) {
        Log.v(TAG + name, "deleting contact " + c);
        contacts.remove(c);
        return;
    }

    // note: d will be shared, not copied.
    public void addData(Contact c, Data d) {
        Log.v(TAG + name, "adding " + d + " to " + c);
        // XXX consider copying (all but id)?
        c.data.add(d);
        return;
    }

    public void delData(Contact c, Data d) {
        Log.v(TAG + name, "deleting " + d + " from " + c);
        c.data.remove(d);
        return;
    }

    public boolean commit() {
        return true;
    }
}

