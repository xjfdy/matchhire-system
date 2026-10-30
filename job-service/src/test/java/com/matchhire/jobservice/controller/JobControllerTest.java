package com.matchhire.jobservice.controller;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.exception.JobNotFoundException;
import com.matchhire.jobservice.exception.UnauthorizedException;
import com.matchhire.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(JobController.class)
class JobControllerTest {

    private static final String DESCRIPTION = "\"description\": \"Build backend services\",";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    private final UUID employerId = UUID.randomUUID();

    @Test
    void getJobById_whenJobDoesNotExist_returns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.getJobById(jobId))
                .thenThrow(new JobNotFoundException("Job not found with ID: " + jobId));

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    void createJob_withValidBody_returns201AndUsesEmployerIdFromHeader() throws Exception {
        JobResponse response = new JobResponse();
        response.setTitle("Java Developer");
        when(jobService.createJob(any(JobRequest.class), eq(employerId))).thenReturn(response);

        mockMvc.perform(post("/jobs")
                        .header("X-User-Id", employerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, DESCRIPTION)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobService).createJob(any(JobRequest.class), eq(employerId));
    }

    @Test
    void createJob_withoutUserIdHeader_returns400() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, DESCRIPTION)))
                .andExpect(status().isBadRequest());

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    void createJob_withoutDescription_returns400() throws Exception {
        // description is only required in CreateJobValidationGroup
        mockMvc.perform(post("/jobs")
                        .header("X-User-Id", employerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());

        verify(jobService, never()).createJob(any(), any());
    }

    @Test
    void createJob_withNegativeSalary_returns400() throws Exception {
        mockMvc.perform(post("/jobs")
                        .header("X-User-Id", employerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(-1, DESCRIPTION)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minSalary").exists());

        verify(jobService, never()).createJob(any(), any());
    }

    // ---------- update ----------

    @Test
    void updateJob_withNegativeSalary_returns400() throws Exception {
        // PUT is validated too now
        mockMvc.perform(put("/jobs/{id}", UUID.randomUUID())
                        .header("X-User-Id", employerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(-1, DESCRIPTION)))
                .andExpect(status().isBadRequest());

        verify(jobService, never()).updateJob(any(), any(), any());
    }

    @Test
    void updateJob_whenNotOwner_returns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.updateJob(eq(jobId), any(JobRequest.class), eq(employerId)))
                .thenThrow(new UnauthorizedException("You can only modify jobs you posted"));

        mockMvc.perform(put("/jobs/{id}", jobId)
                        .header("X-User-Id", employerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, DESCRIPTION)))
                .andExpect(status().isForbidden());
    }

    // ---------- delete ----------

    @Test
    void deleteJob_whenOwner_returns204() throws Exception {
        UUID jobId = UUID.randomUUID();

        mockMvc.perform(delete("/jobs/{id}", jobId).header("X-User-Id", employerId))
                .andExpect(status().isNoContent());

        verify(jobService).deleteJob(jobId, employerId);
    }

    @Test
    void deleteJob_whenNotOwner_returns403() throws Exception {
        UUID jobId = UUID.randomUUID();
        doThrow(new UnauthorizedException("You can only modify jobs you posted"))
                .when(jobService).deleteJob(jobId, employerId);

        mockMvc.perform(delete("/jobs/{id}", jobId).header("X-User-Id", employerId))
                .andExpect(status().isForbidden());
    }

    private String jobJson(int minSalary, String descriptionField) {
        return """
                {
                  "title": "Java Developer",
                  "location": "Toronto",
                  "jobType": "FULL_TIME",
                  "minSalary": %d,
                  "maxSalary": 80000,
                  %s
                  "remote": false,
                  "requiredSkills": ["Java", "Spring"]
                }
                """.formatted(minSalary, descriptionField);
    }
}
