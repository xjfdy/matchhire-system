package com.matchhire.jobservice.controller;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.exception.JobNotFoundException;
import com.matchhire.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Test
    void getJobById_whenJobDoesNotExist_returns404() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.getJobById(jobId))
                .thenThrow(new JobNotFoundException("Job not found with ID: " + jobId));

        mockMvc.perform(get("/jobs/{id}", jobId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createJob_withValidBody_returns201() throws Exception {
        JobResponse response = new JobResponse();
        response.setTitle("Java Developer");
        when(jobService.createJob(any(JobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, "\"description\": \"Build backend services\",")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void createJob_withoutDescription_returns400() throws Exception {
        // description is only required in CreateJobValidationGroup
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(60000, "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());

        verify(jobService, never()).createJob(any());
    }

    @Test
    void createJob_withNegativeSalary_returns400() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson(-1, "\"description\": \"Build backend services\",")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minSalary").exists());

        verify(jobService, never()).createJob(any());
    }

    @Test
    void deleteJob_returns204() throws Exception {
        UUID jobId = UUID.randomUUID();

        mockMvc.perform(delete("/jobs/{id}", jobId))
                .andExpect(status().isNoContent());

        verify(jobService).deleteJob(jobId);
    }

    private String jobJson(int minSalary, String descriptionField) {
        return """
                {
                  "title": "Java Developer",
                  "location": "Toronto",
                  "jobType": "FULL_TIME",
                  "employerId": "%s",
                  "minSalary": %d,
                  "maxSalary": 80000,
                  %s
                  "remote": false,
                  "requiredSkills": ["Java", "Spring"]
                }
                """.formatted(UUID.randomUUID(), minSalary, descriptionField);
    }
}
