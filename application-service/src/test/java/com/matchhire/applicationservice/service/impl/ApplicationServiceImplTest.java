package com.matchhire.applicationservice.service.impl;

import com.matchhire.applicationservice.dto.ApplicationRequest;
import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.event.ApplicationEventProducer;
import com.matchhire.applicationservice.exception.DuplicateApplicationException;
import com.matchhire.applicationservice.exception.UnauthorizedException;
import com.matchhire.applicationservice.grpc.JobGrpcClient;
import com.matchhire.applicationservice.model.Application;
import com.matchhire.applicationservice.model.ApplicationStatus;
import com.matchhire.applicationservice.repository.ApplicationRepository;
import com.matchhire.jobservice.grpc.JobResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobGrpcClient jobGrpcClient;

    @Mock
    private ApplicationEventProducer applicationEventProducer;

    @InjectMocks
    private ApplicationServiceImpl applicationService;


    @Test
    void apply_whenAlreadyApplied_throwsDuplicateApplicationException() {
        UUID candidateId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(UUID.randomUUID());

        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, request.getJobId()))
                .thenReturn(true);

        assertThrows(DuplicateApplicationException.class,
                () -> applicationService.apply(candidateId, request));

        verify(jobGrpcClient, never()).getJobById(anyString());
        verify(applicationRepository, never()).save(any());
        verify(applicationEventProducer, never()).publishSubmitted(any());
    }

    @Test
    void apply_whenNotDuplicate_savesApplicationAndPublishesEvent() {
        UUID candidateId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID employerId = UUID.randomUUID();

        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(jobId);
        request.setCoverLetter("I am interested in this position.");

        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId))
                .thenReturn(false);

        JobResponse job = JobResponse.newBuilder()
                .setId(jobId.toString())
                .setEmployerId(employerId.toString())
                .setTitle("Backend Engineer")
                .build();

        when(jobGrpcClient.getJobById(jobId.toString())).thenReturn(job);

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> {
                    Application saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        ApplicationResponse response = applicationService.apply(candidateId, request);

        assertEquals(candidateId.toString(), response.getCandidateId());
        assertEquals(jobId.toString(), response.getJobId());
        assertEquals(employerId.toString(), response.getEmployerId());
        assertEquals("PENDING", response.getStatus());

        verify(applicationEventProducer).publishSubmitted(any(Application.class));
    }

    @Test
    void updateStatus_capturesOldStatusAndPublishesEvent() {
        UUID applicationId = UUID.randomUUID();
        Application application = new Application();
        application.setId(applicationId);
        application.setCandidateId(UUID.randomUUID());
        application.setJobId(UUID.randomUUID());
        application.setEmployerId(UUID.randomUUID());
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.updateStatus(applicationId, ApplicationStatus.REVIEWED);

        verify(applicationEventProducer).publishStatusChanged(any(Application.class), eq(ApplicationStatus.PENDING));
    }

    @Test
    void withdraw_whenNotOwner_throwsUnauthorizedException() {
        UUID applicationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID differentCandidateId = UUID.randomUUID();

        Application application = new Application();
        application.setId(applicationId);
        application.setCandidateId(ownerId);
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(UnauthorizedException.class,
                () -> applicationService.withdraw(applicationId, differentCandidateId));

        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void withdraw_whenNotPending_throwsIllegalStateException() {
        UUID applicationId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        Application application = new Application();
        application.setId(applicationId);
        application.setCandidateId(candidateId);
        application.setStatus(ApplicationStatus.ACCEPTED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        assertThrows(IllegalStateException.class,
                () -> applicationService.withdraw(applicationId, candidateId));

        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void withdraw_whenOwnerAndPending_deletesApplication() {
        UUID applicationId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        Application application = new Application();
        application.setId(applicationId);
        application.setCandidateId(candidateId);
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        applicationService.withdraw(applicationId, candidateId);

        verify(applicationRepository).delete(application);
    }
}
