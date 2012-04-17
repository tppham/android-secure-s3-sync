A blob store using the S3 service.

The blob store uses the account manager to store credentials
and uses these credentials to make requests to S3 to get and
put blobs of data.


Code organization:

AuthService.java        - glue to let android get the AuthAdapter
AuthAdapter.java        - account authenticator for S3
AuthActivity.java       - gui for entering S3 creds
Store.java              - blob store



for information on programming with the authentication manager see:

http://www.c99.org/2010/01/23/writing-an-android-sync-provider-part-1/
http://developer.android.com/resources/samples/SampleSyncAdapter/index.html
