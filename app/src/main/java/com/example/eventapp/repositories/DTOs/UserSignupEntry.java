package com.example.eventapp.repositories.DTOs;

import com.example.eventapp.models.User;

public class UserSignupEntry {
    private final User user;
    private String attendanceStatus;
    private boolean isSelected = false;

    public UserSignupEntry(User user, String attendanceStatus) {
        this.user = user;
        this.attendanceStatus = attendanceStatus;
    }

    public User getUser() {
        return user;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
