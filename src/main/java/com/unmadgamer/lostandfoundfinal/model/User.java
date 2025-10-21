package com.unmadgamer.lostandfoundfinal.model;

public class User {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    // Required no-arg constructor for Jackson
    public User() {
    }

    public User(String username, String password, String email, String firstName, String lastName, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters and Setters (REQUIRED for Jackson)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', email='%s', role='%s'}", username, email, role);
    }
}