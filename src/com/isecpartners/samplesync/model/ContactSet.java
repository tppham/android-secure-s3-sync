
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
    public Contact push(Contact c, Synch.Changes d) {
        if(c == null) 
            c = add();
        for(Data d : d.addData)
            addData(c, d);
        for(Data d : d.delData)
            delData(c, d);
        if(d.delContact) {
            del(c);
            c = null;
        }
        return c;
    }

    public Contact add() {
        Log.v(TAG + name, "adding contact");
        return c;
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

