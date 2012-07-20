package com.isecpartners.samplesync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowAccountActivity extends Activity{
	
	private static final String TAG = "ShowAccountAcitvity";
	private LinearLayout layout;
	private LinearLayout.LayoutParams p = new LinearLayout.LayoutParams( 
    		LinearLayout.LayoutParams.MATCH_PARENT, 
    		LinearLayout.LayoutParams.WRAP_CONTENT);
	public String acctType, acctName;
	private AccountManager mAcctMgr;
	private Account[] accounts;

	public void onCreate(Bundle icicle){
    	
    	Log.v(TAG, "onCreate");
        super.onCreate(icicle);
        setContentView(R.layout.acctname_details);
        p.setMargins(0, 20, 0, 20);
        
        Bundle bundle = this.getIntent().getExtras();
        acctType = bundle.getString("ACCOUNT_TYPE");
        acctName = bundle.getString("ACCOUNT_NAME");
        
        Log.v(TAG, acctType+":"+acctName);
        
        
        TextView tv = new TextView(getApplicationContext());
        tv.setText(acctName);
    	tv.setTextColor(0xffffffff);
    	tv.setBackgroundColor(Color.argb(255, 88, 88, 88));
    	tv.setTextSize(20);
    	tv.setPadding(10, 10, 10, 10);
    	tv.setTypeface(Typeface.DEFAULT, 1);
    	tv.setLayoutParams(p);
        layout = (LinearLayout) findViewById(R.id.accounts_name);
        layout.addView(tv);
	}
	
	public int getAccountIndex(){
		 int index = -1;
		 for(int i=0; i<accounts.length; i++){
		    	if(accounts[i].name.equals(acctName)){
		    		index = i;
		    		break;
		    	}
		    }
		 return index;
	}
	
	public void deleteAccount(View v){
		
	    mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	    accounts = mAcctMgr.getAccountsByType(acctType);
	   
	   int index = getAccountIndex();
	    if(index != -1){
	    	mAcctMgr.removeAccount(accounts[index], null, null);
	    	finish();
	    }
		
		
	}
	
	public void syncAccount(View v){
		Log.v(TAG, "In SyncAccount");
		mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		accounts = mAcctMgr.getAccountsByType(acctType);
		
		int index = getAccountIndex();
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		Log.v(TAG, "Requesting Sync");
		ContentResolver.requestSync(accounts[index], ContactsContract.AUTHORITY, bundle);
		Log.v(TAG, "Finished synching: "+accounts[index].name);
	}

}
