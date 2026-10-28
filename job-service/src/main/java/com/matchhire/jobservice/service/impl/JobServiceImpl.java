package com.matchhire.jobservice.service.impl;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.exception.JobNotFoundException;
import com.matchhire.jobservice.mapper.JobMapper;
import com.matchhire.jobservice.model.JobType;
import com.matchhire.jobservice.model.Jobs;
import com.matchhire.jobservice.repository.JobRepository;
import com.matchhire.jobservice.service.JobService;
import com.matchhire.jobservice.specification.JobSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class JobServiceImpl implements JobService {

    private JobRepository jobRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public JobResponse createJob(JobRequest jobRequest) {
        Jobs job = JobMapper.toModel(jobRequest);
        job.setCreatedAt(LocalDate.now());
        job.setUpdatedAt(LocalDate.now());
        Jobs savedJob = jobRepository.save(job);
        return JobMapper.toDTO(savedJob);
    }

    @Override
    public JobResponse getJobById(UUID id) {
        Jobs job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));

        return JobMapper.toDTO(job);
    }

    @Override
    public Page<JobResponse> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.findAll(pageable)
                .map(JobMapper::toDTO);
    }

    @Override
    public JobResponse updateJob(UUID id, JobRequest jobRequest) {
        Jobs job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + id));
        job.setTitle(jobRequest.getTitle());
        job.setLocation(jobRequest.getLocation());
        job.setJobType(jobRequest.getJobType());
        job.setMinSalary(jobRequest.getMinSalary());
        job.setMaxSalary(jobRequest.getMaxSalary());
        job.setDescription(jobRequest.getDescription());
        job.setRemote(jobRequest.getRemote());
        job.setUpdatedAt(LocalDate.now());
        job.setRequiredSkills(jobRequest.getRequiredSkills());

        Jobs savedJob = jobRepository.save(job);
        return JobMapper.toDTO(savedJob);
    }

    @Override
    public void deleteJob(UUID id) {
        if (!jobRepository.existsById(id)) {
            throw new JobNotFoundException("Job not found with ID: " + id);
        }
        jobRepository.deleteById(id);
    }

    @Override
    public Page<JobResponse> getJobsByConditions(String title, Boolean remote, JobType jobType,
                                                 Integer minSalary, Integer maxSalary,
                                                 String sortBy, String direction, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(direction), sortBy));

        Specification<Jobs> spec = (root, query, cb) -> cb.conjunction();

        if (title != null && !title.isBlank()) spec = spec.and(JobSpecifications.hasTitleContaining(title));
        if (remote != null)    spec = spec.and(JobSpecifications.hasRemote(remote));
        if (jobType != null)   spec = spec.and(JobSpecifications.hasJobType(jobType));
        if (minSalary != null) spec = spec.and(JobSpecifications.hasMinSalaryGreaterThanOrEqual(minSalary));
        if (maxSalary != null) spec = spec.and(JobSpecifications.hasMaxSalaryLessThanOrEqual(maxSalary));

        return jobRepository.findAll(spec, pageable).map(JobMapper::toDTO);
    }
}
