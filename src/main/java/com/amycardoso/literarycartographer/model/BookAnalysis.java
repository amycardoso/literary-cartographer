package com.amycardoso.literarycartographer.model;

public class BookAnalysis {
    private String title;
    private String author;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private String timePeriod;
    private boolean fictional;
    private String basedOnRealWorld;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public boolean isFictional() {
        return fictional;
    }

    public void setFictional(boolean fictional) {
        this.fictional = fictional;
    }

    public String getBasedOnRealWorld() {
        return basedOnRealWorld;
    }

    public void setBasedOnRealWorld(String basedOnRealWorld) {
        this.basedOnRealWorld = basedOnRealWorld;
    }
}