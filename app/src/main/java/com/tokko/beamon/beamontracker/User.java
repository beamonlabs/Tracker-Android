package com.tokko.beamon.beamontracker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {
    private final String email;
    private String fullName;
    private double longitude;
    private double latitude;

    public User(String email, String fullName, double longitude, double latitude) {
        this(email);
        this.fullName = fullName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public User(String email) {
        this.email = email;
    }

    public String getFullName() {
       return fullName;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        try{
            return email.equals(((User)o).getEmail());
        }
        catch(ClassCastException ignored){
            return false;
        }
    }

    public String getTimestamp(){
        return new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }
}
