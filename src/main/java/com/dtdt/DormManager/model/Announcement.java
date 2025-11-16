package com.dtdt.DormManager.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;

public class Announcement {

    @DocumentId
    private String id;
    private String title;
    private String content;
    
    @ServerTimestamp
    private Date datePosted;

    public Announcement() {}

    // --- Getters and Setters ---
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getDatePosted() { return datePosted; }
    public void setDatePosted(Date datePosted) { this.datePosted = datePosted; }
}