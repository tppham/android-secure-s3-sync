package com.isecpartners.samplesync;

import android.accounts.Account;
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

    static void enable(int x, int flag, Button b) {
        if((x & flag) != 0)
            b.setVisibility(View.VISIBLE);
        else
            b.setVisibility(View.GONE);
    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Bundle e = this.getIntent().getExtras();
        String msg = e.getString("message");
        final int acts = e.getInt("actions");
        final int nid = e.getInt("noteid");
        final Account acct = e.getParcelable("account");
        Log.v(TAG, "remedy: " + msg + " acts: " + acts + " id: " + nid + " acct: " + acct.name);

        // if the account was deleted in the meantime, just give up...
        if(acct == null)
            return;

        setContentView(R.layout.remedy);

        TextView msgView = (TextView)findViewById(R.id.msg);
        msgView.setText("There was an error synching the " + acct.name + " account: " + msg);

        Button delButton = (Button)findViewById(R.id.delButton);
        Button wipeLocalButton = (Button)findViewById(R.id.wipeLocalButton);
        Button wipeRemoteButton = (Button)findViewById(R.id.wipeRemoteButton);
        Button fixCredsButton = (Button)findViewById(R.id.fixCredsButton);
        Button cancelButton = (Button)findViewById(R.id.cancelButton);

        enable(acts, DELETE, delButton);
        enable(acts, WIPELOCAL, wipeLocalButton);
        enable(acts, WIPEREMOTE, wipeRemoteButton);
        enable(acts, FIXCREDS, fixCredsButton);
        enable(1, 1, cancelButton);

        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "delete account: " + acct.name);
                finish();
            }
        });

        wipeLocalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "wipe local: " + acct.name);
                finish();
            }
        });

        wipeRemoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "wipe remote: " + acct.name);
                finish();
            }
        });

        fixCredsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "fix creds: " + acct.name);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, "cancel: " + acct.name);
                finish();
            }
        });
    }
}

