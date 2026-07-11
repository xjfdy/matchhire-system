package com.matchhire.matchingservice.service.impl;

import com.matchhire.authservice.grpc.UserResponse;
import com.matchhire.jobservice.grpc.JobResponse;
import com.matchhire.matchingservice.dto.JobMatchResponse;
import com.matchhire.matchingservice.grpc.JobGrpcClient;
import com.matchhire.matchingservice.grpc.UserGrpcClient;
import com.matchhire.matchingservice.service.MatchingService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MatchingServiceImpl implements MatchingService {

    private final JobGrpcClient jobGrpcClient;
    private final UserGrpcClient userGrpcClient;

    public MatchingServiceImpl(JobGrpcClient jobGrpcClient, UserGrpcClient userGrpcClient) {
        this.jobGrpcClient = jobGrpcClient;
        this.userGrpcClient = userGrpcClient;
    }

    @Override
    public List<JobMatchResponse> getRecommendedJobs(String candidateId) {
        UserResponse candidate = userGrpcClient.getUserById(candidateId);
        List<JobResponse> allJobs = jobGrpcClient.getAllJobs();

        return allJobs.stream()
                .map(job -> toJobMatchResponse(job, candidate.getSkillsList()))
                .sorted(Comparator.comparingDouble(JobMatchResponse::getMatchScore).reversed()).toList();
    }

    private JobMatchResponse toJobMatchResponse(JobResponse job, List<String> candidateSkills) {
        double score = calculateMatchScore(candidateSkills, job.getRequiredSkillsList());
        return new JobMatchResponse(job.getId(), job.getTitle(), job.getEmployerId(), score);
    }

    static double calculateMatchScore(List<String> candidateSkills, List<String> jobRequiredSkills) {
        if(jobRequiredSkills.isEmpty()) {
            return 0.0;
        }

        long overlapCount = jobRequiredSkills.stream()
                .filter(candidateSkills::contains)
                .count();

        return (double) overlapCount / jobRequiredSkills.size();
    }
}
