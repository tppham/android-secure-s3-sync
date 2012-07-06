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


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import com.google.zxing.integration.android.*;
import com.isecpartners.samplesync.R;

import java.io.*; // XXX temp hack for prefill!  remove me!

/**
 * A GUI for entering S3 credentials.  
 * The AccountAuthenticatorActivity allows us to pass results
 * back to our AuthAdapter by calling setAccountAuthenticatorResult.
 */
public class AuthActivity extends AccountAuthenticatorActivity {
    public static final String TAG = "s3.AuthActivity";
    public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.s3";

    private final Handler mCb = new Handler();
    private AccountManager mAcctMgr;
    private Thread mSigninThread;

    private AmazonS3Client s3Client = null;
    
    private TextView mMsgTxt;
    private EditText mAcctIn, mPasswdIn;
    private String mAcct, mPasswd;

        public void signinThread(String acct, String passwd) {
            mAcct = acct;
            mPasswd = passwd;
        }
        
        
        /**
         *  Checks credentials provided by user.
         * listBuckets() is called for this purpose.
         * The method throws AmazonClientException if credentials are incorrect.
         */
        public boolean SetLoginCredentials() {
            if ( s3Client == null) {        
                AWSCredentials credentials = new BasicAWSCredentials( mAcct, mPasswd);
    		    s3Client = new AmazonS3Client( credentials );
    		    try{
    		    s3Client.listBuckets();
    		    return true;
    		    }
    		
    		    catch (AmazonClientException e) {
					Log.v(TAG, "AmazonClientException"+ e);
					
				}
    		    
    		  return false;
            }
            
            return true;
        }

    /*
     * XXX as a temp hack, prefill in the account and password information
     * from the first two lines of /sdcard/secrets.txt.
     * To make testing on an emulator a lot easier.
     */
    void prefillHackXXX() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("/sdcard/secrets.txt"));
            mAcctIn.setText(in.readLine());
            mPasswdIn.setText(in.readLine());
            in.close();
        } catch(Exception e) {
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TAG, "onCreate");
        mAcctMgr = AccountManager.get(this);

        Window w = getWindow();
        w.requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.s3login);
        w.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMsgTxt = (TextView)findViewById(R.id.err_msg);
        mAcctIn = (EditText)findViewById(R.id.acct_edit);
        mPasswdIn = (EditText)findViewById(R.id.passwd_edit);

        prefillHackXXX();
    }

//    @Override
//    protected Dialog onCreateDialog(int id) {
//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setMessage("Authenticating...");
//        dialog.setIndeterminate(true);
//        dialog.setCancelable(true);
//        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            public void onCancel(DialogInterface dialog) {
//                Log.i(TAG, "cancelled");
//                if (mSigninThread != null) {
//                    mSigninThread.interrupt();
//                    finish();
//                }
//            }
//        });
//        return dialog;
//    }

    /**
     * SignIn button was clicked.  Sign in in the background.
     * @param view The Submit button for which this method is invoked
     */
    public void onSignIn(View view) {
    	findViewById(R.id.signin_progress).setVisibility(View.VISIBLE);
    	
        String acct = mAcctIn.getText().toString();
        String passwd = mPasswdIn.getText().toString();
        
        /** TODO
         * Use AWS credentials to get Access ID Key and Secret Key
         */
//        AuthAWSKeys aws = new AuthAWSKeys(acct, passwd);
//        acct = aws.getAccessKeyID();
//        passwd = aws.getSecretKey();

        if(acct.equals("") || passwd.equals("")) {
            mMsgTxt.setText("You must enter an account and password");
            mAcctIn.setText("");
            mPasswdIn.setText("");
            findViewById(R.id.signin_progress).setVisibility(View.GONE);
            return;
        }
        
        signinThread(acct, passwd);
        boolean ok = SetLoginCredentials();
        onSigninDone(mAcct, mPasswd, ok);
    }
    
   
    /**
     *  sends an intent to zxing's QR code scanner application to initiate scanning credentials
     */
    public void onSignInQRcode(View view) {
    	
    	IntentIntegrator integrator = new IntentIntegrator(AuthActivity.this);
    	integrator.initiateScan();
    }
    
    
    /**
     *  read the contents of the QR code and 
     * parse them to get Access Key ID and Secret Access Key 
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	findViewById(R.id.signin_progress).setVisibility(View.VISIBLE);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
          String contents = result.getContents();
          if (contents != null) {
        	  String [] creds = contents.split("\n");
        	  String access_key = creds[0];
        	  String secret_key = creds[1];
        	  
              
              if(access_key.equals("") || secret_key.equals("")) {

            	  mMsgTxt.setText("Invalid S3 Credentials");
                  findViewById(R.id.signin_progress).setVisibility(View.GONE);
                  return;
              }
              
             signinThread(access_key, secret_key);
             boolean ok = SetLoginCredentials();
             onSigninDone(mAcct, mPasswd, ok);
             finish();
        	  
        	  
          } else {       
        	  Log.v(TAG, "Failed to Scan");
          }
        }
      }
    
    /**
     * Callback (through mCb) from SigninThread when completed.
     */
    public void onSigninDone(String acct, String passwd, boolean ok) {
        Log.v(TAG, "onSigninDone: " + ok);
        findViewById(R.id.signin_progress).setVisibility(View.GONE);

        if(!ok) {
            mMsgTxt.setText("Log in failed!");
            mAcctIn.setText("");
            mPasswdIn.setText("");
            return;
        }

        Account a = new Account(acct, ACCOUNT_TYPE);
        mAcctMgr.addAccountExplicitly(a, passwd, null);
        ContentResolver.setSyncAutomatically(a, ContactsContract.AUTHORITY, true);

        Intent i = new Intent();
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, acct);
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        i.putExtra(AccountManager.KEY_AUTHTOKEN, passwd);
      //  i.putExtra(AccountManager.KEY_USERDATA, "");
        setAccountAuthenticatorResult(i.getExtras());
        setResult(RESULT_OK, i);
        Log.v(TAG, "Finishing Authentication!");
        finish();
    }
}
