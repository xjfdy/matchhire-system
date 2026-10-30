package com.matchhire.applicationservice.service;

import com.matchhire.applicationservice.dto.ApplicationRequest;
import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.model.ApplicationStatus;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {
    ApplicationResponse apply(UUID candidateId, ApplicationRequest request);

    List<ApplicationResponse> getMyApplications(UUID candidateId);

    List<ApplicationResponse> getApplicationsByJobId(UUID jobId, UUID employerId);

    ApplicationResponse getApplicationById(UUID id, UUID userId);

    ApplicationResponse updateStatus(UUID id, ApplicationStatus status, UUID employerId);

    void withdraw(UUID id, UUID candidateId);
}
