

Safety:
-------
This code is still alpha quality.  If you use it on a real
device, you should be safe.  Backup your contacts database
before you start and from time to time.  The "normal"
location is /data/data/com.android.providers.contacts/databases/contacts2.db 
so you can backup with:

  adb pull /data/data/com.android.providers.contacts/databases/contacts2.db saved.db

If you ever need to restore you can:

  adb push saved.db /sdcard/
  adb shell cat /sdcard/saved.db ">" /data/data/com.android.providers.contacts/databases/contacts2.db 

and you should have your old contacts back.
If you are using the sync app and restore your contacts you
should probably also stop using the current synch account and
set up a fresh one.  (XXX more details when they're available)



---------------
* Design


** Philosophy

Android's Contacts Provider allows multiple different sources
to contribute contact information into a central database.
It aggregates this data and provides it to the user.  It also
allows "synch providers" to synch some of this data to remote
sources.  By design, android synch providers are only responsible
for synching data that they own to accounts they control.  
For example, a google.com synch provider can synch all the
data created through your user@gmail.com account.
Unfortunately this means that when you disassociate with an
account, all of those contacts are no longer available to you!

This application works differently.  It intentionally violates
the standard android rules for operating on contacts data.
It reads contacts from several popular sources (unowned
contacts and those created by google and exchange providers)
and backs them up remotely.  It then synchs the remote data
to your contacts database.  It intentionally violates the android
rules by performing edits on contacts that are owned by
other providers in order to bring them into synch. 
When you disassociate with an account, although the
contacts are temporarily deleted from the phone, they
are saved remotely and will be restored during the next sync.

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

  - loading up the current local contacts
  - loading up a stored copy of the contacts as of last synch
  - loading up a remote copy of the contacts
  - merging all three contact lists onto a single list, matched
    up as best as possible.
  - for each contact in the list synch it by:
    - determining if there are both local and remote edits,
      - if so, prefer one to the other to resolve conflict
    - compute the change between new contact and last contact
    - apply the change to the last contact to bring it up to date
    - compute the change between the updated last contact and
      the old (remote or local) contact
    - apply the change to that contact
      - when changes are applied to locals, they are pushed to the db
  - when done, take the updated list of remote contacts and store
    it back on the remote

All synching is done using an internal in memory model of
the contacts databases.  We only care about certain data
types:  name, phone (v2), email.
XXX in the future we should add more, like address, too.
All other contact information is ignored.  Further, we only
ever look at and synch with contacts that are generated
under no account, under an exchange synch account or under a google
synch account.  All other contacts are ignored.



** Remote Storage

Remote storage is done to S3 through the BlobStore interface.
Any other BlobStore can be plugged in its place.  The S3 credentials
are stored in the account manager with our S3 authentication
plugins and the account is passed to the sync adapter by android.
During sync the S3 blob store is created with the appropriate
credentials and the blob store's get and put methods are used
to fetch and store data.

Remote data marshalling is done through the model ContactSetBS,
CData and Marsh modules.  The data format is a simple binary
encoding of the mine type and data1..data9 fields of each data
item.  See Marsh.java for more information.

Data encryption uses AES in EAX mode.  This provides
confidentiality and authentication.  The actual key used
is generated from the user supplied passphrase using
the PBKDF2 key derivation algorithm.

TODO
  - allow user to wipe the synch state and start fresh.
  - allow the user to start a new remote data synch store
  - allow user to manually run a synch
    - and display the logs as it happens?
  - show user last synch time
  - give user option to select how to create new contacts from remote
    - should these new contacts be synched to exchange/google or not
      user can select which 
    - program should show user which account types he's currently using
  - give user option to prefer local or reomte during conflict resolution

  - the s3 auth activity
    - we should probably register for some URL type so that
      we can get QR codes directed to us even outside of our app
      s3sync://name:pw/bucketname ?

  - need preferences.  per account?  can we store extra info in accts?

  - add versioning using the s3 store key as a version number
    and some well known name to point to the head.
    - would be nice to be able to revert to older versions on
      some kind of error, corruption or just user preference

  - XXX figure out what we can do about atomicity with s3

  - let the user choose what to do after another account goes away
    - right now we have a hard coded policy in place to
      keep the contacts.  the user may opt to remove them.

  - we might want to allow the user to pick which account providers
    to synch to.  right now we hard wire this policy choice.

 - make sure that metachars cant be entered into account names!
   XXX important todo!  do soon!
   
 - "last" set stores more info (ie. local and remote IDs, account names)
   than "remote".  We could make a more dense encoding of remote if we
   make it skip those items.

 - account needs preference panel
   - conflict resoltuion - prefer local or prefer remote
   - list of accounts to synch
   - let user change password?
   - show info: last synch time
 - XXX differentiate IO errors and blobstore fetches that have no data
 - XXX investigate how intermitent failures are handled by sync framework

polish:
  - icon for providers

 - something to let the user know how much s3 space is being
   used and what the charges will be like?
   or how much sdcard space is used?


