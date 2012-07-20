package com.isecpartners.samplesync.s3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.util.Log;

/**
 * Android AccountManager calls through this interface 
 * for all S3 related authentication requests.
 */
class AuthAdapter extends AbstractAccountAuthenticator {
    private static final String TAG = "s3.AuthAdapter";
    private final Context mCtx;

    public AuthAdapter(Context context) {
        super(context);
        mCtx = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
        /* Return an intent to start up our account GUI */
        Bundle b = new Bundle();
        Intent i = new Intent(mCtx, AuthOptionsActivity.class);
        i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        b.putParcelable(AccountManager.KEY_INTENT, i);
        return b;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        Log.v(TAG, "confirmCredentials");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.v(TAG, "editProperties");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) { 
        Log.v(TAG, "getAuthToken");
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.v(TAG, "getAuthTokenType");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        Log.v(TAG, "hasFeatures");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials");
        return null;
    }
}
