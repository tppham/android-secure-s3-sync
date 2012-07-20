package com.isecpartners.samplesync;

import com.isecpartners.samplesync.AccountHelper;
import com.isecpartners.samplesync.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AuthNamesActivity extends Activity{
	private String AcctName = null;
	private String Passphrase = null;
	private EditText mAcctName;
	private EditText mPassphrase;
	private TextView mErrText;
	private String TAG = "AuthNamesActivity";
	
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.s3login2); 
	}
	
	public void getAccountInfo(View v){
		mAcctName = (EditText)findViewById(R.id.acct_name2);
		mPassphrase = (EditText)findViewById(R.id.s3_passphrase);
		mErrText = (TextView) findViewById(R.id.err2_msg);
		
		AcctName = mAcctName.getText().toString();
		Passphrase = mPassphrase.getText().toString();
		Log.v(TAG, "Act:" + AcctName + " "+ Passphrase);
		
		if(AcctName.equals("") || Passphrase.equals("")){
			mErrText.setText("Enter Account Name and Passphrase");
			return;
		}
		
		AccountHelper h = new AccountHelper(this, AcctName, Passphrase);
        if(h.accountExists()) {
            mErrText.setText("Account already exists. Enter a new account name");
            mAcctName.setText("");
        }
        
        if(!AcctName.equals("") && !Passphrase.equals("") && !h.accountExists()){
		  Intent i = new Intent();
	        i.putExtra("AccountName", AcctName);
	        i.putExtra("Passphrase", Passphrase);
	        setResult(RESULT_OK, i);
	        finish();
        }
		
		
	}

}
