package com.isecpartners.samplesync.sdcard;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
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
 * A GUI for entering sdcard credentials (just a directory name).
 * The AccountAuthenticatorActivity allows us to pass results
 * back to our AuthAdapter by calling setAccountAuthenticatorResult.
 */
public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = "sdcard.AuthActivity";
    public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.sdcard";

    private AccountManager mAcctMgr;

    private TextView mMsgTxt;
    private EditText mAcctIn;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TAG, "onCreate");
        mAcctMgr = AccountManager.get(this);

        Window w = getWindow();
        w.requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.sdcardlogin);
        w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMsgTxt = (TextView)findViewById(R.id.msg);
        mAcctIn = (EditText)findViewById(R.id.acct_edit);
    }

    /**
     * SignIn button was clicked.  
     * @param view The Submit button for which this method is invoked
     */
    public void onSignIn(View view) {
        String acct = mAcctIn.getText().toString();
        if(acct.equals("")) {
            mMsgTxt.setText("You must enter a directory");
            return;
        }
        acct = "/sdcard/" + acct;
        if(!Store.checkStore(acct)) {
            mMsgTxt.setText("Can't use that directory");
            return;
        }

        Account a = new Account(acct, ACCOUNT_TYPE);
        mAcctMgr.addAccountExplicitly(a, "", null);
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
