package com.isecpartners.samplesync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

/*
 * An activity for helping a user to remedy errors.
 * When an error occurs in the background, a notification is posted
 * and if the user responds to the notification, he will end up
 * here.
 */
public class Remedy extends Activity {
    public static final String TAG = "Remedy";

    // enumset seems too heavyweight here.. 
    public static final int DELETE = 1;
    public static final int WIPELOCAL = 2;
    public static final int WIPEREMOTE = 4;
    public static final int FIXCREDS = 8;

    static int nextId = 0;


    /*
     * Send a notification of an error that needs remediation.
     */
    static void error(Context ctx, String msg, int acts, Account acct) {
        long now = System.currentTimeMillis();
        int nid;
        synchronized(Remedy.class) {
            nid = ++nextId;
        }
        Log.v(TAG, "notify: " + msg + " acts: " + acts + " id: " + nid + " acct: " + acct.name);

        Intent i = new Intent(ctx, Remedy.class);
        Bundle e = new Bundle();
        e.putString("message", msg);
        e.putInt("actions", acts);
        e.putInt("noteid", nid);
        e.putParcelable("account", acct);
        i.putExtras(e);

        /* we dont use notification builder since it requires >= API-11 */
        Notification note = new Notification(R.drawable.icon, msg, now);
        PendingIntent ci = PendingIntent.getActivity(ctx, 0, i, 0);
        note.setLatestEventInfo(ctx.getApplicationContext(), "Sync error", msg, ci);

        NotificationManager mgr = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(nid, note);
    }

    // factors out common code for our button handlers...
    class ButtonHelper implements View.OnClickListener {
        int mNid;
        String mName;
        View.OnClickListener mPress;

        public ButtonHelper(int vid, int nid, int act, int flag, String name, View.OnClickListener press) {
            Button b = (Button)findViewById(vid);
            if((act & flag) != 0)
                b.setVisibility(View.VISIBLE);
            else
                b.setVisibility(View.GONE);
            mNid = nid;
            mName = name;
            mPress = press;
            b.setOnClickListener(this);
        }
            
        public void onClick(View v) {
            Log.v(TAG, "user pressed " + mName);
            mPress.onClick(v);

            NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mgr.cancel(mNid);
            finish();
        }
    }

    /* 
     * The errors that happen here should be infrequent and
     * unimportant.  So we just throw up a simple toast on error
     * to let them know what happened.  
     */
    void error(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Bundle e = this.getIntent().getExtras();
        final String msg = e.getString("message");
        final int acts = e.getInt("actions");
        final int nid = e.getInt("noteid");
        final Account acct = e.getParcelable("account");
        final AccountHelper h = new AccountHelper(this, acct);
        final Context ctx = this;
        Log.v(TAG, "remedy: " + msg + " acts: " + acts + " id: " + nid + " acct: " + acct.name);

        // if the account was deleted in the meantime, just give up...
        if(acct == null)
            return;

        setContentView(R.layout.remedy);

        TextView msgView = (TextView)findViewById(R.id.msg);
        msgView.setText("There was an error synching the " + acct.name + " account: " + msg);

        new ButtonHelper(R.id.delButton, nid, acts, DELETE, "delete", new View.OnClickListener() {
            public void onClick(View v) {
                AccountManager mgr = AccountManager.get(ctx);
                mgr.removeAccount(acct, null, null);
            }
        });

        new ButtonHelper(R.id.wipeLocalButton, nid, acts, WIPELOCAL, "wipeLocal", new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    h.initStore(h.getStateStore());
                } catch(Exception e) {
                    Log.v(TAG, "internal error initializing state store!");
                    error("error initializing local state");
                }
            }
        });

        new ButtonHelper(R.id.wipeRemoteButton, nid, acts, WIPEREMOTE, "wipeRemote", new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    /* note: we empty our "last" set anytime we empty the remote */
                    h.initStore(h.getRemoteStore());
                    h.initStore(h.getStateStore());
                } catch(Exception e) {
                    Log.v(TAG, "internal error initializing remote store!");
                    error("error initializing remote state");
                }
            }
        });

        new ButtonHelper(R.id.fixCredsButton, nid, acts, FIXCREDS, "fixCreds", new View.OnClickListener() {
            public void onClick(View v) {
                // XXX todo
                error("not yet implemented");
            }
        });

        new ButtonHelper(R.id.cancelButton, nid, 1, 1, "cancel", new View.OnClickListener() {
            public void onClick(View v) {
                return; // nothing to do
            }
        });
    }
}

