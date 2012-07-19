package com.isecpartners.samplesync;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Authenticate extends Activity{
	 private static final String TAG = "Generic Authenticate";
	private RadioGroup accountType;
	private RadioButton accountButton;
	public String selectedAccountType = null, selectedS3SigninType = null;

	public void onCreate(Bundle icicle){
	    	
	    	Log.v(TAG, "Authenticate");
	        super.onCreate(icicle);
	        setContentView(R.layout.generic_auth);
	        
	}
	
	public void addAccount(View v){
		accountType = (RadioGroup) findViewById(R.id.accounttype);
        int selectedId = accountType.getCheckedRadioButtonId();
        accountButton = (RadioButton) findViewById(selectedId);
        selectedAccountType = (String) accountButton.getText();
        
		if(selectedAccountType.equals("S3 Account")){
			/* Call S3 AuthOptions activity */
			Intent myIntent = new Intent(Authenticate.this, com.isecpartners.samplesync.s3.AuthOptionsActivity.class);
			startActivity(myIntent);
			finish();
		}
		if(selectedAccountType.equals("SDCard Account")){
			/* Call SD auth activity */
			Intent myIntent = new Intent(Authenticate.this, com.isecpartners.samplesync.sdcard.AuthActivity.class);
			startActivity(myIntent);
			finish();
			
		}
	}

}
