package com.example.eventapp.models;

public class Organizer extends User {

    public Organizer() {
        // default constructor for firebase
    }

    public Organizer(String name) {
        super("Organizer", name);
    }
}
