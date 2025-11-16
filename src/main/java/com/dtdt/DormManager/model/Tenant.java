package com.dtdt.DormManager.model;

public class Tenant extends User {

    private String currentYear;

    // This field is correct
    private String roomID;

    public Tenant() {}

    public Tenant(String userId, String email, String passwordHash, String fullName, String currentYear) {
        super(userId, email, passwordHash, fullName);
        this.currentYear = currentYear;
    }

    // --- Getters and Setters Updated ---

    public String getCurrentYear() { return currentYear; }
    public void setCurrentYear(String currentYear) { this.currentYear = currentYear; }

    // --- THESE METHODS ARE NOW FIXED ---
    public String getRoomID() { return roomID; }
    public void setRoomID(String roomID) { this.roomID = roomID; }
    // --- END FIX ---
}