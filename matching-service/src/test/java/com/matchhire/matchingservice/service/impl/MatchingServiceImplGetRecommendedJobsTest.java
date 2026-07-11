package com.matchhire.matchingservice.service.impl;

import com.matchhire.authservice.grpc.UserResponse;
import com.matchhire.jobservice.grpc.JobResponse;
import com.matchhire.matchingservice.dto.JobMatchResponse;
import com.matchhire.matchingservice.grpc.JobGrpcClient;
import com.matchhire.matchingservice.grpc.UserGrpcClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceImplGetRecommendedJobsTest {

    @Mock
    private JobGrpcClient jobGrpcClient;

    @Mock
    private UserGrpcClient userGrpcClient;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    @Test
    void getRecommendedJobs_returnsJobsSortedByScoreDescending() {
        UserResponse candidate = UserResponse.newBuilder()
                .setId("candidate-1")
                .addAllSkills(List.of("Java", "Spring"))
                .build();

        JobResponse fullMatchJob = JobResponse.newBuilder()
                .setId("job-1")
                .setTitle("Backend Engineer")
                .setEmployerId("employer-1")
                .addAllRequiredSkills(List.of("Java", "Spring"))
                .build();

        JobResponse noMatchJob = JobResponse.newBuilder()
                .setId("job-2")
                .setTitle("Frontend Engineer")
                .setEmployerId("employer-1")
                .addAllRequiredSkills(List.of("React", "CSS"))
                .build();

        when(userGrpcClient.getUserById("candidate-1")).thenReturn(candidate);
        when(jobGrpcClient.getAllJobs()).thenReturn(List.of(noMatchJob, fullMatchJob));

        List<JobMatchResponse> results = matchingService.getRecommendedJobs("candidate-1");

        assertEquals(2, results.size());
        assertEquals("job-1", results.get(0).getJobId());
        assertEquals(1.0, results.get(0).getMatchScore());
        assertEquals("job-2", results.get(1).getJobId());
        assertEquals(0.0, results.get(1).getMatchScore());
    }
}
