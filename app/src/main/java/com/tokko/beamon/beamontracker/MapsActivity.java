package com.tokko.beamon.beamontracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ChildEventListener, ValueEventListener {

    private GoogleMap mMap;
    private ArrayList<User> users = new UserList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startService(new Intent(getApplicationContext(), LocationService.class).setAction(LocationService.ACTION_REGISTER));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://crackling-torch-7934.firebaseio.com/beamontracker");
        Query q = ref.child("users");
        q.addChildEventListener(this);
        q.addListenerForSingleValueEvent(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mapmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Add a marker in Sydney and move the camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(mMap.po)); //TODO: move camera to user
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        User user;
        try {
             user = extractUser(dataSnapshot);
            if(!user.getEmail().equals(getSharedPreferences(LoginActivity.class.getSimpleName(), MODE_PRIVATE).getString(LoginActivity.PREF_EMAIL, ""))) {
                users.add(user);
            }
            populateMap();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void populateMap() {
        mMap.clear();
        for (User user : users) {
            Marker userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(user.getLatitude(), user.getLongitude())).draggable(false));
            userMarker.setTitle(user.getFullName());
            userMarker.setVisible(true);
        }
    }

    @NonNull
    private User extractUser(DataSnapshot dataSnapshot) {
        HashMap<String, Object> o = (HashMap<String, Object>) dataSnapshot.getValue();
        User u = new User((String)o.get("email"), (double)o.get("longitude"), (double)o.get("latitude"));
        return u;
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        onChildAdded(dataSnapshot, s);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        onChildAdded(dataSnapshot, "");
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {   }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {   }
}
