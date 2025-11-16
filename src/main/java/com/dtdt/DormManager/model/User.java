package com.dtdt.DormManager.model;

// 1. ADD THIS IMPORT
import com.google.cloud.firestore.annotation.DocumentId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public abstract class User {


    @DocumentId
    private String documentId;

    private String userId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String firstName;
    private String lastName;
    private String userType;
    private String contractID;
    private String genderType;
    private String studentID;

    public User() {}

    public User(String userId, String email, String passwordHash, String fullName) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }


    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // ... (all your other getters/setters) ...
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getContractID() { return contractID; }
    public void setContractID(String contractID) { this.contractID = contractID; }
    public String getGenderType() { return genderType; }
    public void setGenderType(String genderType) { this.genderType = genderType; }
    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}