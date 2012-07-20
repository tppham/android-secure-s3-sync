package com.isecpartners.samplesync;

import com.isecpartners.samplesync.s3.AuthActivity.SigninThread;
import com.isecpartners.samplesync.AuthNamesActivity;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DisplayCredentialsActivity extends Activity{
	private static final String TAG = "DisplayCredentialsActivity";
	private static final String ACCOUNT_TYPE_S3 = "com.isecpartners.samplesync.s3";
	private static final String ACCOUNT_TYPE_SD = "com.isecpartners.samplesync.sdcard"; 
	private static final int ACCT_S3_INFO = 0x3030;
	private static final int ACCT_SD_INFO = 0x4040;
	
	private AccountManager s3mAcctMgr;
	private AccountManager sdmAcctMgr;
	private Account[] s3_accounts;
	private Account[] sd_accounts;
	private LinearLayout s3layout, sdlayout;
	private LinearLayout.LayoutParams p = new LinearLayout.LayoutParams( 
        		LinearLayout.LayoutParams.MATCH_PARENT, 
        		LinearLayout.LayoutParams.WRAP_CONTENT);
	private LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams( 
    		LinearLayout.LayoutParams.MATCH_PARENT, 
    		2);

	private String passphrase;
	private String name;
	private String access_key;
	private String secret_key;
	protected String path;

	public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.display_creds);
        p.setMargins(0, 20, 0, 20);
		
        getCredsInfo();
        listS3Accounts();
        listSDAccounts();
        }
	
	/* Get accounts from Account Manager */
	private void getCredsInfo() {
		s3layout = (LinearLayout) findViewById(R.id.display_creds);
	    s3mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	    s3_accounts = s3mAcctMgr.getAccountsByType(ACCOUNT_TYPE_S3);
	      
		sdlayout = (LinearLayout) findViewById(R.id.display_creds);
		sdmAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	    sd_accounts = sdmAcctMgr.getAccountsByType(ACCOUNT_TYPE_SD);
		
	}

	/* List all S3 accounts */
	public void listS3Accounts(){
		getCredsInfo();
		
		TextView tv = new TextView(getApplicationContext());
    	tv.setText("AWS S3 Accounts");
    	tv.setTextColor(0xffffffff);
    	tv.setBackgroundColor(Color.argb(255, 192, 18, 48));
    	tv.setTextSize(20);
    	tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    	tv.setLayoutParams(p);
    	s3layout.addView(tv);
		
		String acctName = null;
        
        if(s3_accounts.length == 0){
        	/* display a text that no accounts have been created yet */
        	Log.v(TAG, "No S3 Accounts");
        	TextView tv1 = new TextView(getApplicationContext());
        	tv1.setText("You do not have any AWS S3 accounts set up.");
        	tv1.setTextColor(0xff000000);
        	tv1.setTextSize(15);
        	tv1.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        	tv1.setLayoutParams(p);
        	s3layout.addView(tv1);
        	
        	
        }
        
        else{
        	/* list all account names */     
        	
        	final TextView[] s3tv = new TextView[s3_accounts.length];
        	for(int i = 0; i < s3_accounts.length; i++) {
        		   s3tv[i] = new TextView(getApplicationContext());
        		}
        	
        	for (int i=0; i<s3_accounts.length;i++){
        		acctName = s3_accounts[i].name;
        		
        		s3tv[i].setText(acctName);
        		s3tv[i].setLayoutParams(p);
        		s3tv[i].setTextColor(0xff000000);        		        		
        		s3tv[i].setTextSize(20);

        		s3layout.addView(s3tv[i]);
        		final Account a = s3_accounts[i];
        		final AccountManager acctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        		s3tv[i].setOnClickListener(new View.OnClickListener() {	
					

					public void onClick(View v) {
						
						/* Get access key and secret key from the account */
						secret_key = acctMgr.getPassword(a);
						access_key = acctMgr.getUserData(a, "keyID");
						
						/* Get accountname and passphrase */
						Intent myIntent = new Intent(DisplayCredentialsActivity.this, AuthNamesActivity.class);
						startActivityForResult(myIntent, ACCT_S3_INFO);
						
			        	
					}

					
				}); 
        		
        		View v = new View(getApplicationContext());
                v.setBackgroundColor(0xff888888);
                v.setLayoutParams(p1);
        		s3layout.addView(v);
        	}
        
        }
				
	}
	
	/* List all SDCard accounts */
	public void listSDAccounts(){
		
		String acctName = null;
    	TextView tv = new TextView(getApplicationContext());
    	tv.setText("SD Card Accounts");
    	tv.setTextColor(0xffffffff);
    	tv.setBackgroundColor(Color.argb(255, 192, 18, 48));
    	tv.setTextSize(20);
    	tv.setTypeface(Typeface.DEFAULT, 1);
    	tv.setLayoutParams(p);
    	sdlayout.addView(tv);
        
        if(sd_accounts.length == 0){
        	/* display a text that no accounts have been created yet */
        	//sdlayout.removeAllViews();  
        	Log.v(TAG,"No SD Accounts.");
        	TextView tv1 = new TextView(getApplicationContext());
        	tv1.setText("You do not have any SD Card accounts set up.");
        	tv1.setTextColor(0xff000000);
        	tv1.setTextSize(15);
        	tv1.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        	tv1.setLayoutParams(p);
        	s3layout.addView(tv1);
        }
        else{
        	/* list all account names */
        	
        	TextView[] sdtv = new TextView[sd_accounts.length];
        	for(int i = 0; i < sd_accounts.length; i++) {
     		   sdtv[i] = new TextView(getApplicationContext());
     		}
        	for (int i=0; i<sd_accounts.length;i++){
        		acctName = sd_accounts[i].name;
        		sdtv[i].setText(acctName);
        		sdtv[i].setLayoutParams(p);
        		sdtv[i].setTextColor(0xff000000);
        		sdtv[i].setTextSize(20);
        		
        		sdlayout.addView(sdtv[i]);
        		
        		final Account a = sd_accounts[i];
        		final AccountManager acctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        		
        		sdtv[i].setOnClickListener(new View.OnClickListener() {	
					public void onClick(View v) {
						/* Get path */
						path = acctMgr.getUserData(a, "path");
						Intent myIntent = new Intent(DisplayCredentialsActivity.this, AuthNamesActivity.class);
						startActivityForResult(myIntent, ACCT_SD_INFO);
					}
				});
        		
        		
        		
        		View v = new View(getApplicationContext());
                v.setBackgroundColor(0xff888888);
                v.setLayoutParams(p1);
        		sdlayout.addView(v);
        	}
        	
        }
	}
	
	 public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    	if (resultCode == 0){
	        	return;
	        }
	    	if(requestCode == ACCT_S3_INFO){
	     		if(resultCode == Activity.RESULT_OK){
	     		passphrase = intent.getStringExtra("Passphrase");
	     		name = intent.getStringExtra("AccountName");
	     		
	     		com.isecpartners.samplesync.s3.AuthActivity a = new com.isecpartners.samplesync.s3.AuthActivity();
	     		a.mCtx = this;
	     		a.mAcctMgr = AccountManager.get(this);
	             SigninThread mSigninThread = a.new SigninThread(name, passphrase, access_key, secret_key);
	             mSigninThread.start();
	     		}
	     	}
	    	if(requestCode == ACCT_SD_INFO){
	    		
	     		if(resultCode == Activity.RESULT_OK){
	     		passphrase = intent.getStringExtra("Passphrase");
	     		name = intent.getStringExtra("AccountName");
	     		
	     		com.isecpartners.samplesync.sdcard.AuthActivity a = new com.isecpartners.samplesync.sdcard.AuthActivity();
	     		a.mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	     		a.flag = 1;
	     		a.acct = name;
	     		a.dir = path;
	     		a.passphrase = passphrase;
	     		a.mCtx = this;
	     		a.onSignIn(null);
	          
	     		}
	     	}
	    	finish();
	    	
	 }
		 	
}
