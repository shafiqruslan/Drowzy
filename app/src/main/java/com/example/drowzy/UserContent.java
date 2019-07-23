package com.example.drowzy;

public class UserContent {

    public String username;
    public String email;

    public UserContent() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserContent(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
