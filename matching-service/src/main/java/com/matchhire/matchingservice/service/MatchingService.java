package com.matchhire.matchingservice.service;

import com.matchhire.matchingservice.dto.JobMatchResponse;

import java.util.List;

public interface MatchingService {
    List<JobMatchResponse> getRecommendedJobs(String candidateId);
}
