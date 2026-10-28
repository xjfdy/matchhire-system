package com.matchhire.jobservice.repository;

import com.matchhire.jobservice.model.Jobs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Jobs, UUID>, JpaSpecificationExecutor<Jobs> {

    Page<Jobs> findJobsByTitleContaining(String title, Pageable pageable);

}
