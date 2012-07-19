package com.isecpartners.samplesync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/* Display home screen - Sync Prefs and Help 
 * */
public class MainActivity extends Activity{
	
	private static final String TAG = "Main Activity";

	public void onCreate(Bundle icicle) {
	    	Log.v(TAG, "Displaying home page");
	        super.onCreate(icicle);
	        setContentView(R.layout.mainlayout);
	   }
	public void displayHomePage(View v){
		/* send an intent to AccountListActivity to list all account names */
		Intent myIntent = new Intent(MainActivity.this, AccountListActivity.class);
		startActivity(myIntent);
	}
	
	public void displayHelpPage(View v){
		/* send an intent to HelpActivity */
		Intent myIntent = new Intent(MainActivity.this, HelpActivity.class);
		startActivity(myIntent);
		
		
	}

}
