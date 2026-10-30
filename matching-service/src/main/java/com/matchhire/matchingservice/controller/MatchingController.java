package com.matchhire.matchingservice.controller;

import com.matchhire.matchingservice.dto.JobMatchResponse;
import com.matchhire.matchingservice.service.MatchingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/recommended-jobs")
    public List<JobMatchResponse> getRecommendedJobs(@RequestHeader("X-User-Id") String candidateId) {
        return matchingService.getRecommendedJobs(candidateId);
    }
}
