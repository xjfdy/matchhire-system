package com.matchhire.jobservice.mapper;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.model.JobType;
import com.matchhire.jobservice.model.Jobs;

public class JobMapper {

    public static JobResponse toDTO(Jobs job) {
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId().toString());
        jobResponse.setTitle(job.getTitle());
        jobResponse.setLocation(job.getLocation());
        jobResponse.setJobType(job.getJobType().toString());
        jobResponse.setMinSalary(String.valueOf(job.getMinSalary()));
        jobResponse.setMaxSalary(String.valueOf(job.getMaxSalary()));
        jobResponse.setDescription(job.getDescription());
        jobResponse.setEmployerId(job.getEmployerId().toString());
        jobResponse.setRemote(
                job.getRemote() != null ? job.getRemote().toString() : null);
        jobResponse.setCreatedAt(job.getCreatedAt().toString());
        jobResponse.setUpdatedAt(job.getUpdatedAt().toString());
        jobResponse.setClosedAt(
                job.getClosedAt() != null ? job.getClosedAt().toString() : null);
        jobResponse.setRequiredSkills(job.getRequiredSkills());

        return jobResponse;
    }

    public static Jobs toModel(JobRequest jobRequest) {
        Jobs job = new Jobs();
        job.setTitle(jobRequest.getTitle());
        job.setLocation(jobRequest.getLocation());
        job.setJobType(jobRequest.getJobType());
        job.setMinSalary(jobRequest.getMinSalary());
        job.setMaxSalary(jobRequest.getMaxSalary());
        job.setDescription(jobRequest.getDescription());
        job.setRemote(jobRequest.getRemote());
        job.setEmployerId(jobRequest.getEmployerId());
        job.setRequiredSkills(jobRequest.getRequiredSkills());

        return job;
    }
}
