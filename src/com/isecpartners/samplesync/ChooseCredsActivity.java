package com.isecpartners.samplesync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ChooseCredsActivity extends Activity{
	
	public String TAG = "ChooseCredsActivity";
	private RadioGroup credsType;
	private RadioButton credsButton;
	private String selectedCredsType;
	
	public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.creds_choice);
        
	}

	public void selectCredentials(View v) {
		
		 credsType = (RadioGroup) findViewById(R.id.credstype);
	        int selectedId = credsType.getCheckedRadioButtonId();
	        credsButton = (RadioButton) findViewById(selectedId);
	        selectedCredsType = (String) credsButton.getText();
		
		if(selectedCredsType.equals("Choose an existing account")){
		
			Intent myIntent = new Intent(ChooseCredsActivity.this, DisplayCredentialsActivity.class);
			startActivity(myIntent);
			finish();
		}
		if(selectedCredsType.equals("Add a new account")){
		
			Intent myIntent = new Intent(ChooseCredsActivity.this, Authenticate.class);
			startActivity(myIntent);
			finish();
			
		}
	}

}
