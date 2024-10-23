package com.example.eventapp.models;

public class Entrant extends User{
    private String email;
    private String phoneNumber;
    private boolean notificationOptOut;

    public Entrant(String name, String email){
        super(name);
        this.email = email;
        this.phoneNumber = "";
        this.notificationOptOut = false;
    }

    public Entrant(String name, String email, String phoneNumber) {
        super(name);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.notificationOptOut = false;
    }

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
