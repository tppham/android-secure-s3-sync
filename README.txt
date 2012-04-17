



---------------
* Design

** Android

The two major components to hook into the android system are
for interacting with the account manager and the sync system.
The account manager handles storing the credentials we need.
To hook into it we have:

  the auth server - glue that gives android a way to get a hold of
     our authenticator with the onBind method.

  the auth activity - a gui for entering creds

  the authenticator - the portion that can authenticate the account
     (ie. to s3) and get an auth token


To hook into synchronization we need:
  
  the sync server - glue that gives android a way to get a hold of
     our synch adapter with the onBind method.

  the sync adapter - performs the sync when requested by android.
     Android provides the account we need to use, and we can
     ask for an auth token for that account.

** Synching

The user sets up an account and sets up synch on that account
with normal android settings.  When a synch is requested we
perform a synch by:

  XXX to be determined... something like:
  - pull down all new contacts since last time
    - add them to the contact database and update our local notes
  - enumerate contacts and bundle them
    - go through our notes and see if any of them are not present
      on the remode
    - bundle up any new contacts and send to remote
    - update our local notes


** Remote Storage

Remote storage is done to S3 through the BlobStore interface.
Any other BlobStore can be plugged in its place.  The S3 credentials
are stored in the account manager with our S3 authentication
plugins and the account is passed to the sync adapter by android.
During sync the S3 blob store is created with the appropriate
credentials and the blob store's get and put methods are used
to fetch and store data.

XXX data encryption and data formatting...

