package com.example.eventapp.models;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

public class User {
    @Exclude
    private String userId;
    private String name;
    private boolean isOrganizer;
    private boolean isAdmin;

    private String photoUriString = "";
    private String email;
    private String phoneNumber;
    private boolean notificationOptOut = false;

    public User() {
        // default constructor for firebase
    }

    public User(String name) {
        this.name = name;
    }

    public User(String name, String email){
        this.name = name;
        this.email = email;
        this.phoneNumber = "";
        this.notificationOptOut = false;
    }

    public User(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.notificationOptOut = false;
    }

    public User(String name, String email, String phoneNumber, String photoUriString) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.notificationOptOut = false;
        this.photoUriString = photoUriString;
    }

    public User(String name, boolean isOrganizer) {
        this.name = name;
        this.isOrganizer = isOrganizer;
    }

    public User(String name, boolean isOrganizer, boolean isAdmin) {
        this.name = name;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getPhotoUriString() {
        return photoUriString;
    }

    public void setPhotoUriString(String photoUriString) {this.photoUriString = photoUriString;}

    public Uri getPhotoUri() { return Uri.parse(photoUriString);}

    public Boolean hasPhoto() { return !photoUriString.isEmpty(); }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isNotificationOptOut() {
        return notificationOptOut;
    }

    public void setNotificationOptOut(boolean notificationOptOut) {
        this.notificationOptOut = notificationOptOut;
    }
}
