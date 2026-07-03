package com.matchhire.applicationservice.service.impl;

import com.matchhire.applicationservice.dto.ApplicationRequest;
import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.exception.ApplicationNotFoundException;
import com.matchhire.applicationservice.exception.DuplicateApplicationException;
import com.matchhire.applicationservice.exception.UnauthorizedException;
import com.matchhire.applicationservice.mapper.ApplicationMapper;
import com.matchhire.applicationservice.model.Application;
import com.matchhire.applicationservice.model.ApplicationStatus;
import com.matchhire.applicationservice.repository.ApplicationRepository;
import com.matchhire.applicationservice.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public ApplicationResponse apply(UUID candidateId, ApplicationRequest request) {
        boolean exists = applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId());
        if (exists) {
            throw new DuplicateApplicationException("You have already applied for this job");
        }

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setJobId(request.getJobId());
        application.setEmployerId(request.getEmployerId());
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        return ApplicationMapper.toResponse(saved);
    }

    @Override
    public List<ApplicationResponse> getMyApplications(UUID candidateId) {
        return applicationRepository.findByCandidateId(candidateId)
                .stream()
                .map(ApplicationMapper::toResponse)
                .toList();
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJobId(UUID jobId) {
        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(ApplicationMapper::toResponse)
                .toList();
    }

    @Override
    public ApplicationResponse getApplicationById(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));
        return ApplicationMapper.toResponse(application);
    }

    @Override
    public ApplicationResponse updateStatus(UUID id, ApplicationStatus status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());

        Application updated = applicationRepository.save(application);
        return ApplicationMapper.toResponse(updated);
    }

    @Override
    public void withdraw(UUID id, UUID candidateId) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + id));

        if (!application.getCandidateId().equals(candidateId)) {
            throw new UnauthorizedException("You are not allowed to withdraw this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be withdrawn");
        }

        applicationRepository.delete(application);
    }
}
