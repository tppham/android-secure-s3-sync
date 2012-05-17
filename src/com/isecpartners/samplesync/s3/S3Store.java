package com.isecpartners.samplesync.s3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.util.Log;
import com.amazonaws.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.isecpartners.samplesync.IBlobStore;

public class S3Store implements IBlobStore {
	
	public static final String TAG = "s3.S3Store";
	private AmazonS3Client s3client;
	
	public S3Store(AWSCredentials credentials){
		s3client = new AmazonS3Client(credentials);
	}

	public boolean create(String store) {
		try{
			s3client.createBucket(store);
			return true;
			
		} catch(AmazonClientException e){
			Log.v(TAG, "create bucket failed: "+e);
		}
		return false;
	}

	public byte[] get(String store, String name) {
		try {
			
			
			S3Object result = s3client.getObject(store, name);
			long length = result.getObjectMetadata().getContentLength();
			S3ObjectInputStream is = result.getObjectContent();

			Log.v(TAG, " length: "+length);

			/* TODO Change this ASAP */
			byte[] buffer = new byte[(int) (length)];
			
			/* Change this ASAP */
			is.read(buffer, 0, (int)length);
			is.close();

			return buffer;
		} catch (IOException e) {
			Log.v(TAG, "Exception: "+e);
			return null;
		} catch (AmazonClientException e){
			Log.v(TAG, "Exception: "+e);
			return null;
		}
	}
	public List<Bucket> getList() {
		// TODO Auto-generated method stub
		List<Bucket> l = s3client.listBuckets();
		if(!l.isEmpty()){
			return l;
		}
		return null;
		
	}

	public boolean put(String store, String name, byte[] data) {
	    InputStream is;
	    ObjectMetadata om;
	    
		try {
			is = new ByteArrayInputStream(data);
			om = new ObjectMetadata();
			om.setContentLength(data.length);
			
			Log.v(TAG, "Content Length:" + data.length);
			om.setContentType("plain/text");
			
			s3client.putObject(store, name, is, om );
		} 	
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}

}
