package com.isecpartners.samplesync.s3;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.isecpartners.samplesync.Passphrase;
import com.isecpartners.samplesync.R;
import com.isecpartners.samplesync.S3Sync;

import android.R.layout;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BucketActivity extends Activity{
	 
	 public static final String TAG = "s3.BucketActivity";
	    public static final String ACCOUNT_TYPE = "com.isecpartners.samplesync.s3";

	    private final Handler mCb = new Handler();
	    private AccountManager mAcctMgr;
		private TextView mBucketTxt;
		private TextView mBucketErrTxt;
		private AWSCredentials credentials;
		LinearLayout layout;
		private String bucketName;
		private Account[] a = null;
	    
	    public void onCreate(Bundle icicle) {
	    	
	    	Log.v(TAG, "Bucket Activity");
	        super.onCreate(icicle);
	        setContentView(R.layout.bucketdetails);
	        layout = (LinearLayout) findViewById(R.id.list_layout);
	        /* Create a bucket for sync if it doesnt exist*/
	        mAcctMgr = (AccountManager) getSystemService(ACCOUNT_SERVICE);;
            a = mAcctMgr.getAccountsByType(ACCOUNT_TYPE);
            
            credentials = new BasicAWSCredentials(a[0].name, mAcctMgr.getPassword(a[0]));
            if (a.length != 1){
            	Log.e(TAG, "Sync account error: "+ a.length);
            }
            
            if(mAcctMgr.getUserData(a[0], AccountManager.KEY_USERDATA) == null){
            	Log.v(TAG, "Sync Bucket does not exist. Creating one!");
            	
            	onCreateBucket(findViewById(R.layout.mainlayout));
            	bucketName = mAcctMgr.getUserData(a[0], AccountManager.KEY_USERDATA);
            	Log.v(TAG, "Created bucket: "+bucketName);
            }
            
            else{
            	Log.v(TAG, "Bucket already exists:  "+ mAcctMgr.getUserData(a[0], AccountManager.KEY_USERDATA));
            }
            
            onListBucket(findViewById(R.id.bucket_details));
	    }
	    
	    String dictionaryFile() {
	        return "basic-words-en.txt";
	    }
	    
	    public void onCreateBucket(View v){
	    	Store s3 = new Store(credentials);
	    	
	    	 try {
	    		 
	    		 mBucketErrTxt = (TextView)findViewById(R.id.main_err_text);
	             InputStream r = getResources().getAssets().open(dictionaryFile());
	             String passphrase = Passphrase.dictionaryPassphrase(r, 5);
	             r.close();
	             
	            String bucketname = Passphrase.hexadecimalKey(8); 
	            //Log.v(TAG, "bucket: "+bucketname);
	 	    	boolean result = s3.create(bucketname);
	 	    	
	 	    	if(result){
	 	    		mAcctMgr.setUserData(a[0], AccountManager.KEY_USERDATA, bucketname);
	 	    	}
	 	    	else{
	 	    		mBucketErrTxt.setText("Failed to create bucket");
	 	    	}
	             
	         }
	         catch (Exception e) {
	             Log.e(TAG, "passphrase", e);
	         }
	    }
	    
	    public void onListBucket(View v){
	    	final Store s3 = new Store(credentials); 
	    	
	    	 try {
	    		 LinearLayout.LayoutParams p = new 
		                 LinearLayout.LayoutParams( 
		                		LinearLayout.LayoutParams.FILL_PARENT, 
		                		LinearLayout.LayoutParams.WRAP_CONTENT 
		                		);
	             /* clear previous view */
	             layout.removeAllViews();
	            bucketName =  mAcctMgr.getUserData(a[0], AccountManager.KEY_USERDATA); 

	            	 Button button = new Button(this);
	                 button.setText("Sync to: "+bucketName); 
	                 layout.addView(button, p);
	                 
	                 button.setOnClickListener(new View.OnClickListener() {
	                     public void onClick(View v) {
	                         /* Code to sync to this bucket */
	                    	 S3Sync s = new S3Sync(BucketActivity.this);
	                    	 
	                    	 /* Get Contacts from device*/
	                    	 byte[] data = s.getContacts();
	                    	 String name = "Android Contacts";
	                    	 s3.put(bucketName, name, data);
	                    	 
	                    	 /* Get Raw contacts */
	                    	 data = s.getRawContacts();
	                    	 name = "Android Raw Contacts";
	                    	 s3.put(bucketName, name, data);
	                    	 
	                    	 /* Get Data */
	                    	 data = s.getData();
	                    	 name = "Android Data";
	                    	 s3.put(bucketName, name, data);
	                    	 
	                     }
	                 });
	            	 
	                 Button button1 = new Button(this);
	                 button1.setText("Sync from: "+bucketName); 
	                 layout.addView(button1, p);
	                 
	                 button1.setOnClickListener(new View.OnClickListener() {
	                     public void onClick(View v) {
	                         /* Code to sync to this bucket */
	                    	 S3Sync s = new S3Sync(BucketActivity.this);

	                    	 
	                    	 /* Get Contacts from S3*/
	                    	 String name = "Android Contacts";
	                    	 byte[] data = s3.get(bucketName, name);
	                    	 /* TODO create contacts and push them onto device */
	                    	 Log.v(TAG, "Contacts: "+data.length+" : "+ (new String(data)));
	                    	 /* Get Raw contacts */ 
	                    	 name = "Android Raw Contacts";
	                    	 data = s3.get(bucketName, name);
	                    	 Log.v(TAG, "Raw Contacts: "+data.length+" : "+ (new String(data)));
	                    	 /* Get Data */
	                    	 name = "Android Data";
	                    	 data = s3.get(bucketName, name);
	                    	 Log.v(TAG, "Data: "+data.length+" : "+ (new String(data)));
	                    	 
	                     }
	                 });
	             
	         }
	         catch (Exception e) {
	             Log.e(TAG, "onListBucket", e);
	         }
	    }

}
