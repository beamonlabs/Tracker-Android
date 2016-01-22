package com.tokko.beamon.beamontracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
      //  getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendBroadcast(new Intent(LocationService.ACTION_REGISTER));
    }
}
