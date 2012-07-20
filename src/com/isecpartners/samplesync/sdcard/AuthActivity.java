package com.isecpartners.samplesync.sdcard;

import java.io.File;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.isecpartners.samplesync.R;
import com.isecpartners.samplesync.Constants;
import com.isecpartners.samplesync.FileStore;
import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.AccountHelper;

/**
 * A GUI for entering sdcard credentials (just a directory name).
 * The AccountAuthenticatorActivity allows us to pass results
 * back to our AuthAdapter by calling setAccountAuthenticatorResult.
 */
public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = "sdcard.AuthActivity";
    public static final String ACCOUNT_TYPE = Constants.ACCOUNT_TYPE_SD;

    public AccountManager mAcctMgr;

    private TextView mMsgTxt;
    public EditText mDirIn;
    public EditText mAcctIn;
    public EditText mPassphrase;
    public int flag = 0;
	public String acct;
	public String dir;
	public String passphrase;
	public Context mCtx;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TAG, "onCreate");
        mAcctMgr = AccountManager.get(this);
        mCtx = this;
        setContentView(R.layout.sdcardlogin);

        mMsgTxt = (TextView)findViewById(R.id.msg);
        mDirIn = (EditText)findViewById(R.id.dir_edit);
        mAcctIn = (EditText)findViewById(R.id.acct_edit);
        mPassphrase = (EditText)findViewById(R.id.sd_passphrase);
    }

    /**
     * SignIn button was clicked.  
     * Validate the fields, initialize the storage and save the account.
     *
     * @param view The Submit button for which this method is invoked
     */
    public void onSignIn(View view) {
    	
    	if(flag == 0){
        acct = mAcctIn.getText().toString();
        dir = mDirIn.getText().toString();
        passphrase = mPassphrase.getText().toString();
    	}
    	
    	
        
        if(acct.equals("") || dir.equals("") || passphrase.equals("")) {
            mMsgTxt.setText("You must enter an account name, a directory and a passphrase");
            return;
        }

        if(h.accountExists()) {
            mMsgTxt.setText("That account already exists");
            return;
        }

        
        
        File sd = Environment.getExternalStorageDirectory();
        File d = new File(sd, dir);
        if(d.exists()) {
            if(!d.isDirectory()) {
                mMsgTxt.setText("Can't use that directory");
                return;
            }
        } else {
            /* XXX ask for user confirmation if it already exists! */
            if(!d.mkdirs()) {
                mMsgTxt.setText("Couldn't create directory " + d.getPath());
                return;
            }
        }

        FileStore f = new FileStore(d.getPath());
        try {
            // XXX warn user if store already exists, ask for confirmation
            // give them option to use existing, wipe, or cancel
            if(!h.storeExists(f)) {
                Log.v(TAG, "create empty file store for " + acct);
                h.initStore(f);
            }

            h.initStore(h.getStateStore());
        } catch(final IBlobStore.Error e) { /* shouldn't happen! */
            Log.e(TAG, "error creating the store: " + e);
            mMsgTxt.setText("Error initializing!");
            return;
        }

        Account a = new Account(acct, ACCOUNT_TYPE);
        mAcctMgr.addAccountExplicitly(a, "", null);
        AccountHelper h2 = new AccountHelper(this, a);
        h2.setAcctPref("path", d.getPath());
        h2.setAcctPref("passphrase", passphrase);
        ContentResolver.setSyncAutomatically(a, ContactsContract.AUTHORITY, true);

        Intent i = new Intent();
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, acct);
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        i.putExtra(AccountManager.KEY_AUTHTOKEN, "");
        setAccountAuthenticatorResult(i.getExtras());
        setResult(RESULT_OK, i);
        finish();
    } 
}
