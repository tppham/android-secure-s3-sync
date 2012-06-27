package com.isecpartners.samplesync.model;

import java.util.List;
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

    public ContactSet(String n, List<Contact> cs) { 
        name = n;
        contacts = cs; 
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
        return c;
    }

    public Contact add() {
        Log.v(TAG + name, "adding contact");
        return null;
    }

    public void del(Contact c) {
        Log.v(TAG + name, "deleting contact " + c);
        return;
    }

    public void addData(Contact c, Data d) {
        Log.v(TAG + name, "adding " + d + " to " + c);
        return;
    }

    public void delData(Contact c, Data d) {
        Log.v(TAG + name, "deleting " + d + " from " + c);
        return;
    }
}

