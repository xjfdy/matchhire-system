package com.matchhire.applicationservice.repository;

import com.matchhire.applicationservice.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    List<Application> findByCandidateId(UUID candidateId);

    List<Application> findByJobId(UUID jobId);
}
