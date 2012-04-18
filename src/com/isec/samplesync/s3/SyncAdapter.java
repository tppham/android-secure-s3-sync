package com.isecpartners.samplesync.s3;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.isecpartners.samplesync.Constants;
import com.isecpartners.samplesync.client.NetworkUtilities;
import com.isecpartners.samplesync.client.User;
import com.isecpartners.samplesync.client.User.Status;
import com.isecpartners.samplesync.platform.ContactManager;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Android calls through this interface to request a sync.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAcctMgr;
    private final Context mCtx;
    private String mToken;

    private Date mLastUpdated;

    public SyncAdapter(Context context) {
        super(context, true);
        mCtx = context;
        mAcctMgr = AccountManager.get(context);
    }

    // XXX re-evaluate exception list
    private void _onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) throws Exception {
        List<User> users;
        List<Status> statuses;
        mToken = null;

        mToken = mAcctMgr.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true); 

        users = NetworkUtilities.fetchFriendUpdates(account, mToken, mLastUpdated);
        mLastUpdated = new Date();
        ContactManager.syncContacts(mCtx, account.name, users);
        statuses = NetworkUtilities.fetchFriendStatuses(account, mToken);
        ContactManager.insertStatuses(mCtx, account.name, statuses);
    }

    /* a synch is requested. */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            _onPerformSync(account, extras, authority, provider, syncResult);

        // XXX re-evaluate which of these are needed...
        } catch (final AuthenticatorException e) {
            Log.e(TAG, "onPerformSync", e);
            syncResult.stats.numParseExceptions++;
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "onPerformSync", e);
        } catch (final IOException e) {
            Log.e(TAG, "onPerformSync", e);
            syncResult.stats.numIoExceptions++;
        } catch (final AuthenticationException e) {
            Log.e(TAG, "onPerformSync", e);
            mAcctMgr.invalidateAuthToken(Constants.ACCOUNT_TYPE, mToken);
            syncResult.stats.numAuthExceptions++;
        } catch (final ParseException e) {
            Log.e(TAG, "onPerformSync", e);
            syncResult.stats.numParseExceptions++;
        } catch (final JSONException e) {
            Log.e(TAG, "onPerformSync", e);
            syncResult.stats.numParseExceptions++;
        } catch (final Exception e) {
            Log.e(TAG, "onPerformSync", e);
            syncResult.stats.numIoExceptions++; // XXX?
        }
    }
}
