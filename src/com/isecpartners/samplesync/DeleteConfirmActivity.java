package com.isecpartners.samplesync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DeleteConfirmActivity extends Activity{
private static final String TAG = "DeleteConfirmActivity";
private RadioGroup confirmation;
private RadioButton confirmButton;

public void onCreate(Bundle icicle){
    	
    	Log.v(TAG, "onCreate");
        super.onCreate(icicle);
        setContentView(R.layout.delete_confirm);        
}

public void getChoice(View v){
	Log.v(TAG, "getChoice()");
	confirmation = (RadioGroup) findViewById(R.id.delete_radio);
    int selectedId = confirmation.getCheckedRadioButtonId();
    confirmButton = (RadioButton) findViewById(selectedId);
    String choice = (String) confirmButton.getText();
    Intent myIntent = new Intent();
    myIntent.putExtra("CHOICE", choice);
    setResult(Activity.RESULT_OK, myIntent);
    finish();
    
}

}
