package com.tokko.beamon.beamontracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Random;

public class MyMapFragment extends MapFragment implements OnMapReadyCallback, ChildEventListener, ValueEventListener {
    public static final String ACTION_GEOCODE = "action_geocode";
    public static final String EXTRA_ADDRESS = "adress";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_KEY = "key";

    private GoogleMap mMap;
    private HashMap<String, Marker> users = new HashMap<>();
    private String query;
    private GeocodeReceiver gr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
        getActivity().startService(new Intent(getActivity(), LocationService.class).setAction(LocationService.ACTION_REGISTER));
    }

    @Override
    public void onStart() {
        super.onStart();
        if(gr == null) {
            gr = new GeocodeReceiver(this);
            getActivity().registerReceiver(gr, new IntentFilter(ACTION_GEOCODE));
        }
        getUserName();
    }

    private void getUserName()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
       storeUserName();
    }

    private void storeUserName(){
        Cursor c = getActivity().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        String s = (c.getString(c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)));
        c.close();
        getActivity().getSharedPreferences(LoginActivity.class.getSimpleName(), Context.MODE_PRIVATE).edit().putString(LoginActivity.FULL_NAME, s).apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(gr != null){
            getActivity().unregisterReceiver(gr);
            gr = null;
        }
    }

    public void updateGeocode(String key, String address){
        if(!users.containsKey(key)) return;
        users.get(key).setSnippet(address);
    }

    public void filterUsers(String query){
        this.query = query;
        filterUsers();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestFireBase();
    }

    private void requestFireBase() {
        //noinspection ResourceType
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
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
        marker.setVisible(visible);
    }

    private void addMarker(User user) {
        if(users.containsKey(user.getEmail())) return;
        MarkerOptions markerOptions = new MarkerOptions();
        //TODO: set stale markers to red
        markerOptions = markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        LatLng position = new LatLng(user.getLatitude(), user.getLongitude());
        Marker userMarker = mMap.addMarker(markerOptions.position(position).draggable(false));
        userMarker.setTitle(user.getFullName());
        setMarkerVisibility(user.getFullName(), userMarker);
        users.put(user.getEmail(), userMarker);
        getActivity().startService(new Intent(getActivity(), FetchAddressIntentService.class).putExtra(EXTRA_KEY, user.getEmail()).putExtra(EXTRA_LOCATION, position));
    }

    private User extractUser(DataSnapshot dataSnapshot) {
        HashMap<String, Object> o = (HashMap<String, Object>) dataSnapshot.getValue();
        User u = new User((String)o.get("email"), (String)o.get("fullName"), (double)o.get("longitude"), (double)o.get("latitude"));

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
            LatLng position = marker.getPosition();
            getActivity().startService(new Intent(getActivity(), FetchAddressIntentService.class).putExtra(EXTRA_KEY, user.getEmail()).putExtra(EXTRA_LOCATION, position));

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

    private class GeocodeReceiver extends BroadcastReceiver{
        private MyMapFragment f;

        public GeocodeReceiver(MyMapFragment f){
            this.f = f;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if(f == null) return;
            f.updateGeocode(intent.getStringExtra(EXTRA_KEY), intent.getStringExtra(EXTRA_ADDRESS));
        }
    }
}
