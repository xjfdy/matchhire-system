package com.matchhire.matchingservice.dto;

public class JobMatchResponse {

    private String jobId;
    private String title;
    private String employerId;
    private double matchScore;

    public JobMatchResponse(String jobId, String title, String employerId, double matchScore) {
        this.jobId = jobId;
        this.title = title;
        this.employerId = employerId;
        this.matchScore = matchScore;
    }

    public String getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public String getEmployerId() {
        return employerId;
    }

    public double getMatchScore() {
        return matchScore;
    }
}
