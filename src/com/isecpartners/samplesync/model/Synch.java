package com.isecpartners.samplesync.model;

import java.util.List;
import java.util.LinkedList;
import android.util.Log; // XXX temp hack

/*
 * Synching algorithm.  Merging happens on three sets of
 * contacts, the "local" contacts fetched from the phone that
 * were generated by other contact providers and the user,
 * the "remote" contacts fetched from our remote storage of
 * contacts and the "last" contacts, which we store on the phone
 * after each synch and represent the state of the contacts 
 * after the last synch point.
 *
 * To synch we first correlate the contacts from all three sources
 * together (whenever possible).  Then for each contact, we see
 * if there are changes between the last set and the local set
 * or the last set and the remote set.  If there are changes from
 * two sources, we resolve the conflict (simplistically by choosing
 * one set of changes over the other).  Then we apply these changes
 * to bring all three contacts into agreement.  This is done by
 * first updating the "last" contact, and then applying any difference
 * between the "last" contact and the local or remote (whichever
 * was not the source of the changes) to that contact.  When
 * applying these changes, the updates to the databases are made
 * (local and last are stored in the contacts manager) and the
 * in-memory copy of the remote contacts is updated.  After 
 * all contacts are synched, the remote contacts are serialized into
 * a blob and sent to the remote storage.
 */
public class Synch {
    ContactSet mLast, mLocal, mRemote;
    boolean mPreferLocal;

    /* return the first d from ds that has mime type mime, or null. */
    static Data firstDataMime(List<Data> ds, String mime) {
        for(Data d : ds) {
            if(d.mime.compareTo(mime) == 0)
                return d;
        }
        return null;
    }

    /* return true if x matches c either by some data match
     * with data of type kl, or with any match whatsoever if kl is null.
     */
    static boolean match(Contact x, Contact c, String mime) {
        if(mime == null) { /* if any data matches */
            for(Data xd : x.data) {
                for(Data cd : c.data) {
                    if(xd == cd)
                        return true;
                }
            }
            return false;
    
        } else { /* if the first mime match in x.data matches one in c.data */
            Data xd = firstDataMime(x.data, mime);
            return xd != null && xd == firstDataMime(c.data, mime);
        }
    }

    /* return the most specific match to x from cs */
    static Contact bestMatch(Contact x, List<Contact> cs) {
        /* 
         * try to find match with primary name, phone or email first
         * then fall back to a generic match of any data.
         */
        String[] mimes = new String[] {
            Name.MIMETYPE,
            Phone.MIMETYPE,
            Email.MIMETYPE,
            null // null means any data
        };
        for(String mime : mimes) {
            for(Contact c : cs) {
                if(match(x, c, mime))
                    return c;
            }
        }
        return null;
    }

    /*
     * Merge contacts from different sources onto a single list,
     * keeping track of whence they came.
     *
     * When finished, all contacts have a "master" field which links
     * to an element on the returned list (which might be the contact
     * itself).  Each element on the returned list has fields
     * "remote", "local" and "last" which point to any merged entries
     * from the local, remote or last lists.
     */
    static List<Contact> merge(List<Contact> last, List<Contact> local, List<Contact> remote) {
        LinkedList<Contact> all = new LinkedList<Contact>();

        // put all "last" entries onto the list
        for(Contact c : last) {
            c.last = c;
            c.master = c;
            all.add(c);
        }

        // merge all "local" entries onto the list
        Contact m;
        for(Contact c : local) {
            if((m = bestMatch(c, all)) != null) {
                m.local = c;
                c.master = m;
            } else { // not found
                c.local = c; 
                c.master = m;
                all.add(c);
            }
        }

        // merge all "remote" entries onto the list
        for(Contact c : remote) {
            if((m = bestMatch(c, all)) != null) {
                m.remote = c;
                c.master = m;
            } else { // not found
                c.remote = c; 
                c.master = c;
                all.add(c);
            }
        }
        return all;
    }

    /* 
     * The changes between two contacts.
     * It has a flag if this contact is added, if it is deleted,
     * and a list of data items that were added or deleted.
     */
    public static class Changes {
        public boolean delContact;
        public List<Data> addData, delData;

        public Changes(boolean del, List<Data> a, List<Data> d) {
            delContact = del;
            addData = a;
            delData = d;
        }
    };

    /* copy the list */
    static <D> List<D> copy(List<D> ds) {
        if(ds == null)
            return null;
        return new LinkedList<D>(ds);
    }

    /* return all elements in ds that arent in ds2 */
    static <D> List<D> diff(List<D> ds, List<D> ds2) {
        LinkedList<D> r = new LinkedList<D>();
        for(D d : ds) {
            if(!ds2.contains(d))
                r.add(d);
        }
        return r.isEmpty() ? null : r;
    }

    /*
     * Return the delta to change c into c2, or null if they're the same.
     */
    static Changes changes(Contact c, Contact c2) {
        if(c == null && c2 == null)
            return null;
        if(c == null)
            return new Changes(false, copy(c2.data), null); // add
        if(c2 == null)
            return new Changes(true, null, copy(c.data)); // delete

        List<Data> adds = Synch.<Data>diff(c.data, c2.data);
        List<Data> dels = Synch.<Data>diff(c2.data, c.data);
        if(adds == null && dels == null)
            return null;
        return new Changes(false, adds, dels); // alter data
    }

    /* sync changes from local sources */
    boolean syncLocal(Contact c) {
        Changes d = changes(c.last, c.local);
        if(d == null)
            return false;

        /* bring last up to date */
        c.last = mLast.push(c.last, d);

        /* then bring remote up to date */
        Changes d2 = changes(c.remote, c.last);
        if(d2 != null)
            c.remote = mRemote.push(c.remote, d2);
        return true;
    }

    /* sync changes from remote sources */
    boolean syncRemote(Contact c) {
        Changes d = changes(c.last, c.remote);
        if(d == null)
            return false;

        /* bring last up to date */
        c.last = mLast.push(c.last, d);

        /* then bring local up to date */
        Changes d2 = changes(c.local, c.last);
        if(d2 != null)
            c.local = mLocal.push(c.local, d2);
        return true;
    }

    /*
     * Sync data.
     * Use "last" as a comparison points for "local" and "remote" to
     * find differences, and propagate differences to the local
     * contacts database and the remote database while bringing "last"
     * up to date.
     * If "preferLocal" is true, prefer local edits over remote edits,
     * otherwise prefer remote edits over local edits.
     * Return true if any changes were encountered.
     */
    public boolean sync() {
        List<Contact> all = merge(mLast.contacts, mLocal.contacts, mRemote.contacts);
        boolean b, updated = false;
        for(Contact c : all) {
            if(mPreferLocal)
                b = syncLocal(c) || syncRemote(c);
            else
                b = syncRemote(c) || syncLocal(c);
            if(b)
                updated = true;
        }
        return updated;
    }

    public Synch(ContactSet last, ContactSet local, ContactSet remote, boolean preferLocal) {
        mLast = last;
        mLocal = local;
        mRemote = remote;
        mPreferLocal = preferLocal;
    }
}

