package com.isecpartners.samplesync;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowAccountActivity extends Activity{
	
	private static final String TAG = "ShowAccountAcitvity";
	private LinearLayout layout;
	private LinearLayout.LayoutParams p = new LinearLayout.LayoutParams( 
    		LinearLayout.LayoutParams.MATCH_PARENT, 
    		LinearLayout.LayoutParams.WRAP_CONTENT);
	public String acctType, acctName;

	public void onCreate(Bundle icicle){
    	
    	Log.v(TAG, "onCreate");
        super.onCreate(icicle);
        setContentView(R.layout.acctname_details);
        p.setMargins(0, 20, 0, 20);
        
        Bundle bundle = this.getIntent().getExtras();
        acctType = bundle.getString("ACCOUNT_TYPE");
        acctName = bundle.getString("ACCOUNT_NAME");
        
        
        TextView tv = new TextView(getApplicationContext());
        tv.setText(acctName);
    	tv.setTextColor(0xffffffff);
    	tv.setBackgroundColor(Color.argb(255, 192, 18, 48));
    	tv.setTextSize(20);
    	tv.setTypeface(Typeface.DEFAULT, 1);
    	tv.setLayoutParams(p);
        layout = (LinearLayout) findViewById(R.id.accounts_desc);
        layout.addView(tv);
	}

}
