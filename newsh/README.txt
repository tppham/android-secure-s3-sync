
Some small helpers:

 - run: shell script to do a new test run. read it, its short and helpful.
 - d.py: python helper to format the output of the Sync tests into
   more digestable formats.  You can easily edit it to get different info
   out of the adb logs without having to do more test runs.

work I've been doing is in the Sync.java class.  It tries to
"shadow" account information under our own synch provider.
There are helpers for clearing the data from previous runs,
for shadowing, and for dumping out the state of the contacts
db.  I usually comment out some of run() to do whatever test
I want to do.

When the apk is installed you can register an account
(contacts menu "account" or the accounts & syncs in settings).
The test cases assume an sdcard account named "test".
After adding an account you can mark account contacts as
visible with the contacts menu -> visibility menu.

That said, this work might mostly be in vein because I think
the synch strategy I outlined in ../synch.txt is not necessary
afterall.




--
feel free to delete this directory when it outlives its usefulness.

