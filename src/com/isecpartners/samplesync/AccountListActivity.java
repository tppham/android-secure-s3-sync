package com.isecpartners.samplesync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AccountListActivity extends Activity{
	private static final String TAG = "AccountListActivity";
    
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

	public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        Log.v(TAG, "OnCreate");
        setContentView(R.layout.account_list);
        p.setMargins(0, 20, 0, 20);
		
        getAccountInfo();
        checkAccounts();
        listS3Accounts();
        listSDAccounts();
        }
	public void onResume(){
		super.onResume();
		Log.v(TAG, "OnResume");
		s3layout.removeAllViews();
		sdlayout.removeAllViews();
		
		getAccountInfo();
        checkAccounts();
        listS3Accounts();
        listSDAccounts();
	}
	
	private void getAccountInfo() {
		s3layout = (LinearLayout) findViewById(R.id.display_accounts);
	    s3mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	    s3_accounts = s3mAcctMgr.getAccountsByType(Constants.ACCOUNT_TYPE_S3);
	      
		sdlayout = (LinearLayout) findViewById(R.id.display_accounts);
		sdmAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	    sd_accounts = sdmAcctMgr.getAccountsByType(Constants.ACCOUNT_TYPE_SD);
	    
	    Log.v(TAG, "s3: "+s3_accounts.length+" sd: "+sd_accounts.length);
	        
		
	}

	private void checkAccounts() {
		/* No accounts exist, add an account */
		if (s3_accounts.length == 0 && sd_accounts.length == 0){
			
			Log.v(TAG, "No accounts exist");
			/* send an intent to add an account */
			Intent myIntent = new Intent(AccountListActivity.this, Authenticate.class);
			startActivity(myIntent);
			Log.v(TAG, "added account");
			finish();
		}
		
		
	}

	/* List all S3 accounts */
	public void listS3Accounts(){
		getAccountInfo();
		
		TextView tv = new TextView(getApplicationContext());
    	tv.setText("AWS S3 Accounts");
    	tv.setTextColor(0xffffffff);
    	tv.setBackgroundColor(Color.argb(255, 88, 88, 88));
    	tv.setTextSize(20);
    	tv.setPadding(10, 10, 10, 10);
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
//        		Log.v(TAG, "acctname["+i+"]: "+acctName);
        		s3tv[i].setText(acctName);
        		s3tv[i].setLayoutParams(p);
        		s3tv[i].setTextColor(0xff000000);        		        		
        		s3tv[i].setTextSize(20);

        		s3layout.addView(s3tv[i]);
        		final String acct = acctName; 
        		s3tv[i].setOnClickListener(new View.OnClickListener() {	
					public void onClick(View v) {
						Intent myIntent = new Intent(AccountListActivity.this, ShowAccountActivity.class);
						
						Bundle bundle = new Bundle();
			        	bundle.putString("ACCOUNT_TYPE", Constants.ACCOUNT_TYPE_S3);
			        	bundle.putString("ACCOUNT_NAME", acct);
			        	myIntent.putExtras(bundle);
			        	startActivity(myIntent);
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
    	tv.setBackgroundColor(Color.argb(255, 88, 88, 88));
    	tv.setTextSize(20);
    	tv.setPadding(10, 10, 10, 10);
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
//        		Log.v(TAG, "acctname["+i+"]: "+acctName);
        		sdtv[i].setText(acctName);
        		sdtv[i].setLayoutParams(p);
        		sdtv[i].setTextColor(0xff000000);
        		sdtv[i].setTextSize(20);
        		
        		sdlayout.addView(sdtv[i]);
        		final String acct = acctName; 
        		sdtv[i].setOnClickListener(new View.OnClickListener() {	
					public void onClick(View v) {
						Intent myIntent = new Intent(AccountListActivity.this, ShowAccountActivity.class);
						
						Bundle bundle = new Bundle();
			        	bundle.putString("ACCOUNT_TYPE", Constants.ACCOUNT_TYPE_SD);
			        	bundle.putString("ACCOUNT_NAME", acct);
			        	myIntent.putExtras(bundle);
			        	startActivity(myIntent);
					}
				});
        		
        		
        		
        		View v = new View(getApplicationContext());
                v.setBackgroundColor(0xff888888);
                v.setLayoutParams(p1);
        		sdlayout.addView(v);
        	}
        	
        }
	}
	
	public void addAccount(View v){
		Intent myIntent = new Intent(AccountListActivity.this, ChooseCredsActivity.class);
		startActivity(myIntent);
	}
	
	public void syncAllAccounts(View v){
		Log.v(TAG, "In syncAllAccounts()");
		getAccountInfo();
		
		/* sync all S3 accounts */
		for (int i=0; i<s3_accounts.length;i++){
			Bundle bundle = new Bundle();
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(s3_accounts[i], ContactsContract.AUTHORITY, bundle);
			Log.v(TAG, "Finished synching: "+s3_accounts[i].name);
			
		}
		
		/* sync all SD card accounts */
		for (int i=0; i<sd_accounts.length;i++){
			Bundle bundle = new Bundle();
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
			bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(sd_accounts[i], ContactsContract.AUTHORITY , bundle);
			Log.v(TAG, "Finished synching: "+sd_accounts[i].name);
			
		}
		
		
	}
		 	
}
