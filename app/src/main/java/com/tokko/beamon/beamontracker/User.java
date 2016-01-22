package com.tokko.beamon.beamontracker;

public class User {
    public String email;
    public double longitude;
    public double latitude;

    public User(String email, double longitude, double latitude) {
       setEmail(email);
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return email.split("@")[0].replace('.', ' ').replaceAll("\\s([a-z])", " $1".toUpperCase());
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

    @Override
    public boolean equals(Object o) {
        try{
            return email.equals(((User)o).getEmail());
        }
        catch(ClassCastException ignored){
            return false;
        }
    }
}
