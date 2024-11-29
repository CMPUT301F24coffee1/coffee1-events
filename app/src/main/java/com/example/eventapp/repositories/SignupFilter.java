package com.example.eventapp.repositories;

/**
 * Represents filter criteria for querying signups.
 * A null field means that filter will not be applied.
 */
public class SignupFilter {
    public Boolean isCancelled = null;
    public Boolean isWaitlisted = null;

    // Chosen means that a user is selected, but has not accepted the invitation yet
    public Boolean isChosen = null;
    public Boolean isEnrolled = null;

    public SignupFilter() {}
}
