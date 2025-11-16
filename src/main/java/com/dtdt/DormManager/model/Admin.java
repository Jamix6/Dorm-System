package com.dtdt.DormManager.model;

public class Admin extends User {

    private String staffRole;

    // --- THIS IS THE FIX ---
    // A no-arg constructor is required by Firestore
    public Admin() {}
    // --- END FIX ---

    public Admin(String userId, String email, String passwordHash, String fullName, String staffRole) {
        // Make sure this matches your User constructor
        super(userId, email, passwordHash, fullName);
        this.staffRole = staffRole;
    }

    // Getters and setters
    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
}