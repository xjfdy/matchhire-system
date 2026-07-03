package com.matchhire.applicationservice.mapper;

import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.model.Application;

public class ApplicationMapper {

    public static ApplicationResponse toResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId().toString());
        response.setCandidateId(application.getCandidateId().toString());
        response.setJobId(application.getJobId().toString());
        response.setEmployerId(application.getEmployerId().toString());
        response.setStatus(application.getStatus().name());
        response.setCoverLetter(application.getCoverLetter());
        response.setResumeUrl(application.getResumeUrl());
        response.setAppliedAt(application.getAppliedAt() != null ? application.getAppliedAt().toString() : null);
        response.setUpdatedAt(application.getUpdatedAt() != null ? application.getUpdatedAt().toString() : null);
        return response;
    }
}
