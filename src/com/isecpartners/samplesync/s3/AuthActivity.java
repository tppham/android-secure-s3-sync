package com.isecpartners.samplesync.s3;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import com.google.zxing.integration.android.*;

import com.isecpartners.samplesync.AccountHelper;
import com.isecpartners.samplesync.IBlobStore;
import com.isecpartners.samplesync.R;

import java.io.*; // XXX temp hack for prefill!  remove me!

/**
 * A GUI for entering S3 credentials.  
 * The AccountAuthenticatorActivity allows us to pass results
 * back to our AuthAdapter by calling setAccountAuthenticatorResult.
 */
public class AuthActivity extends AccountAuthenticatorActivity {
    private static final String TAG = "s3.AuthActivity";
    public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.s3";
    private static final int DIALOG_PROGRESS = 0;
	private static final int AUTH_SDCARD_CREDS = 0x00001010;
	private static final int QR_CODE_CREDS = 0x0000c0de;
    
    private final Handler mCb = new Handler();

    private AccountManager mAcctMgr;
    private AmazonS3Client s3Client = null;
    
    private Context mCtx;
    private TextView mMsgTxt;
    private EditText mNameIn, mKeyIdIn, mKeyIn;
    private Thread mSigninThread;

    /* bg threads for testing creds and creating the state */
    private class SigninThread extends Thread {
        private static final String TAG = "s3.SigninThread";
        public String mName, mKeyId, mKey, mPassphrase;

        public SigninThread(String name, String passphrase, String keyid, String key) {
            mName = name;
            mPassphrase = passphrase;
            mKeyId = keyid;
            mKey = key;
        }

        /* perform the background steps. calls done() when done. */
        public void run() {
            AccountHelper h = new AccountHelper(mCtx, mName, mPassphrase);
            if(h.accountExists()) {
                done("That account already exists");
                return;
            }

            Store s = new Store(mKeyId, mKey);
            try {
                // XXX warn user if store already exists, ask for confirmation
                // give them option to use existing, wipe, or cancel
                if(!h.storeExists(s)) {
                    Log.v(TAG, "create empty s3 store for " + mName);
                    h.initStore(s);
                }
                h.initStore(h.getStateStore());
            } catch(final IBlobStore.AuthError e) {
                Log.e(TAG, "auth error making new acct: " + e);
                done("Invalid credentials");
                return;
            } catch(final IBlobStore.Error e) {
                Log.e(TAG, "error making new acct: " + e);
                done("Error initializing");
                return;
            }

            done(null); // success!
        }

        // callback with our results
        private void done(final String err) {
            final SigninThread thr = this;
            mCb.post(new Runnable() {
                public void run() { onSigninDone(thr, err); }
            });
        }
    };
        
    /*
     * XXX as a temp hack, prefill in the account and password information
     * from the first two lines of /sdcard/secrets.txt.
     * To make testing on an emulator a lot easier.
     */
    void prefillHackXXX() {
        try {
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            BufferedReader in = new BufferedReader(new FileReader(sdcard + "/secrets.txt"));
            mKeyIdIn.setText(in.readLine());
            mKeyIn.setText(in.readLine());
            in.close();
        } catch(Exception e) {
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mCtx = this;

        Log.v(TAG, "onCreate");
        mAcctMgr = AccountManager.get(this);

        Window w = getWindow();
        w.requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.s3login);
        w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMsgTxt = (TextView)findViewById(R.id.err_msg);
        mNameIn = (EditText)findViewById(R.id.name_edit);
        mKeyIdIn = (EditText)findViewById(R.id.keyid_edit);
        mKeyIn = (EditText)findViewById(R.id.key_edit);
    }

    @Override
    public void onStart() {
        super.onStart();
        prefillHackXXX();
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
        String name = mNameIn.getText().toString();
        String keyid = mKeyIdIn.getText().toString();
        String key = mKeyIn.getText().toString();
        if(name.equals("") || key.equals("") || keyid.equals("")) {
            mMsgTxt.setText("You must enter a name, key ID and key");
            return;
        }
        
        //findViewById(R.id.signin_progress).setVisibility(View.VISIBLE);
        showDialog(DIALOG_PROGRESS);
        String passphrase = "the quick brown fox"; // XXX get from gui!
        mSigninThread = new SigninThread(name, passphrase, keyid, key);
        mSigninThread.start();
        /* thread will invoke onSigninDone when done */
    }
    
    /**
     *  sends an intent to zxing's QR code scanner application to initiate scanning credentials
     */
    public void onSignInQRcode(View view) {
        
        IntentIntegrator integrator = new IntentIntegrator(AuthActivity.this);
        integrator.initiateScan();
    }
    
    public void onSignInFromFile(View view) {
    	
  	   /*  call file picker activity to read credentials */	   
  	   Intent myIntent = new Intent(AuthActivity.this, FilePickerActivity.class);
  	   startActivityForResult(myIntent, AUTH_SDCARD_CREDS);
      }
     
     /**
      *  read the contents of the QR code and 
      * parse them to get Access Key ID and Secret Access Key 
      * XXX this needs to be fixed up to also support the account name!
      */
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	String access_key = null;
     	String secret_key = null;
     	String passphrase = "the quick brown fox";
     	if (requestCode == QR_CODE_CREDS){ 
         IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
         if(result == null)
             return;

         String contents = result.getContents();
         if (contents != null) {
             String [] creds = contents.split("\n");
             access_key = creds[0];
             secret_key = creds[1];
             if(access_key.equals("") || secret_key.equals("")) {
                 mMsgTxt.setText("Invalid S3 Credentials");
                 return;
                 }
             }
     	
     	else {       
             Log.v(TAG, "Failed to Scan");
          }
     	}
     	
     	if (requestCode == AUTH_SDCARD_CREDS){
       	  /* read the contents of the file */
       	  access_key = intent.getStringExtra("access_key");
       	  secret_key = intent.getStringExtra("secret_key");
         }
             //findViewById(R.id.signin_progress).setVisibility(View.VISIBLE);
               
             String name = "XXXdummy"; // XXX fetch account name from result
             showDialog(DIALOG_PROGRESS);
             mSigninThread = new SigninThread(name, passphrase,access_key, secret_key);
             mSigninThread.start();
             /* thread will invoke onSigninDone when done */
           
     }
    
    /**
     * Callback (through mCb) from SigninThread when completed.
     * At this point everything's been validated and all state
     * has been initialized or err has an error message.
     */
    public void onSigninDone(SigninThread thr, String err) {
        Log.v(TAG, "onSigninDone: " + err);
        dismissDialog(DIALOG_PROGRESS);

        if(err != null) {
            mMsgTxt.setText(err);
            //mNameIn.setText("");
            //mKeyIdIn.setText("");
            //mKeyIn.setText("");
            return;
        }

        //findViewById(R.id.signin_progress).setVisibility(View.GONE);

        Account a = new Account(thr.mName, ACCOUNT_TYPE);
        mAcctMgr.addAccountExplicitly(a, thr.mKey, null);
        mAcctMgr.setUserData(a, "keyID", thr.mKeyId);
        mAcctMgr.setUserData(a, "passphrase", thr.mPassphrase);
        ContentResolver.setSyncAutomatically(a, ContactsContract.AUTHORITY, true);

        Intent i = new Intent();
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, thr.mName);
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        i.putExtra(AccountManager.KEY_AUTHTOKEN, thr.mKey);
        setAccountAuthenticatorResult(i.getExtras());
        setResult(RESULT_OK, i);
        finish();
        Log.v(TAG, "Finishing Authentication!");
    }
}
