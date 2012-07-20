package com.isecpartners.samplesync;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HelpActivity extends Activity{
	private static final String TAG = "HelpActivity";

	public void onCreate(Bundle icicle){
    	
    	Log.v(TAG, "onCreate");
        super.onCreate(icicle);
        setContentView(R.layout.helppage);
	}

}
