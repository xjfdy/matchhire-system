package com.matchhire.jobservice.controller;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.dto.validator.CreateJobValidationGroup;
import com.matchhire.jobservice.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs", description = "API for Job Posting")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @Operation(summary = "Create a new job post")
    public ResponseEntity<JobResponse> createJob(
            @Validated({Default.class, CreateJobValidationGroup.class}) @RequestBody JobRequest jobRequest) {
        JobResponse jobResponse = jobService.createJob(jobRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find a job by ID")
    public ResponseEntity<JobResponse> findJobById(@PathVariable UUID id) {
        JobResponse jobResponse = jobService.getJobById(id);
        return ResponseEntity.ok().body(jobResponse);
    }

    @GetMapping()
    @Operation(summary = "Get all jobs")
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<JobResponse> jobs = jobService.getAllJobs(page, size);
        return ResponseEntity.ok().body(jobs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @RequestBody JobRequest jobRequest) {
        JobResponse jobResponse = jobService.updateJob(id, jobRequest);
        return ResponseEntity.ok().body(jobResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
