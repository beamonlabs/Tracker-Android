package com.tokko.beamon.beamontracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Random;

public class MyMapFragment extends MapFragment implements OnMapReadyCallback, ChildEventListener, ValueEventListener {

    private GoogleMap mMap;
    private HashMap<String, Marker> users = new HashMap<>();
    private String query;
    private Random r = new Random(1337);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
        getActivity().startService(new Intent(getActivity(), LocationService.class).setAction(LocationService.ACTION_REGISTER));
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void filterUsers(String query){
        this.query = query;
        filterUsers();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Firebase.setAndroidContext(getActivity());
        Firebase ref = new Firebase("https://crackling-torch-7934.firebaseio.com/beamontracker");
        Query q = ref.child("users");
        q.addChildEventListener(this);
        q.addListenerForSingleValueEvent(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        User user = extractUser(dataSnapshot);
        if(user != null)
            addMarker(user);
    }

    private void filterUsers() {
        for (String key : users.keySet()) {
            Marker marker = users.get(key);
            setMarkerVisibility(key, marker);
        }
    }

    private void setMarkerVisibility(String key, Marker marker) {
        boolean visible = query == null || key.toLowerCase().contains(query);
        marker.setVisible(!marker.isVisible());
    }

    private void addMarker(User user) {

        Marker userMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(user.getLatitude(), user.getLongitude())).draggable(false));
        userMarker.setTitle(user.getFullName());
        //setMarkerVisibility(user.getFullName(), userMarker);
        userMarker.setVisible(false);
        users.put(user.getFullName(), userMarker);
    }

    private User extractUser(DataSnapshot dataSnapshot) {
        HashMap<String, Object> o = (HashMap<String, Object>) dataSnapshot.getValue();
        User u = new User((String)o.get("email"), (double)o.get("longitude"), (double)o.get("latitude"));

        if(u.getEmail() != null && getActivity() != null && !u.getEmail().equals(getActivity().getSharedPreferences(LoginActivity.class.getSimpleName(), Context.MODE_PRIVATE).getString(LoginActivity.PREF_EMAIL, ""))) {
            return u;
        }
        return null;
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        User user = extractUser(dataSnapshot);
        if(user != null){
            Marker marker = users.get(user.getEmail());
            if(marker == null){
                addMarker(user);
                return;
            }
            marker.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        User user = extractUser(dataSnapshot);
        if(user != null){
            Marker marker = users.get(user.getEmail());
            marker.remove();
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {   }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            onChildChanged(ds, "");
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {   }
}
