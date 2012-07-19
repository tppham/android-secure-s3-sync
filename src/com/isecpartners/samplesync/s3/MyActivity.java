package com.isecpartners.samplesync.s3;

import com.isecpartners.samplesync.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyActivity extends Activity{// implements OnAccountsUpdateListener{
	
	public static final String TAG = "s3.MainActivity";
	public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.s3";
	
    private AccountManager mAcctMgr;
	private TextView mWelcomeTxt;
	LinearLayout layout;
	private Account[] a = null;
	private Button startButton;
	
    /* TODO handle multiple S3 sync accounts */
	
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        
        /* Check if the account is already set up */
        try{
            Log.v(TAG, "onCreate");
            
            /* Check if the account is already set up */
            mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
            a = mAcctMgr.getAccountsByType(ACCOUNT_TYPE);
            	
            	 if(a.length  == 0) {
            		 
      	        	Log.v(TAG, "No accounts exist. Starting Auth Activity");
                     /* Load UI */
                     setContentView(R.layout.mainlayout);
                     startButton = (Button) findViewById(R.id.start_button);                     
                     startButton.setOnClickListener(new View.OnClickListener() {
             			
             			public void onClick(View v) {
             				Intent myIntent = new Intent(MyActivity.this, AuthActivity.class);
             				startActivityForResult(myIntent, 0);
             			}
             		});
     	        	
     	        }
            	 
        
            if(a.length == 1){
            	
            	Log.v(TAG, "Account already exists!");
            	setContentView(R.layout.mainlayout);
                startButton = (Button) findViewById(R.id.start_button);
            	startButton.setText("Sync now");
            	
            	mWelcomeTxt = (TextView) findViewById(R.id.welcome_text);
            	mWelcomeTxt.setText("Start synching data");
            	
            	startButton.setOnClickListener(new View.OnClickListener() {
         			
         			public void onClick(View v) {
         				Intent myIntent = new Intent(MyActivity.this, BucketListActivity.class);
         				startActivity(myIntent);
         			}
         		});
            	
            }
        
       
        	 
        }
        catch(Exception e){
        	Log.v(TAG, "Account Checking Exception: "+e);
        	return;
        }
        		 
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	
//    	acct = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//    	passwd = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
//    	bucketName = intent.getStringExtra(AccountManager.KEY_USERDATA);

    	
    	
    	setContentView(R.layout.mainlayout);
        startButton = (Button) findViewById(R.id.start_button);
    	startButton.setText("Sync now");
    	mWelcomeTxt = (TextView) findViewById(R.id.welcome_text);
    	mWelcomeTxt.setText("Start synching data.");
    	
    	 startButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				startActivity(new Intent(MyActivity.this, BucketListActivity.class));
 			}
 		});
    	

    }
}
