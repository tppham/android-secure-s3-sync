package com.isecpartners.samplesync.s3;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.isecpartners.samplesync.R;

/**
 * A GUI for entering S3 credentials.  
 * The AccountAuthenticatorActivity allows us to pass results
 * back to our AuthAdapter by calling setAccountAuthenticatorResult.
 */
public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = "s3.AuthActivity";
    public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.s3";
    public static final int DIALOG_PROGRESS = 0;

    private final Handler mCb = new Handler();
    private AccountManager mAcctMgr;
    private Thread mSigninThread;

    private TextView mMsgTxt;
    private EditText mAcctIn, mPasswdIn;

    /* bg thread that performs signin and reports back */
    private class SigninThread extends Thread {
        private static final String TAG = "s3.SigninThread";
        private String mAcct, mPasswd;

        public SigninThread(String acct, String passwd) {
            mAcct = acct;
            mPasswd = passwd;
        }

        public void run() {
            // XXX stuff to do during login...
            Log.v(TAG, "XXX should verify signin here! " + mAcct + " " + mPasswd);
            done(true);
        }

        // callback with our results
        private void done(final boolean ok) {
            mCb.post(new Runnable() {
                public void run() { onSigninDone(mAcct, mPasswd, ok); }
            });
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TAG, "onCreate");
        mAcctMgr = AccountManager.get(this);

        Window w = getWindow();
        w.requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.s3login);
        w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMsgTxt = (TextView)findViewById(R.id.msg);
        mAcctIn = (EditText)findViewById(R.id.acct_edit);
        mPasswdIn = (EditText)findViewById(R.id.passwd_edit);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Authenticating...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "cancelled");
                if (mSigninThread != null) {
                    mSigninThread.interrupt();
                    finish();
                }
            }
        });
        return dialog;
    }

    /**
     * SignIn button was clicked.  Sign in in the background.
     * @param view The Submit button for which this method is invoked
     */
    public void onSignIn(View view) {
        String acct = mAcctIn.getText().toString();
        String passwd = mPasswdIn.getText().toString();

        if(acct.equals("") || passwd.equals("")) {
            mMsgTxt.setText("You must enter an account and password");
            return;
        }

        // XXX showDialog is deprecated.. investigate..
        showDialog(DIALOG_PROGRESS);
        mSigninThread = new SigninThread(acct, passwd);
        mSigninThread.start();
        /* thread will invoke onSigninDone callback if not cancelled... */
    }

    /**
     * Callback (through mCb) from SigninThread when completed.
     */
    public void onSigninDone(String acct, String passwd, boolean ok) {
        Log.v(TAG, "onSigninDone: " + ok);
        dismissDialog(DIALOG_PROGRESS);

        if(!ok) {
            mMsgTxt.setText("Log in failed!");
            return;
        }

        Account a = new Account(acct, ACCOUNT_TYPE);
        mAcctMgr.addAccountExplicitly(a, passwd, null);
        ContentResolver.setSyncAutomatically(a, ContactsContract.AUTHORITY, true);

        Intent i = new Intent();
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, acct);
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        i.putExtra(AccountManager.KEY_AUTHTOKEN, passwd);
        setAccountAuthenticatorResult(i.getExtras());
        setResult(RESULT_OK, i);
        finish();
    }
}
