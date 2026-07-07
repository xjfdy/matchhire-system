package com.matchhire.applicationservice.event;

import com.matchhire.applicationservice.model.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

//what the packages look like between Kafka producer and consumer
public class ApplicationEvent {

    private String eventType; // submitted or status_changed
    private UUID applicationId;
    private UUID candidateId;
    private UUID jobId;
    private UUID employerId;
    private ApplicationStatus oldStatus; //null when eventType = SUBMITTED
    private ApplicationStatus newStatus;
    private LocalDateTime timestamp;

    public ApplicationEvent() {

    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getEmployerId() {
        return employerId;
    }

    public void setEmployerId(UUID employerId) {
        this.employerId = employerId;
    }

    public ApplicationStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ApplicationStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ApplicationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
