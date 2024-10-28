package com.example.eventapp.models;

public class Admin extends User {

    public Admin() {
        // default constructor for firebase
    }

    public Admin(String name) {
        super("Admin", name);
    }
}
