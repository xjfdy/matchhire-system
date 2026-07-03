package com.matchhire.jobservice.repository;

import com.matchhire.jobservice.model.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Jobs, UUID> {
}
