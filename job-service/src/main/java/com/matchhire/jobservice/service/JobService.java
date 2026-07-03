package com.matchhire.jobservice.service;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface JobService {
    JobResponse createJob(JobRequest jobRequest);

    JobResponse getJobById(UUID id);

    Page<JobResponse> getAllJobs(int page, int size);

    JobResponse updateJob(UUID id, JobRequest jobRequest);

    void deleteJob(UUID id);
}
