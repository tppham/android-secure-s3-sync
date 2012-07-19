package com.isecpartners.samplesync;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

// XXX this needs to go away and references to it should
// go instead t othe GenericSync
public class S3Sync {
	
	 private static final String TAG = "S3Sync";
	 private String string;

	 
	 private Context mCtx;

	    public S3Sync(Context ctx) {
	        mCtx = ctx;
	        string = new String();
	    }

//	 public byte[] getData(){
//		 dumpContacts();
//		 byte[] data = new byte[string.length()];
//		 
//		 data = string.getBytes();
//		 Log.v(TAG, "SL / DL: "+string.length()+"/"+data.length);
//		 return data;
//		 
//	 }
	    
	  public byte[] getData() {
	    	Log.v(TAG, "in dumpData");
	        Cursor c = mCtx.getContentResolver().query(Data.CONTENT_URI, null, null, null, null);
	        int i=0;
	        while(c.moveToNext()) {
	        	String s = allColumns(c);
	        	string = string + s +"\n";
	        	i++;
	        }
	        c.close();
	        Log.v(TAG, "Dumped "+i+" data rows");
	        return string.getBytes();
	        
	    }

	    /* dump the raw contacts table and the data table */
	    public byte[] getRawContacts() {
	        Log.v(TAG, "Dumping Raw Contacts");
	        Cursor c = mCtx.getContentResolver().query(RawContacts.CONTENT_URI, null, null, null, null);
	        int i = 0;
	        while(c.moveToNext()) {
	            int id = c.getInt(3);
	            String s = allColumns(c);
	            string = string + s +"\n";
	            i++;
	        }
	        
	        c.close();
	        Log.v(TAG, "Dumped "+ i+" raw contacts");
	        return string.getBytes();
	    }
	    
		  public byte[] getContacts() {
		    	Log.v(TAG, "in dumpContacts");
		        Cursor c = mCtx.getContentResolver().query(Contacts.CONTENT_URI, null, null, null, null);
		        int i=0;
		        while(c.moveToNext()) {
		        	String s = allColumns(c);
		        	string = string + s +"\n";
		        	i++;
		        }
		        c.close();
		        Log.v(TAG, "Dumped "+i+" contact rows");
		        return string.getBytes();
		        
		    }
	    
	    String allColumns(Cursor c) {
	        String msg = "";
	        int cols = c.getColumnCount();
	        for(int col = 0; col < cols; col++) {
	            if(col > 0)
	                msg += ", ";
	            try {
	                String val = "" + c.getString(col);
	                msg += c.getColumnName(col) + "=" + val.replace('\n', '_');
	            } catch(final Exception e) {
	                msg += c.getColumnName(col) + "=???";
	            }
	        }
	        return msg;
	    }

		public byte[] putContacts() {
			
			return null;
		}

		public byte[] putRawContacts() {
			// TODO Auto-generated method stub
			return null;
		}

		public byte[] putData() {
			// TODO Auto-generated method stub
			return null;
		}

}
