package com.dtdt.DormManager.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;

public class Invoice {

    @DocumentId
    private String id;
    
    private String tenantId;
    private String contractId;
    
    private double rentAmount;
    private double utilitiesAmount;
    private double totalAmount;
    
    private String status; // "Pending", "Paid"
    private Date dueDate;
    
    @ServerTimestamp
    private Date datePaid;

    // No-arg constructor
    public Invoice() {}

    // --- Getters and Setters ---
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }

    public double getUtilitiesAmount() { return utilitiesAmount; }
    public void setUtilitiesAmount(double utilitiesAmount) { this.utilitiesAmount = utilitiesAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getDatePaid() { return datePaid; }
    public void setDatePaid(Date datePaid) { this.datePaid = datePaid; }
}