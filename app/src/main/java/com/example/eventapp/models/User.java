package com.example.eventapp.models;

import com.google.firebase.firestore.Exclude;

public abstract class User {
    @Exclude
    private String userId;
    private String name;
    private String userType;

    public User() {
        // default constructor for firebase
    }

    public User(String userType) {
        this.userType = userType;
    }

    public User(String userType, String name) {
        this.userType = userType;
        this.name = name;
    }

    public User(String userType, String name, String userId) {
        this.userType = userType;
        this.name = name;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
