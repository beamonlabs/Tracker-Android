package com.tokko.beamon.beamontracker;

public class User {
    public String email;
    public double longitude;
    public double latitude;

    public User(String email, double longitude, double latitude) {
        this.email = email;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
