package com.matchhire.applicationservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ApplicationRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @Size(max = 2000, message = "Cover letter cannot exceed 2000 characters")
    private String coverLetter;

    private String resumeUrl;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }
}
