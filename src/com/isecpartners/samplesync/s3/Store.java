package com.isecpartners.samplesync.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.accounts.AccountManager;
import android.util.Log;

import com.amazonaws.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import com.isecpartners.samplesync.AccountHelper;
import com.isecpartners.samplesync.Constants;
import com.isecpartners.samplesync.IBlobStore;

public class Store implements IBlobStore {
	public static final String TAG = "s3.Store";
	private AmazonS3Client s3client;
	
    public Store(AWSCredentials creds) {
		s3client = new AmazonS3Client(creds);
    }
        
	public Store(String name, String pw) {
        this(new BasicAWSCredentials(name, pw));
	}

    /* return the remote store associated with the account */
    public static IBlobStore getRemoteStore(AccountHelper h) {
        if(!h.mAcct.type.equals(Constants.ACCOUNT_TYPE_S3))
            return null;

        AccountManager mgr = AccountManager.get(h.mCtx);
        String keyid = h.getAcctPref("keyID", null);
        String key = mgr.getPassword(h.mAcct);
        Log.v(TAG, "return s3 store: " + h.mAcct.name + " " +  keyid);
        return new Store(keyid, key);
    }

	public void create(String store) throws IBlobStore.Error {
		try{
			s3client.createBucket(store);
		} catch(AmazonServiceException e){
            if(e.getStatusCode() == 403)
                throw new IBlobStore.AuthError("" + e);
            throw new IBlobStore.IOError("" + e);
		} catch(AmazonClientException e){
            throw new IBlobStore.IOError("" + e);
		}
	}

    public boolean storeExists(String store) throws IBlobStore.Error {
        try {
            s3client.listObjects(store);
            return true;
        } catch(final AmazonServiceException e) {
            if(e.getStatusCode() == 404)
                return false;
            if(e.getStatusCode() == 403)
                throw new IBlobStore.AuthError("" + e);
            throw new IBlobStore.IOError("" + e);
		} catch(AmazonClientException e){
            Log.v(TAG, "Error: " + e);
            throw new IBlobStore.IOError("" + e);
		}
    }

	public ByteBuffer get(String store, String name) throws IBlobStore.Error {
		try {
			S3Object result = s3client.getObject(store, name);
			long length = result.getObjectMetadata().getContentLength();
			S3ObjectInputStream is = result.getObjectContent();

			Log.v(TAG, " length: "+length);
            if(length > 1024 * 1024)
                throw new IBlobStore.IOError("data is too big");
            byte[] buf = new byte[(int)length];
            is.read(buf);
            is.close();
            
            return ByteBuffer.wrap(buf);
		} catch (IOException e) {
            throw new IBlobStore.IOError("" + e);
		} catch(AmazonServiceException e){
            if(e.getStatusCode() == 403)
                throw new IBlobStore.AuthError("" + e);
            if(e.getStatusCode() == 404)
                throw new IBlobStore.NotFoundError("" + e);
            throw new IBlobStore.IOError("" + e);
		} catch(AmazonClientException e){
            throw new IBlobStore.IOError("" + e);
		}
	}

	public void put(String store, String name, ByteBuffer data) throws IBlobStore.Error {
	    InputStream is;
	    ObjectMetadata om;
	    
		try {
			is = new ByteArrayInputStream(data.array(), 0, data.limit());
			om = new ObjectMetadata();
			om.setContentLength(data.limit());
			
			Log.v(TAG, "Content Length:" + data.limit());
			om.setContentType("plain/text");
			
			s3client.putObject(store, name, is, om );
		} catch(AmazonServiceException e){
            if(e.getStatusCode() == 403)
                throw new IBlobStore.AuthError("" + e);
            throw new IBlobStore.IOError("" + e);
		} catch(AmazonClientException e){
            throw new IBlobStore.IOError("" + e);
		}
	}
}
