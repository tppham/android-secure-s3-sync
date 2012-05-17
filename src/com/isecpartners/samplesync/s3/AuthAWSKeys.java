package com.isecpartners.samplesync.s3;

public class AuthAWSKeys {
	
	private String AccessKey = null;
	private String SecretKey = null;
	private String acct = null;
	private String passwd = null;

	public AuthAWSKeys(String acct, String passwd){
		this.acct = acct;
		this.passwd = passwd;
	}
	
	public String getAccessKeyID(){
		
		return AccessKey;
	}
	
	public String getSecretKey(){
		return SecretKey;
	}
	
	public void sendAWSRequest(){
		
		
	}
}
