package com.isecpartners.samplesync;

import java.io.File;
import java.util.Calendar;

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

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class ShowAccountActivity extends Activity{
	
	private static final String TAG = "ShowAccountAcitvity";
	private LinearLayout layout;
	private LinearLayout.LayoutParams p = new LinearLayout.LayoutParams( 
    		LinearLayout.LayoutParams.MATCH_PARENT, 
    		LinearLayout.LayoutParams.WRAP_CONTENT);
	public String acctType, acctName;
	private AccountManager mAcctMgr;
	private Account[] accounts;
	private AmazonS3Client s3client;
	private AWSCredentials credentials;
	private int index = -1;
	private Account a;
	private String choice;
	private int requestCode = 0x5050;
	
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
        
        mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	  	accounts = mAcctMgr.getAccountsByType(acctType);
	  	index = getAccountIndex();
	  	if(index != -1){
	  		a = accounts[index];
	        showAccountInfo();
	  	}
	  	else{
	  		TextView t = (TextView)findViewById(R.id.err_acctname_details);
	  		t.setText("An error occurred retrieving account details");
	  	}
        
	}
	
	private void showAccountInfo() {
			
		AccountHelper h = new AccountHelper(this, accounts[index]);
			
			/* get last sync time */
		TextView synctv = (TextView)findViewById(R.id.synctimeval);
			
		long synctime = h.getAcctPrefLong("lastSync", 0);
		Calendar c = Calendar.getInstance();	
		c.setTimeInMillis(synctime);
		java.util.Date date = c.getTime();
		String synctext = ""+date;
			
		if (synctime == 0){
			synctext = "error: could not get last sync time.";
			synctv.setTextColor(Color.argb(255, 255, 0, 0));
		}
		synctv.setText(synctext);
		TextView heading = (TextView)findViewById(R.id.acttype);
		if(acctType.equals(Constants.ACCOUNT_TYPE_SD)){	
			heading.setText("Sync Directory");	
			String dir = h.getAcctPref("path", "dir not found");
			Log.v(TAG,"dir: "+dir);			
			TextView dirtv = (TextView)findViewById(R.id.actname);
			dirtv.setText(dir);	
				
			TextView sizetv = (TextView)findViewById(R.id.sizeval);
			String p = dir+"/"+acctName;
			File f = new File(p,"synch");
			if(f.exists() && f.isFile()) {
					long size = f.length();
					String s = ""+size+" bytes";
					sizetv.setText(s);
			}
			else{
				sizetv.setText("error: could not get the size of sync store.");
				sizetv.setTextColor(Color.argb(255, 255, 0, 0));
			}
		}
			
		if(acctType.equals(Constants.ACCOUNT_TYPE_S3)){
				heading.setText("Bucket Name");
				TextView bucketv = (TextView)findViewById(R.id.actname);
				bucketv.setText(acctName);
				
				String access_key = h.getAcctPref("keyID", "No key");
				
				
				Log.v(TAG, "access_key for account" + acctName+" : "+ access_key);
				if(!access_key.equals("No key")){
				credentials = new BasicAWSCredentials(access_key, mAcctMgr.getPassword(a)); 
				s3client = new AmazonS3Client(credentials);
				
				if(s3client != null){
					TextView sizetv = (TextView)findViewById(R.id.sizeval);
				try{
					
					Log.v(TAG, "bucketName: "+acctName);
					String name = acctName.toLowerCase();
					S3Object o = s3client.getObject(name, "synch");
					Log.v(TAG, "bucketName from Object"+o.getBucketName());
					ObjectMetadata om = o.getObjectMetadata();
					Log.v(TAG, "om: "+om.getContentLength());
										
					long size = om.getContentLength();
					String s = ""+size+" bytes";
					sizetv.setText(s);
				}
				catch(AmazonClientException e){
					Log.v(TAG, "error getting bucket size: "+e);
					sizetv.setText("error: could not get the size of S3 sync store.");
					sizetv.setTextColor(Color.argb(255, 255, 0, 0));
				}
				}
				
				}
				
				
			}
			
	
		
		
	}

	public int getAccountIndex(){
		 int index = -1;
		 for(int i=0; i<accounts.length; i++){
		    	if(accounts[i].name.equals(acctName)){
		    		index = i;
		    		break;
		    	}
		    }
		 Log.v(TAG, "index: "+index);
		 return index;
	}
	
	public void deleteAccount(View v){
		Intent myIntent = new Intent(ShowAccountActivity.this, DeleteConfirmActivity.class);
		startActivityForResult(myIntent, requestCode);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if(resultCode == Activity.RESULT_OK){
		  choice = data.getStringExtra("CHOICE");
		  mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);
	  	  accounts = mAcctMgr.getAccountsByType(acctType);
	  	  
	  	   int index = getAccountIndex();
	  	    if(index != -1){
	  	    	if(choice.equals("Yes, I would like to delete the sync data.")){
	  	    		Log.v(TAG,"DELETING: "+ accounts[index].name+ " type: "+ acctType);
					deleteSyncAccountData(accounts[index]);
				}
	  	    
	  	    	mAcctMgr.removeAccount(accounts[index], null, null);
	  	    	finish();
	  	    }
	  }
	}
	
	public void deleteSyncAccountData(Account a) {
		
		AccountHelper h = new AccountHelper(this, a);

		if (acctType.equals(Constants.ACCOUNT_TYPE_S3)){
			/* delete bucket */
			String access_key = h.getAcctPref("keyID", "No key");
			
			Log.v(TAG, "access_key for account" + a.name+" : "+ access_key);
			if(!access_key.equals("No key")){
			credentials = new BasicAWSCredentials(access_key, mAcctMgr.getPassword(a)); 
			s3client = new AmazonS3Client(credentials);
			if(s3client != null){
			try{
				/* delete contents of the bucket first*/
				String name = a.name.toLowerCase();
				s3client.deleteObject(name, "synch");
				/* delete bucket: returns an error if bucket is not empty*/
				s3client.deleteBucket(name);
			}
			catch(AmazonClientException e){
				Log.v(TAG, "error deleting S3 bucket: "+e);
			}
			}
			
			}
		        
		                
		}
		
		if(acctType.equals(Constants.ACCOUNT_TYPE_SD)){
			/* delete sdcard account*/
			 String path = h.getAcctPref("path", "path not found");
			 if(!path.equals("path not found")){
				 Log.v(TAG, "path: "+path);
				 
				 File f = new File(path, a.name);
				 if(!f.isHidden()){
					 if (f.isDirectory()) {
						 Log.v(TAG, f.getAbsolutePath()+"/"+f.getName()+" is a directory.");
					        String[] children = f.list();
					        
					        /* delete contents of the directory */
					        for (int i = 0; i < children.length; i++) {
					        	File del = new File(f, children[i]);
					        	Log.v(TAG, "deleting: "+del);
					            del.delete();
					        }
					    }
				 }
				 /* delete the directory */
				 f.delete();
				 
			 }
			 
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
