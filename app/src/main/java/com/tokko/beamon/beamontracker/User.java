package com.tokko.beamon.beamontracker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {
    private final String email;
    private String fullName;
    private double longitude;
    private double latitude;
    private String key;

    public User(String email, String fullName, double longitude, double latitude) {
        this(email, fullName);
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public User(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public String fallbackKey(){
        String name = email.split("@")[0]; //.replace('.', ' '); //.replaceAll(" ([a-z])", " $1".toUpperCase());
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("\\.")) {
                    sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
    public String getFullName() {
        if(fullName == null || fullName.equals(""))
            return fallbackKey();
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
