package com.matchhire.jobservice.service.impl;

import com.matchhire.jobservice.dto.JobRequest;
import com.matchhire.jobservice.dto.JobResponse;
import com.matchhire.jobservice.exception.JobNotFoundException;
import com.matchhire.jobservice.model.JobType;
import com.matchhire.jobservice.model.Jobs;
import com.matchhire.jobservice.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    // ---------- create ----------

    @Test
    void createJob_setsTimestampsAndSaves() {
        when(jobRepository.save(any(Jobs.class))).thenAnswer(inv -> {
            Jobs job = inv.getArgument(0);
            job.setId(UUID.randomUUID());   // mock the DB generating an ID
            return job;
        });

        JobResponse response = jobService.createJob(jobRequest("Java Developer"));

        ArgumentCaptor<Jobs> captor = ArgumentCaptor.forClass(Jobs.class);
        verify(jobRepository).save(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().getCreatedAt());
        assertEquals(LocalDate.now(), captor.getValue().getUpdatedAt());
        assertEquals("Java Developer", response.getTitle());
        assertNotNull(response.getId());
    }

    // ---------- get ----------

    @Test
    void getJobById_whenNotFound_throwsJobNotFoundException() {
        UUID id = UUID.randomUUID();
        when(jobRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.getJobById(id));
    }

    // ---------- update ----------

    @Test
    void updateJob_whenNotFound_throwsAndDoesNotSave() {
        UUID id = UUID.randomUUID();
        when(jobRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobService.updateJob(id, jobRequest("New title")));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void updateJob_whenFound_updatesFieldsAndSaves() {
        Jobs existing = existingJob("Old title");
        when(jobRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(jobRepository.save(any(Jobs.class))).thenAnswer(inv -> inv.getArgument(0));

        JobResponse response = jobService.updateJob(existing.getId(), jobRequest("New title"));

        assertEquals("New title", response.getTitle());
        assertEquals(LocalDate.now(), existing.getUpdatedAt());
    }

    // ---------- delete ----------

    @Test
    void deleteJob_whenNotFound_throwsAndDoesNotDelete() {
        UUID id = UUID.randomUUID();
        when(jobRepository.existsById(id)).thenReturn(false);

        assertThrows(JobNotFoundException.class, () -> jobService.deleteJob(id));
        verify(jobRepository, never()).deleteById(any());
    }

    @Test
    void deleteJob_whenFound_deletes() {
        UUID id = UUID.randomUUID();
        when(jobRepository.existsById(id)).thenReturn(true);

        jobService.deleteJob(id);

        verify(jobRepository).deleteById(id);
    }

    // ---------- search ----------

    @Test
    @SuppressWarnings("unchecked")
    void getJobsByConditions_buildsPageableWithPagingAndSort() {
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingJob("Java Developer"))));

        Page<JobResponse> result = jobService.getJobsByConditions(
                "java", true, JobType.FULL_TIME, 50000, 90000,
                "minSalary", "DESC", 1, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobRepository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("minSalary").getDirection());
        assertEquals(1, result.getTotalElements());
    }

    // ---------- helpers ----------

    private JobRequest jobRequest(String title) {
        JobRequest request = new JobRequest();
        request.setTitle(title);
        request.setLocation("Toronto");
        request.setJobType(JobType.FULL_TIME);
        request.setEmployerId(UUID.randomUUID());
        request.setMinSalary(60000);
        request.setMaxSalary(80000);
        request.setDescription("Build backend services");
        request.setRemote(false);
        request.setRequiredSkills(List.of("Java", "Spring"));
        return request;
    }

    private Jobs existingJob(String title) {
        Jobs job = new Jobs();
        job.setId(UUID.randomUUID());
        job.setTitle(title);
        job.setLocation("Toronto");
        job.setJobType(JobType.FULL_TIME);
        job.setEmployerId(UUID.randomUUID());
        job.setMinSalary(60000);
        job.setMaxSalary(80000);
        job.setDescription("Build backend services");
        job.setRemote(false);
        job.setRequiredSkills(List.of("Java"));
        job.setCreatedAt(LocalDate.now().minusDays(3));
        job.setUpdatedAt(LocalDate.now().minusDays(3));
        return job;
    }
}
