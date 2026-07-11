package com.matchhire.jobservice.controller;

import com.matchhire.jobservice.exception.JobNotFoundException;
import com.matchhire.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
