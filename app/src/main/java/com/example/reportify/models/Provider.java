package com.example.reportify.models;

public class Provider {

    private String uid;
    private String name;
    private String serviceType;
    private String address;
    private double latitude;
    private double longitude;
    private boolean available;
    private double rating;
    private double distanceKm;

    private long ratingCount;

    public Provider() {}

    public Provider(String uid, String name, String serviceType,
                    String address, double latitude, double longitude,
                    boolean available, double rating) {
        this.uid = uid;
        this.name = name;
        this.serviceType = serviceType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.rating = rating;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getServiceType() { return serviceType; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return available; }
    public double getRating() { return rating; }
    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public long getRatingCount() { return ratingCount; }

}
