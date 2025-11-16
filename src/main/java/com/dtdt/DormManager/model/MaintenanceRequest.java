package com.dtdt.DormManager.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;

public class MaintenanceRequest {

    @DocumentId
    private String id;
    
    private String tenantId;
    private String roomId;
    private String issueDescription;
    private String status; // "Pending", "In Progress", "Completed"
    
    @ServerTimestamp
    private Date dateSubmitted;
    
    public MaintenanceRequest() {}
    
    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDateSubmitted() { return dateSubmitted; }
    public void setDateSubmitted(Date dateSubmitted) { this.dateSubmitted = dateSubmitted; }
}