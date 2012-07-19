package com.isecpartners.samplesync.s3;

import com.isecpartners.samplesync.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AuthOptionsActivity extends Activity{
	
	private RadioGroup S3SigninType;
	private RadioButton S3SigninButton;
	private String TAG = "AuthOptionsActivity";
	public void onCreate(Bundle icicle) {
		
        super.onCreate(icicle);
        setContentView(R.layout.s3login);
	}
	
	public void SelectSigninType(View v){
		S3SigninType = (RadioGroup) findViewById(R.id.signintype);
		final int selectedId1 = S3SigninType.getCheckedRadioButtonId();
        S3SigninButton = (RadioButton) findViewById(selectedId1);
        Log.v(TAG, "Selected signin type: "+ S3SigninButton.getText());
            
        if(selectedId1 == R.id.manual){
        	Intent myIntent = new Intent(AuthOptionsActivity.this, com.isecpartners.samplesync.s3.AuthActivity.class);
 	        Bundle bundle = new Bundle();
        	bundle.putString("SIGNIN_TYPE", "s3login1_1");
        	myIntent.putExtras(bundle);
        	startActivity(myIntent);
        	finish();
        }
        if(selectedId1 == R.id.scan_qrcode){
        	Intent myIntent = new Intent(AuthOptionsActivity.this, com.isecpartners.samplesync.s3.AuthActivity.class);
 	        Bundle bundle = new Bundle();
        	bundle.putString("SIGNIN_TYPE", "s3login1_2");
        	myIntent.putExtras(bundle);
        	startActivity(myIntent);
        	finish();
        }
        if(selectedId1 == R.id.read_creds){
        	Intent myIntent = new Intent(AuthOptionsActivity.this, com.isecpartners.samplesync.s3.AuthActivity.class);
 	        Bundle bundle = new Bundle();
        	bundle.putString("SIGNIN_TYPE", "s3login1_3");
        	myIntent.putExtras(bundle);
        	startActivity(myIntent);
        	finish();
        }
	}

}
