package com.matchhire.jobservice.grpc;

import com.matchhire.jobservice.model.Jobs;
import com.matchhire.jobservice.repository.JobRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class JobGrpcServiceImpl extends JobGrpcServiceGrpc.JobGrpcServiceImplBase {
    private final JobRepository jobRepository;

    public JobGrpcServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void getJobById(JobRequest request, StreamObserver<JobResponse> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getId());
            Jobs job = jobRepository.findById(id)
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("Job not found with ID: " + id)
                            .asRuntimeException());

            JobResponse response = JobResponse.newBuilder()
                    .setId(job.getId().toString())
                    .setEmployerId(job.getEmployerId().toString())
                    .setTitle(job.getTitle())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid UUID format")
                    .asRuntimeException());

        }
    }
}
