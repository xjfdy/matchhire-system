package com.matchhire.applicationservice.controller;

import com.matchhire.applicationservice.dto.ApplicationRequest;
import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.model.ApplicationStatus;
import com.matchhire.applicationservice.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(
            @RequestHeader("X-User-Id") UUID candidateId,
            @Validated @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.apply(candidateId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") UUID candidateId) {
        return ResponseEntity.ok(applicationService.getMyApplications(candidateId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJobId(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(
            @RequestHeader("X-User-Id") UUID candidateId,
            @PathVariable UUID id) {
        applicationService.withdraw(id, candidateId);
        return ResponseEntity.noContent().build();
    }
}
