package com.tokko.beamon.beamontracker;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String ACTION_REGISTER = "com.tokko.beamon.beamontracker.action.REGISTER";

    private GoogleApiClient googleApiClient;

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleIntent(intent);
        }
    };

    public LocationService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (br != null) {
            unregisterReceiver(br);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        registerReceiver(br, new IntentFilter(ACTION_REGISTER));
        return Service.START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (googleApiClient == null)
                getClient();
            if (ACTION_REGISTER.equals(action)) {
                if (googleApiClient.isConnected())
                    isConnected();
                else
                    googleApiClient.connect();
            }
        }
    }

    private void getClient() {
        // Create an instance of GoogleAPIClient.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        isConnected();
    }

    private void isConnected() {
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("locations_enabled", true)) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
            User user = new User(getSharedPreferences(LoginActivity.class.getSimpleName(), MODE_PRIVATE).getString(LoginActivity.PREF_EMAIL, ""));
            String username = user.getFullName();
            Firebase ref = new Firebase("https://crackling-torch-7934.firebaseio.com/beamontracker/users/" + username);
            ref.removeValue();
            return;
        }
        LocationRequest request = new LocationRequest();
        request.setInterval(1000 * 60); //one minute interval
        request.setFastestInterval(1000);

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String email = getSharedPreferences(LoginActivity.class.getSimpleName(), MODE_PRIVATE).getString(LoginActivity.PREF_EMAIL, "");
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://crackling-torch-7934.firebaseio.com/beamontracker/");
        Firebase posts = ref.child("users/" + new User(email).getFullName());
        User user = new User(email,
                location.getLongitude(),
                location.getLatitude());
        posts.setValue(user);
    }

}
