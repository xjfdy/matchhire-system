package com.matchhire.applicationservice.controller;

import com.matchhire.applicationservice.dto.ApplicationRequest;
import com.matchhire.applicationservice.dto.ApplicationResponse;
import com.matchhire.applicationservice.exception.ApplicationNotFoundException;
import com.matchhire.applicationservice.exception.DuplicateApplicationException;
import com.matchhire.applicationservice.exception.UnauthorizedException;
import com.matchhire.applicationservice.model.ApplicationStatus;
import com.matchhire.applicationservice.service.ApplicationService;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    private final UUID candidateId = UUID.randomUUID();

    // ---------- apply ----------

    @Test
    void apply_withValidRequest_returns201AndUsesUserIdFromHeader() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setStatus("PENDING");
        when(applicationService.apply(eq(candidateId), any(ApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/applications")
                        .header("X-User-Id", candidateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(applicationService).apply(eq(candidateId), any(ApplicationRequest.class));
    }

    @Test
    void apply_withoutUserIdHeader_returns400() throws Exception {
        // the gateway is supposed to add X-User-Id; without it the request is rejected
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(UUID.randomUUID())))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).apply(any(), any());
    }

    @Test
    void apply_withoutJobId_returns400() throws Exception {
        mockMvc.perform(post("/applications")
                        .header("X-User-Id", candidateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coverLetter\": \"Hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.jobId").value("Job ID is required"));

        verify(applicationService, never()).apply(any(), any());
    }

    @Test
    void apply_whenAlreadyApplied_returns409() throws Exception {
        when(applicationService.apply(eq(candidateId), any(ApplicationRequest.class)))
                .thenThrow(new DuplicateApplicationException("You have already applied for this job"));

        mockMvc.perform(post("/applications")
                        .header("X-User-Id", candidateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(UUID.randomUUID())))
                .andExpect(status().isConflict());
    }

    @Test
    void apply_whenJobServiceUnavailable_returns503() throws Exception {
        // tests the gRPC error -> HTTP status mapping in GlobalExceptionHandler
        when(applicationService.apply(eq(candidateId), any(ApplicationRequest.class)))
                .thenThrow(Status.UNAVAILABLE.withDescription("job-service is down").asRuntimeException());

        mockMvc.perform(post("/applications")
                        .header("X-User-Id", candidateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(UUID.randomUUID())))
                .andExpect(status().isServiceUnavailable());
    }

    // ---------- read ----------

    @Test
    void getMyApplications_returns200WithList() throws Exception {
        when(applicationService.getMyApplications(candidateId))
                .thenReturn(List.of(new ApplicationResponse(), new ApplicationResponse()));

        mockMvc.perform(get("/applications/my").header("X-User-Id", candidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getApplicationById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(applicationService.getApplicationById(id))
                .thenThrow(new ApplicationNotFoundException("Application not found"));

        mockMvc.perform(get("/applications/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ---------- update status ----------

    @Test
    void updateStatus_withValidStatus_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ApplicationResponse response = new ApplicationResponse();
        response.setStatus("ACCEPTED");
        when(applicationService.updateStatus(id, ApplicationStatus.ACCEPTED)).thenReturn(response);

        mockMvc.perform(put("/applications/{id}/status", id).param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void updateStatus_withInvalidStatus_returns400() throws Exception {
        mockMvc.perform(put("/applications/{id}/status", UUID.randomUUID()).param("status", "HIRED"))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).updateStatus(any(), any());
    }

    // ---------- withdraw ----------

    @Test
    void withdraw_whenOwner_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/applications/{id}", id).header("X-User-Id", candidateId))
                .andExpect(status().isNoContent());

        verify(applicationService).withdraw(id, candidateId);
    }

    @Test
    void withdraw_whenNotOwner_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new UnauthorizedException("You can only withdraw your own application"))
                .when(applicationService).withdraw(id, candidateId);

        mockMvc.perform(delete("/applications/{id}", id).header("X-User-Id", candidateId))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String applyJson(UUID jobId) {
        return """
                {
                  "jobId": "%s",
                  "coverLetter": "I'd love to join your team",
                  "resumeUrl": "https://example.com/resume.pdf"
                }
                """.formatted(jobId);
    }
}
