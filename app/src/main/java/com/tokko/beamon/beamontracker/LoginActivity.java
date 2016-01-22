package com.tokko.beamon.beamontracker;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LoginActivity extends Activity {
    static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final String PREF_EMAIL = "email";
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inside your Activity class onCreate method
        settings = getSharedPreferences(LoginActivity.class.getSimpleName(), 0);
        setSelectedAccountName(settings.getString(PREF_EMAIL, null));
        if (settings.getString(PREF_EMAIL, null) != null) {
            continueToNextActivity();
        } else {
            chooseAccount();
        }

    }

    private void setSelectedAccountName(String accountName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_EMAIL, accountName);
        editor.apply();
    }

    void chooseAccount() {
        startActivityForResult(AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, true, null, null, null, null), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_EMAIL, accountName);
                        editor.apply();
                        continueToNextActivity();
                    }
                }
                break;
        }
    }

    private void continueToNextActivity() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }
}
