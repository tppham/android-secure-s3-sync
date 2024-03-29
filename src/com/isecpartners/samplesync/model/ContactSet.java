package com.isecpartners.samplesync.model;

import java.util.List;
import java.util.LinkedList;

import android.content.SyncStats;
import android.util.Log;

/*
 * A contact set represents a collection of contacts and their
 * source. It has the ability to fetch contacts and push updates
 * to them.
 *
 * This simple contact set doesn't do anything during synch except 
 * maintain its internal list.  Other contact sets extend this to add more
 * functionality during synch.
 */
public class ContactSet {
    protected final String TAG = "ContactSet_";
    public String name;

    public List<Contact> contacts;
    public boolean dirty;

    public ContactSet(String n) {
        name = n;
        contacts = new LinkedList<Contact>();
        dirty = false;
    }

    /*
     * Apply changes d to contact c, returning the contact.
     */
    public Contact push(Contact c, Synch.Changes ch, SyncStats stats) {
        stats.numEntries ++;
        if(c == null)  {
            stats.numInserts++;
            c = add();
        }
        if(ch.addData != null) {
            for(CData d : ch.addData)
                addData(c, d);
        }
        if(ch.delData != null) {
            for(CData d : ch.delData)
                delData(c, d);
        }
        if(ch.delContact) {
            stats.numDeletes++;
            del(c);
            c = null;
        }
        commit();
        return c;
    }

    public Contact push(Contact c, Synch.Changes ch) {
        return push(c, ch, new SyncStats());
    }

    public Contact add() {
        dirty = true;
        Log.v(TAG + name, "adding contact");
        Contact c = new Contact();
        contacts.add(c);
        return c;
    }

    public void del(Contact c) {
        dirty = true;
        Log.v(TAG + name, "deleting contact " + c);
        contacts.remove(c);
        return;
    }

    // note: d will be shared, not copied.
    // we might want to consider copying in the future.
    public void addData(Contact c, CData d) {
        dirty = true;
        Log.v(TAG + name, "adding " + d + " to " + c);
        c.data.add(d);
        return;
    }

    public void delData(Contact c, CData d) {
        dirty = true;
        Log.v(TAG + name, "deleting " + d + " from " + c);
        c.data.remove(d);
        return;
    }

    public boolean commit() {
        return true;
    }
}

