package com.matchhire.matchingservice.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchingServiceImplTest {

    @Test
    void fullOverLap_returnsScoreOfOne() {
        List<String> candidateSkills = List.of("Java", "Spring", "SQL");
        List<String> jobRequiredSkills = List.of("Java", "Spring", "SQL");

        double score = MatchingServiceImpl.calculateMatchScore(candidateSkills, jobRequiredSkills);
        assertEquals(1.0, score);
    }

    @Test
    void partOverlap_returnsScoreOfLessThanOne() {
        List<String> candidateSkills = List.of("Java", "Spring", "JS");
        List<String> jobRequiredSkills = List.of("Java", "Spring", "SQL");

        double score = MatchingServiceImpl.calculateMatchScore(candidateSkills, jobRequiredSkills);
        assertEquals(2.0 / 3.0, score);
    }

    @Test
    void partOverlap_returnsScoreOfZero() {
        List<String> candidateSkills = List.of("HTML", "CSS", "JS");
        List<String> jobRequiredSkills = List.of("Java", "Spring", "SQL");

        double score = MatchingServiceImpl.calculateMatchScore(candidateSkills, jobRequiredSkills);
        assertEquals(0, score);
    }
}
