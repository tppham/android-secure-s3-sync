package com.isecpartners.samplesync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.SyncResult;
import android.util.Log;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.ParseException;
import org.json.JSONException;
import java.io.IOException;

/**
 * Generic sync handler that doesn't care which type of
 * accounts or backends we're synchronizing through.
 */
public class GenericSync {
    private static final String TAG = "GenericSync";

    // XXX re-evaluate exception list
    private static void _onPerformSync(Context ctx, IBlobStore store, SyncResult res) throws Exception {
        /*
         * XXX: 
            - fetch new contacts from remote (using local metadata)
                - add to contacts list, update metadata
            - list existing contacts, find new ones (using our metadata)
                - send to remote, update local metadata
         */
    }

    /* a synch is requested. */
    public static void onPerformSync(Context ctx, String acctType, String token, IBlobStore store, SyncResult res) {
        Log.v(TAG, "onPerformSync");
        AccountManager mgr = AccountManager.get(ctx);

        try {
            _onPerformSync(ctx, store, res);

        // XXX re-evaluate which of these are needed...
        } catch (final AuthenticatorException e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numParseExceptions++;
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "onPerformSync", e);
        } catch (final IOException e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numIoExceptions++;
        } catch (final AuthenticationException e) {
            Log.e(TAG, "onPerformSync", e);
            mgr.invalidateAuthToken(acctType, token);
            res.stats.numAuthExceptions++;
        } catch (final ParseException e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numParseExceptions++;
        } catch (final JSONException e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numParseExceptions++;
        } catch (final Exception e) {
            Log.e(TAG, "onPerformSync", e);
            res.stats.numIoExceptions++; // XXX?
        }
    }

    /* return a token or null, updating the result status */
    public static String getToken(Context ctx, Account acct, String tokenType, SyncResult res) {
        Log.v(TAG, "getToken");
        AccountManager mgr = AccountManager.get(ctx);

        try {
            return mgr.blockingGetAuthToken(acct, tokenType, true);

        // XXX re-evaluate which of these are needed...
        } catch (final AuthenticatorException e) {
            Log.e(TAG, "getToken", e);
            res.stats.numParseExceptions++;
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "getToken", e);
        } catch (final IOException e) {
            Log.e(TAG, "getToken", e);
            res.stats.numIoExceptions++;
        }
        return null;
    }

}

