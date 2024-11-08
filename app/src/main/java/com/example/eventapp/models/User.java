package com.example.eventapp.models;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

/**
 * Represents a User in the application, including attributes such as name, role, and contact details.
 * Provides functionality to track a user’s settings and organizational/admin role.
 */
public class User {
    @Exclude
    private String userId;
    private String name;
    private boolean isOrganizer;
    private boolean isAdmin;

    private String photoUriString = "";
    private String email = "";
    private String phoneNumber = "";
    private boolean notificationOptOut = false;

    public User() {
        // default constructor for firebase
    }

    /**
     * Creates a User with a specified name.
     *
     * @param name the name of the user
     */
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

    /**
     * Gets the unique user device ID for the user.
     *
     * @return the user device ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique user device ID for the user.
     *
     * @param userId the new user device ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the name of the user.
     *
     * @return the user name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name the new user name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the user has organizer privileges.
     *
     * @return true if the user is an organizer, otherwise false
     */
    public boolean isOrganizer() {
        return isOrganizer;
    }

    /**
     * Sets if the user has organizer privileges.
     *
     * @param organizer true if the user is an organizer, otherwise false
     */
    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
    }

    /**
     * Checks if the user has admin privileges.
     *
     * @return true if the user is an admin, otherwise false
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Sets if the user has admin privileges.
     *
     * @param admin true if the user is an admin, otherwise false
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * Gets the user's profile picture URI string.
     *
     * @return String representing the user's profile picture URI.
     */
    public String getPhotoUriString() {
        return photoUriString;
    }

    /**
     * Sets the user's profile picture URI string.
     *
     * @param photoUriString String representing the user's profile picture URI.
     */
    public void setPhotoUriString(String photoUriString) {this.photoUriString = photoUriString;}

    /**
     * Gets the user's profile picture URI, or null if none is found.
     *
     * @return Uri - the user's profile picture URI.
     */
    public Uri getPhotoUri() {
        if (photoUriString != null) {
            return Uri.parse(photoUriString);
        } else {
            return null;
        }
    }

    /**
     * Checks if the user has a photo uri string set.
     *
     * @return true if the user has a photo uri string set, false if not.
     */
    public Boolean hasPhoto() { return !photoUriString.isEmpty(); }

    /**
     * Gets the user’s email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user’s email address.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user’s phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user’s phone number.
     *
     * @param phoneNumber the new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Checks if the user has opted out of notifications.
     *
     * @return true if opted out, otherwise false
     */
    public boolean isNotificationOptOut() {
        return notificationOptOut;
    }

    /**
     * Sets if the user has opted out of notifications.
     *
     * @param notificationOptOut - true if opted out, otherwise false
     */
    public void setNotificationOptOut(boolean notificationOptOut) {
        this.notificationOptOut = notificationOptOut;
    }
}
