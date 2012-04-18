A blob store using local files.

The blob store uses the account manager to store credentials
and uses these credentials to form a path on the sdcard to
get and put blobs of data.

A sync adapter is set up for synching with a sdcard path. It calls
through to the GenericSync to perform all the work once 
the account name is used to create a blob store.


---
Code organization:

AuthService.java        - glue to let android get the AuthAdapter
AuthAdapter.java        - account authenticator for sdcard
AuthActivity.java       - gui for entering sdcard creds

SyncService.java        - glue to let android get the SyncAdapter
SyncAdapter.java        - called for a sync with an sdcard account

Store.java              - blob store


---
for information on programming with the authentication manager and
sync adapters see:

http://www.c99.org/2010/01/23/writing-an-android-sync-provider-part-1/
http://developer.android.com/resources/samples/SampleSyncAdapter/index.html
