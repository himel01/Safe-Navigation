package com.example.safenavigation;

public class LocationInfo {
    private String locationInfo,address;
    private Double lat,lng;

    public LocationInfo() {
    }

    public LocationInfo(String locationInfo, String address, Double lat, Double lng) {
        this.locationInfo = locationInfo;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
