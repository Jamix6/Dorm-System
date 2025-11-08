package com.dtdt.DormManager.model;

public class Tenant extends User {

    
    private int currentYear;
    private String assignedRoomID;

    // The constructor must call the parent 'User' constructor using 'super()'
    public Tenant(String userId, String email, String password, String fullName, int currentYear) {
        super(userId, email, password, fullName);
        this.currentYear = currentYear;
    }

    public int getCurrentYear() { return currentYear; }
    public void setCurrentYear(int currentYear) { this.currentYear = currentYear; }

    public String getAssignedRoomID() { return assignedRoomID; }
    public void setAssignedRoomID(String assignedRoomID) { this.assignedRoomID = assignedRoomID; }
}