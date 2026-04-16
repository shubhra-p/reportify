package com.example.reportify.models;

public class Complaint {

    private String complaintId;
    private String userId;
    private String providerId;
    private String serviceType;
    private String title;
    private String description;
    private String status; // PENDING, ACCEPTED, IN_PROGRESS, RESOLVED
    private long timestamp;

    private String urgency;

    private float rating;
    private boolean rated;


    public Complaint() {}

    public Complaint(String complaintId, String userId, String providerId,
                     String serviceType, String title, String description,
                     String status, String urgency, long timestamp) {

        this.complaintId = complaintId;
        this.userId = userId;
        this.providerId = providerId;
        this.serviceType = serviceType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.urgency = urgency;
        this.rating = 0f;
        this.rated = false;
    }

    public String getComplaintId() { return complaintId; }
    public String getUserId() { return userId; }
    public String getProviderId() { return providerId; }
    public String getServiceType() { return serviceType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public String getUrgency() { return urgency; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public boolean isRated() { return rated; }
    public void setRated(boolean rated) { this.rated = rated; }

}

